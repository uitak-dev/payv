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
public class LedgerEventOutboxRelay {

    private static final int MAX_ERROR_MESSAGE_LENGTH = 900;

    private final LedgerEventOutboxMapper ledgerEventOutboxMapper;
    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.outbox.batch-size:100}")
    private int batchSize;

    @Value("${rabbitmq.outbox.confirm-timeout-ms:5000}")
    private long confirmTimeoutMs;

    @Value("${rabbitmq.outbox.retry-base-delay-ms:1000}")
    private long retryBaseDelayMs;

    @Value("${rabbitmq.outbox.retry-max-delay-ms:60000}")
    private long retryMaxDelayMs;

    @Scheduled(fixedDelayString = "${rabbitmq.outbox.fixed-delay-ms:2000}")
    public void relayPending() {
        int safeBatchSize = Math.max(batchSize, 1);
        List<LedgerEventOutboxRecord> pending = ledgerEventOutboxMapper.selectPending(OffsetDateTime.now(), safeBatchSize);
        if (pending == null || pending.isEmpty()) {
            return;
        }

        for (LedgerEventOutboxRecord record : pending) {
            publishOne(record);
        }
    }

    private void publishOne(LedgerEventOutboxRecord record) {
        if (record == null) {
            return;
        }

        try {
            LedgerTransactionChangedEvent event = LedgerEventPayloadSerializer.deserialize(record.getPayload());

            CorrelationData correlationData = new CorrelationData(record.getOutboxId());
            rabbitTemplate.convertAndSend(
                    record.getExchangeName(),
                    record.getRoutingKey(),
                    event,
                    message -> applyMessageMetadata(message, record),
                    correlationData
            );

            CorrelationData.Confirm confirm = correlationData.getFuture()
                    .get(Math.max(confirmTimeoutMs, 100L), TimeUnit.MILLISECONDS);

            if (confirm == null || !confirm.isAck()) {
                String reason = confirm == null ? "publisher confirm is null" : confirm.getReason();
                scheduleRetry(record, "publisher nacked: " + safeText(reason));
                return;
            }

            if (correlationData.getReturned() != null) {
                scheduleRetry(record, "returned by broker: " + correlationData.getReturned().toString());
                return;
            }

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

    private Message applyMessageMetadata(Message message, LedgerEventOutboxRecord record) {
        message.getMessageProperties().setMessageId(record.getOutboxId());
        message.getMessageProperties().setHeader("x-outbox-id", record.getOutboxId());
        message.getMessageProperties().setHeader("x-event-type", record.getEventType());
        return message;
    }

    private void scheduleRetry(LedgerEventOutboxRecord record, String rawErrorMessage) {
        int nextRetryCount = Math.max(record.getRetryCount(), 0) + 1;
        long safeBaseDelay = Math.max(retryBaseDelayMs, 100L);
        long safeMaxDelay = Math.max(retryMaxDelayMs, safeBaseDelay);

        int exponent = Math.min(nextRetryCount - 1, 10);
        long delay = safeBaseDelay * (1L << exponent);
        long boundedDelay = Math.min(delay, safeMaxDelay);

        OffsetDateTime nextRetryAt = OffsetDateTime.now().plusNanos(boundedDelay * 1_000_000L);
        String errorMessage = abbreviate(rawErrorMessage);

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
