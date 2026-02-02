package com.payv.ledger.application.command.model;

import com.payv.ledger.domain.model.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Data
public final class CreateTransactionCommand {

    private TransactionType transactionType;
    private Money amount;
    private LocalDate transactionDate;
    private String assetId;

    private String categoryIdLevel1;
    private String categoryIdLevel2;     // nullable
    private String memo;

    private final LinkedHashSet<String> tagIds = new LinkedHashSet<>();

    @Builder
    public CreateTransactionCommand(TransactionType transactionType, Money amount,
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
}
