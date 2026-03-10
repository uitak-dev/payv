package com.payv.notification.infrastructure.messaging;

import com.payv.common.event.ledger.LedgerTransactionChangedEvent;
import com.payv.notification.application.listener.NotificationPolicyHandler;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component("notificationRabbitConsumer")
@RequiredArgsConstructor
@Slf4j
/**
 * RabbitMQ 소비자.
 * - Ledger 거래 변경 이벤트를 수신해 정책 핸들러로 위임한다.
 * - 처리 성공/실패에 따라 manual ack/nack를 수행한다.
 * - 소비 처리와 ACK 타이밍을 분리해 처리 실패 시 재전달을 허용하고
 *   at-least-once 수신을 보장한다.
 */
public class NotificationRabbitConsumer {

    private final NotificationPolicyHandler notificationPolicyHandler;

    /**
     * 거래 변경 이벤트를 소비한다.
     *
     * @param event 역직렬화된 도메인 이벤트
     * @param rawMessage 원본 AMQP 메시지(delivery 태그 포함)
     * @param channel AMQP 채널(ACK/NACK 수행)
     * @throws IOException ACK/NACK 전송 과정에서 I/O 오류가 발생한 경우
     */
    public void handleLedgerTransactionChanged(LedgerTransactionChangedEvent event,
                                               Message rawMessage,
                                               Channel channel) throws IOException {
        long deliveryTag = rawMessage == null ? -1L : rawMessage.getMessageProperties().getDeliveryTag();
        try {
            notificationPolicyHandler.handleLedgerTransactionChanged(event);
            if (deliveryTag > 0L) {
                channel.basicAck(deliveryTag, false);
            }
        } catch (Exception e) {
            if (deliveryTag > 0L) {
                channel.basicNack(deliveryTag, false, true);
            }
            log.error("Failed to consume ledger transaction changed event. deliveryTag={}", deliveryTag, e);
        }
    }
}
