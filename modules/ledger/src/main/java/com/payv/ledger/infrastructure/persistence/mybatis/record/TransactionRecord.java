package com.payv.ledger.infrastructure.persistence.mybatis.record;

import com.payv.ledger.domain.model.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
public final class TransactionRecord {

    private String transactionId;
    private String ownerUserId;
    private String transactionType;
    private long amount;
    private LocalDate transactionDate;
    private String assetId;
    private String memo;
    private String categoryIdLevel1;
    private String categoryIdLevel2;
    private String sourceType;
    private String fixedCostTemplateId;

    @Builder
    private TransactionRecord(String transactionId, String ownerUserId, String transactionType,
                              long amount, LocalDate transactionDate, String assetId, String memo,
                              String categoryIdLevel1, String categoryIdLevel2,
                              String sourceType, String fixedCostTemplateId) {

        this.transactionId = transactionId;
        this.ownerUserId = ownerUserId;
        this.transactionType = transactionType;
        this.amount = amount;
        this.transactionDate = transactionDate;
        this.assetId = assetId;
        this.memo = memo;
        this.categoryIdLevel1 = categoryIdLevel1;
        this.categoryIdLevel2 = categoryIdLevel2;
        this.sourceType = sourceType;
        this.fixedCostTemplateId = fixedCostTemplateId;
    }

    public static TransactionRecord toRecord(Transaction tx) {
        String type = tx.getTransactionType().name();
        String sourceType = tx.getTransactionSource().getType().name();
        String originRef = tx.getTransactionSource().getOriginalReference();

        return new TransactionRecord(
                tx.getId().getValue(),
                tx.getOwnerUserId(),
                type,
                tx.getAmount().getAmount(),
                tx.getTransactionDate(),
                tx.getAssetId(),
                tx.getMemo(),
                tx.getCategoryIdLevel1(),
                tx.getCategoryIdLevel2(),
                sourceType,
                originRef
        );
    }

    public Transaction toEntity(List<String> tagIds, List<Attachment> attachments) {
        return Transaction.of(
                TransactionId.of(transactionId),
                ownerUserId,
                TransactionType.valueOf(transactionType),
                Money.generate(amount),
                transactionDate,
                memo,
                assetId,
                categoryIdLevel1,
                categoryIdLevel2,
                toSource(),
                tagIds,
                attachments
        );
    }

    private TransactionSource toSource() {
        TransactionSourceType t = TransactionSourceType.valueOf(sourceType);
        return (t == TransactionSourceType.FIXED_COST_AUTO)
                ? TransactionSource.fixedCost(fixedCostTemplateId)
                : TransactionSource.manual();
    }
}
