package com.payv.ledger.infrastructure.messaging;

import com.payv.common.event.ledger.LedgerTransactionChangedEvent;
import com.payv.ledger.infrastructure.persistence.mybatis.mapper.LedgerEventOutboxMapper;
import com.payv.ledger.infrastructure.persistence.mybatis.record.LedgerEventOutboxRecord;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Component
@RequiredArgsConstructor
@Slf4j
/**
 * Ledger Outbox 메시지를 RabbitMQ로 전달하는 릴레이.
 * - PENDING/RETRY Outbox 레코드를 주기적으로 조회해 브로커로 publish한다.
 * - Publisher Confirm/Return 결과에 따라 PUBLISHED 또는 RETRY 상태로 갱신한다.
 * - 브로커 일시 장애가 있어도 재시도로 복구 가능하게 하여, 최종적으로 at-least-once 전달을 달성한다.
 */
public class LedgerEventOutboxRelay {

    private static final int MAX_ERROR_MESSAGE_LENGTH = 900;

    private final LedgerEventOutboxMapper ledgerEventOutboxMapper;
    private final RabbitTemplate rabbitTemplate;

    // 한 번에 가져올 이벤트 개수 (기본 100개)
    @Value("${rabbitmq.outbox.batch-size:100}")
    private int batchSize;

    // RabbitMQ 브로커로부터 응답(Ack)을 기다릴 최대 시간
    @Value("${rabbitmq.outbox.confirm-timeout-ms:5000}")
    private long confirmTimeoutMs;

    // 재시도 시 사용할 기본 지연 시간 (1초)
    @Value("${rabbitmq.outbox.retry-base-delay-ms:1000}")
    private long retryBaseDelayMs;

    // 재시도 지연 시간의 최대 한도 (60초)
    @Value("${rabbitmq.outbox.retry-max-delay-ms:60000}")
    private long retryMaxDelayMs;

    /**
     * 대기 중인 Outbox 이벤트를 배치로 읽어 RabbitMQ로 전송(Relay)한다.
     *
     * Business logic:
     * - 재시도 대상 포함(PENDING/RETRY + nextRetryAt 도달)
     * - oldest-first 순서로 처리
     */
    @Scheduled(fixedDelayString = "${rabbitmq.outbox.fixed-delay-ms:2000}")
    public void relayPending() {
        int safeBatchSize = Math.max(batchSize, 1);

        // 1) 처리 대상 조회: 아직 전송되지 않았거나(PENDING), 재시도 시간이 도달한(RETRY) 레코드 호출
        List<LedgerEventOutboxRecord> pending = ledgerEventOutboxMapper.selectPending(OffsetDateTime.now(), safeBatchSize);
        if (pending == null || pending.isEmpty()) {
            return;
        }

        // 2) 조회된 레코드를 하나씩 순차적으로 발행
        for (LedgerEventOutboxRecord record : pending) {
            publishOne(record);
        }
    }

    /**
     * 특정 레코드를 RabbitMQ로 전송하고 발행 결과를 확인한다.
     */
    private void publishOne(LedgerEventOutboxRecord record) {
        if (record == null) {
            return;
        }

        try {
            // 1) DB에 저장된 JSON 형태의 페이로드를 Java 객체로 변환
            LedgerTransactionChangedEvent event = LedgerEventPayloadSerializer.deserialize(record.getPayload());

            // 2) 발행 결과 확인을 위한 상관관계 데이터 설정 (outboxId를 키로 사용)
            CorrelationData correlationData = new CorrelationData(record.getOutboxId());

            // 3) RabbitMQ로 메시지 전송
            rabbitTemplate.convertAndSend(
                    record.getExchangeName(),
                    record.getRoutingKey(),
                    event,
                    message -> applyMessageMetadata(message, record),
                    correlationData
            );

            // 4) [동기식 대기] 브로커가 메시지를 안전하게 받았는지(Ack) 응답을 기다림
            CorrelationData.Confirm confirm = correlationData.getFuture()
                    .get(Math.max(confirmTimeoutMs, 100L), TimeUnit.MILLISECONDS);

            // 응답이 없거나 브로커가 메시지 수신을 거부한 경우
            if (confirm == null || !confirm.isAck()) {
                String reason = confirm == null ? "publisher confirm is null" : confirm.getReason();
                scheduleRetry(record, "publisher nacked: " + safeText(reason));
                return;
            }

            // 브로커에는 도달했으나 라우팅 키 오류 등으로 반송된 경우
            if (correlationData.getReturned() != null) {
                scheduleRetry(record, "returned by broker: " + correlationData.getReturned().toString());
                return;
            }

            // 5) 모든 확인이 완료되면 DB 상태를 '발행 완료'로 업데이트
            ledgerEventOutboxMapper.markPublished(record.getOutboxId(), OffsetDateTime.now());

        } catch (TimeoutException e) {
            scheduleRetry(record, "publisher confirm timeout");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            scheduleRetry(record, "interrupted while waiting publisher confirm");
        } catch (Exception e) {
            scheduleRetry(record, e.getMessage());
            log.error("Failed to publish outbox event: outboxId={}", record.getOutboxId(), e);
        }
    }

    /**
     * RabbitMQ 메시지 헤더에 추적을 위한 메타데이터 주입
     */
    private Message applyMessageMetadata(Message message, LedgerEventOutboxRecord record) {
        message.getMessageProperties().setMessageId(record.getOutboxId());
        message.getMessageProperties().setHeader("x-outbox-id", record.getOutboxId());
        message.getMessageProperties().setHeader("x-event-type", record.getEventType());
        return message;
    }

    /**
     * [지수 백오프] 실패 시 다음 재시도 시간을 계산하여 DB를 업데이트한다.
     */
    private void scheduleRetry(LedgerEventOutboxRecord record, String rawErrorMessage) {
        int nextRetryCount = Math.max(record.getRetryCount(), 0) + 1;
        long safeBaseDelay = Math.max(retryBaseDelayMs, 100L);
        long safeMaxDelay = Math.max(retryMaxDelayMs, safeBaseDelay);

        // Exponential Backoff 계산: 2^(retryCount-1) * baseDelay (최대 2^10배까지)
        int exponent = Math.min(nextRetryCount - 1, 10);
        long delay = safeBaseDelay * (1L << exponent);

        // 지연 시간이 설정된 최대치를 넘지 않도록 제한
        long boundedDelay = Math.min(delay, safeMaxDelay);

        // 다음 재시도 시점 계산
        OffsetDateTime nextRetryAt = OffsetDateTime.now().plusNanos(boundedDelay * 1_000_000L);
        String errorMessage = abbreviate(rawErrorMessage);

        // DB 업데이트: 재시도 횟수 증가, 다음 실행 시간 및 에러 사유 저장
        ledgerEventOutboxMapper.markRetry(
                record.getOutboxId(),
                nextRetryCount,
                nextRetryAt,
                errorMessage
        );
    }

    private String safeText(String value) {
        return value == null ? "" : value;
    }

    private String abbreviate(String value) {
        String safe = safeText(value);
        if (safe.length() <= MAX_ERROR_MESSAGE_LENGTH) {
            return safe;
        }
        return safe.substring(0, MAX_ERROR_MESSAGE_LENGTH);
    }
}
