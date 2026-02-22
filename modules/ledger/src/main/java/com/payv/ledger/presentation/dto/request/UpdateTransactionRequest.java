package com.payv.ledger.presentation.dto.request;

import com.payv.ledger.application.command.model.UpdateTransactionCommand;
import com.payv.ledger.application.command.model.UpdateTransferCommand;
import com.payv.ledger.domain.model.Money;
import com.payv.ledger.domain.model.TransactionType;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;
import java.time.LocalDate;

@Data
public final class UpdateTransactionRequest {

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
    private Object tagIds;
    private String fromAssetId;
    private String toAssetId;

    public UpdateTransactionCommand toCommand() {
        UpdateTransactionCommand command = UpdateTransactionCommand.builder()
                .transactionType(TransactionType.valueOf(transactionType))
                .amount(Money.generate(amount))
                .transactionDate(transactionDate)
                .assetId(assetId)
                .categoryIdLevel1(categoryIdLevel1)
                .categoryIdLevel2(blankToNull(categoryIdLevel2))
                .memo(blankToNull(memo))
                .build();

        appendTagIds(command, tagIds);

        return command;
    }

    public boolean isTransferType() {
        return "TRANSFER".equalsIgnoreCase(transactionType);
    }

    public UpdateTransferCommand toTransferCommand() {
        return UpdateTransferCommand.builder()
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

    private static void appendTagIds(UpdateTransactionCommand command, Object rawTagIds) {
        if (rawTagIds == null) return;

        if (rawTagIds instanceof Iterable) {
            for (Object each : (Iterable<?>) rawTagIds) {
                appendSingleTagId(command, each);
            }
            return;
        }

        appendSingleTagId(command, rawTagIds);
    }

    private static void appendSingleTagId(UpdateTransactionCommand command, Object value) {
        if (value == null) return;
        String tagId = String.valueOf(value).trim();
        if (tagId.isEmpty()) return;
        command.addTagId(tagId);
    }
}
