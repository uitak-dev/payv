package com.payv.ledger.infrastructure.persistence.mybatis.record;

import com.payv.common.event.ledger.LedgerTransactionChangedEvent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LedgerEventOutboxRecord {

    private String outboxId;
    private String eventType;
    private String ownerUserId;
    private String exchangeName;
    private String routingKey;
    private byte[] payload;
    private String status;
    private int retryCount;
    private OffsetDateTime nextRetryAt;
    private OffsetDateTime publishedAt;
    private String lastError;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public static LedgerEventOutboxRecord pending(String outboxId,
                                                  String exchangeName,
                                                  String routingKey,
                                                  LedgerTransactionChangedEvent event,
                                                  byte[] payload,
                                                  OffsetDateTime now) {
        return LedgerEventOutboxRecord.builder()
                .outboxId(outboxId)
                .eventType("LEDGER_TRANSACTION_CHANGED")
                .ownerUserId(event.getOwnerUserId())
                .exchangeName(exchangeName)
                .routingKey(routingKey)
                .payload(payload)
                .status("PENDING")
                .retryCount(0)
                .nextRetryAt(now)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }
}
