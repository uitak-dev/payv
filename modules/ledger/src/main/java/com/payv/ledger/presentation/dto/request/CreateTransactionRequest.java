package com.payv.ledger.presentation.dto.request;

import com.payv.ledger.application.command.model.CreateTransactionCommand;
import com.payv.ledger.application.command.model.CreateTransferCommand;
import com.payv.ledger.domain.model.Money;
import com.payv.ledger.domain.model.TransactionType;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;
import java.time.LocalDate;
import java.util.List;

@Data
public final class CreateTransactionRequest {

    @NotNull
    private String transactionType;

    @Positive
    private long amount;

    @NotNull
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate transactionDate;

    @NotBlank
    private String assetId;

    @NotBlank
    private String categoryIdLevel1;

    private String categoryIdLevel2;
    private String memo;
    private List<String> tagIds;
    private String fromAssetId;
    private String toAssetId;

    public CreateTransactionCommand toCommand() {
        CreateTransactionCommand command = CreateTransactionCommand.builder()
                .transactionType(TransactionType.valueOf(transactionType))
                .amount(Money.generate(amount))
                .transactionDate(transactionDate)
                .assetId(assetId)
                .categoryIdLevel1(categoryIdLevel1)
                .categoryIdLevel2(blankToNull(categoryIdLevel2))
                .memo(blankToNull(memo))
                .build();

        if (tagIds != null) {
            for (String tagId : tagIds) {
                if (tagId == null || tagId.trim().isEmpty()) {
                    continue;
                }
                command.addTagId(tagId);
            }
        }

        return command;
    }

    public boolean isTransferType() {
        return "TRANSFER".equalsIgnoreCase(transactionType);
    }

    public CreateTransferCommand toTransferCommand() {
        return CreateTransferCommand.builder()
                .fromAssetId(blankToNull(fromAssetId))
                .toAssetId(blankToNull(toAssetId))
                .amount(Money.generate(amount))
                .transferDate(transactionDate)
                .memo(blankToNull(memo))
                .build();
    }

    private static String blankToNull(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
