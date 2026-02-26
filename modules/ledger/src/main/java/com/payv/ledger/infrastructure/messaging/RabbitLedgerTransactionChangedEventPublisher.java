package com.payv.ledger.infrastructure.messaging;

import com.payv.common.event.ledger.LedgerTransactionChangedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class RabbitLedgerTransactionChangedEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange.ledger.events:payv.ledger.events}")
    private String exchange;

    @Value("${rabbitmq.routing-key.ledger-transaction-changed:ledger.transaction.changed}")
    private String routingKey;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publish(LedgerTransactionChangedEvent event) {
        if (event == null) {
            return;
        }

        try {
            rabbitTemplate.convertAndSend(exchange, routingKey, event);
        } catch (Exception e) {
            log.error("Failed to publish ledger transaction changed event: ownerUserId={}, changeType={}",
                    event.getOwnerUserId(),
                    event.getChangeType(),
                    e);
        }
    }
}
