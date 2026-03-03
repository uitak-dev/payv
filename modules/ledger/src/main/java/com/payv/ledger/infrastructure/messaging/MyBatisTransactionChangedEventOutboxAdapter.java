package com.payv.ledger.infrastructure.messaging;

import com.payv.common.event.ledger.LedgerTransactionChangedEvent;
import com.payv.ledger.application.port.TransactionChangedEventOutboxPort;
import com.payv.ledger.infrastructure.persistence.mybatis.mapper.LedgerEventOutboxMapper;
import com.payv.ledger.infrastructure.persistence.mybatis.record.LedgerEventOutboxRecord;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class MyBatisTransactionChangedEventOutboxAdapter implements TransactionChangedEventOutboxPort {

    private final LedgerEventOutboxMapper ledgerEventOutboxMapper;

    @Value("${rabbitmq.exchange.ledger.events:payv.ledger.events}")
    private String exchange;

    @Value("${rabbitmq.routing-key.ledger-transaction-changed:ledger.transaction.changed}")
    private String routingKey;

    @Override
    public void enqueue(LedgerTransactionChangedEvent event) {
        if (event == null) {
            return;
        }

        OffsetDateTime now = OffsetDateTime.now();
        String outboxId = UUID.randomUUID().toString();

        ledgerEventOutboxMapper.insert(
                LedgerEventOutboxRecord.pending(
                        outboxId,
                        exchange,
                        routingKey,
                        event,
                        LedgerEventPayloadSerializer.serialize(event),
                        now
                )
        );
    }
}
