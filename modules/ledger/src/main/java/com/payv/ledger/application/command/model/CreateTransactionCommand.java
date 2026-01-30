package com.payv.ledger.application.command.model;

import com.payv.ledger.domain.model.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

@Getter
@AllArgsConstructor
@Builder
public final class CreateTransactionCommand {

    private TransactionType transactionType;
    private Money amount;
    private LocalDate transactionDate;
    private String assetId;

    private String categoryIdLevel1;
    private String categoryIdLevel2;     // nullable
    private String memo;

    private final LinkedHashSet<String> tagIds = new LinkedHashSet<>();
    private final List<Attachment> attachments = new ArrayList<>();
}
