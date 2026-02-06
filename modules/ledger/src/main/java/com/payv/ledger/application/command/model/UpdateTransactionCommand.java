package com.payv.ledger.application.command.model;

import com.payv.ledger.domain.model.Money;
import com.payv.ledger.domain.model.TransactionType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.LinkedHashSet;

@Data
public final class UpdateTransactionCommand {

    private TransactionType transactionType;
    private Money amount;
    private LocalDate transactionDate;
    private String assetId;

    private String categoryIdLevel1;
    private String categoryIdLevel2;     // nullable
    private String memo;

    private final LinkedHashSet<String> tagIds = new LinkedHashSet<>();
    private boolean tagIdsProvided;

    @Builder
    public UpdateTransactionCommand(TransactionType transactionType, Money amount,
                                    LocalDate transactionDate, String assetId,
                                    String categoryIdLevel1, String categoryIdLevel2, String memo) {
        this.transactionType = transactionType;
        this.amount = amount;
        this.transactionDate = transactionDate;
        this.assetId = assetId;
        this.categoryIdLevel1 = categoryIdLevel1;
        this.categoryIdLevel2 = categoryIdLevel2;
        this.memo = memo;
    }

    public void addTagId(String tagId) {
        tagIds.add(tagId);
    }

    public void markTagIdsProvided() {
        this.tagIdsProvided = true;
    }
}
