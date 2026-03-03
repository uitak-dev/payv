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
public class NotificationRabbitConsumer {

    private final NotificationPolicyHandler notificationPolicyHandler;

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
