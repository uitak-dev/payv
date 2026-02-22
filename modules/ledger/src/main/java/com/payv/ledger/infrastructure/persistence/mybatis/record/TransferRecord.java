package com.payv.ledger.infrastructure.persistence.mybatis.record;

import com.payv.ledger.domain.model.Money;
import com.payv.ledger.domain.model.Transfer;
import com.payv.ledger.domain.model.TransferId;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public final class TransferRecord {

    private String transferId;
    private String ownerUserId;
    private String fromAssetId;
    private String toAssetId;
    private long amount;
    private LocalDate transferDate;
    private String memo;

    @Builder
    private TransferRecord(String transferId,
                           String ownerUserId,
                           String fromAssetId,
                           String toAssetId,
                           long amount,
                           LocalDate transferDate,
                           String memo) {
        this.transferId = transferId;
        this.ownerUserId = ownerUserId;
        this.fromAssetId = fromAssetId;
        this.toAssetId = toAssetId;
        this.amount = amount;
        this.transferDate = transferDate;
        this.memo = memo;
    }

    public static TransferRecord toRecord(Transfer transfer) {
        return TransferRecord.builder()
                .transferId(transfer.getId().getValue())
                .ownerUserId(transfer.getOwnerUserId())
                .fromAssetId(transfer.getFromAssetId())
                .toAssetId(transfer.getToAssetId())
                .amount(transfer.getAmount().getAmount())
                .transferDate(transfer.getTransferDate())
                .memo(transfer.getMemo())
                .build();
    }

    public Transfer toEntity() {
        return Transfer.of(
                TransferId.of(transferId),
                ownerUserId,
                fromAssetId,
                toAssetId,
                Money.generate(amount),
                transferDate,
                memo
        );
    }
}
