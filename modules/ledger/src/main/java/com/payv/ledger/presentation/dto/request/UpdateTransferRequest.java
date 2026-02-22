package com.payv.ledger.presentation.dto.request;

import com.payv.ledger.application.command.model.UpdateTransferCommand;
import com.payv.ledger.domain.model.Money;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.time.LocalDate;

@Data
public final class UpdateTransferRequest {

    @NotBlank
    private String fromAssetId;

    @NotBlank
    private String toAssetId;

    @Positive
    private long amount;

    @NotNull
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate transferDate;

    private String memo;

    public UpdateTransferCommand toCommand() {
        return UpdateTransferCommand.builder()
                .fromAssetId(fromAssetId)
                .toAssetId(toAssetId)
                .amount(Money.generate(amount))
                .transferDate(transferDate)
                .memo(blankToNull(memo))
                .build();
    }

    private static String blankToNull(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
