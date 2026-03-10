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
/**
 * 거래 변경 이벤트 Outbox 적재 어댑터(MyBatis 구현).
 * - 이벤트를 직렬화하여 Outbox 테이블에 PENDING 상태로 저장한다.
 * - 거래 트랜잭션 커밋 시점에 이벤트를 안전하게 보관하고,
 *   실제 브로커 전송은 별도 릴레이가 재시도 가능하게 처리한다.
 */
public class MyBatisTransactionChangedEventOutboxAdapter implements TransactionChangedEventOutboxPort {

    private final LedgerEventOutboxMapper ledgerEventOutboxMapper;

    @Value("${rabbitmq.exchange.ledger.events:payv.ledger.events}")
    private String exchange;

    @Value("${rabbitmq.routing-key.ledger-transaction-changed:ledger.transaction.changed}")
    private String routingKey;

    /**
     * 거래 변경 이벤트를 Outbox에 적재한다.
     *
     * @param event 거래 변경 이벤트
     */
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
