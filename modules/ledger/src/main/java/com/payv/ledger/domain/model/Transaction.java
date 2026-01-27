package com.payv.ledger.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;

@Getter
public final class Transaction {

    private static final int MAX_TAGS = 3;
    private static final int MAX_ATTACHMENTS = 2;

    private final TransactionId id;
//    private final String ownerUserId;

//    private TransactionType transactionType;
//    private Money amount;
    private LocalDate transactionDate;
    private String memo;

    private String assetId;
    private String categoryIdLevel1;
    private String categoryIdLevel2; // nullable

//    private final LinkedHashSet<String> tagIds = new LinkedHashSet<>();
//    private final List<Attachment> attachments = new ArrayList<>();

//    private final TransactionSource transactionSource;

    @Builder
    private Transaction(TransactionId id, LocalDate transactionDate, String memo) {
        this.id = id;
        this.transactionDate = transactionDate;
        this.memo = memo;
    }

    public static Transaction of(LocalDate transactionDate, String memo) {
        return Transaction.builder()
                .id(TransactionId.of())
                .transactionDate(transactionDate)
                .memo(memo)
                .build();
    }

    public static Transaction of(TransactionId id, LocalDate transactionDate, String memo) {
        return Transaction.builder()
                .id(id)
                .transactionDate(transactionDate)
                .memo(memo)
                .build();
    }
}
