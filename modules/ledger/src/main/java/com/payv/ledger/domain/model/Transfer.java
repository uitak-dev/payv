package com.payv.ledger.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public final class Transfer {

    private final TransferId id;
    private final String ownerUserId;

    private String fromAssetId;
    private String toAssetId;
    private Money amount;
    private LocalDate transferDate;
    private String memo;

    @Builder
    private Transfer(TransferId id, String ownerUserId,
                     String fromAssetId, String toAssetId,
                     Money amount, LocalDate transferDate, String memo) {

        validateAssetPair(fromAssetId, toAssetId);
        this.id = id;
        this.ownerUserId = ownerUserId;
        this.fromAssetId = fromAssetId;
        this.toAssetId = toAssetId;
        this.amount = amount;
        this.transferDate = transferDate;
        this.memo = memo;
    }

    public static Transfer create(String ownerUserId, String fromAssetId,
                                  String toAssetId, Money amount,
                                  LocalDate transferDate, String memo) {
        return Transfer.builder()
                .id(TransferId.generate())
                .ownerUserId(ownerUserId)
                .fromAssetId(fromAssetId)
                .toAssetId(toAssetId)
                .amount(amount)
                .transferDate(transferDate)
                .memo(memo)
                .build();
    }

    public static Transfer of(TransferId id, String ownerUserId,
                              String fromAssetId, String toAssetId,
                              Money amount, LocalDate transferDate, String memo) {
        return Transfer.builder()
                .id(id)
                .ownerUserId(ownerUserId)
                .fromAssetId(fromAssetId)
                .toAssetId(toAssetId)
                .amount(amount)
                .transferDate(transferDate)
                .memo(memo)
                .build();
    }

    public void update(String fromAssetId,
                             String toAssetId,
                             Money amount,
                             LocalDate transferDate) {
        validateAssetPair(fromAssetId, toAssetId);
        this.fromAssetId = fromAssetId;
        this.toAssetId = toAssetId;
        this.amount = amount;
        this.transferDate = transferDate;
    }

    public void updateMemo(String memo) {
        this.memo = memo;
    }

    private static void validateAssetPair(String fromAssetId, String toAssetId) {
        if (fromAssetId == null || fromAssetId.trim().isEmpty())
            throw new IllegalArgumentException("fromAssetId must not be blank");
        if (toAssetId == null || toAssetId.trim().isEmpty())
            throw new IllegalArgumentException("toAssetId must not be blank");
        if (fromAssetId.equals(toAssetId))
            throw new IllegalArgumentException("fromAssetId and toAssetId must be different");
    }
}
