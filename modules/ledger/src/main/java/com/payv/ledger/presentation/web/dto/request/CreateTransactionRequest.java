package com.payv.ledger.presentation.web.dto.request;

import com.sun.istack.internal.NotNull;
import lombok.*;

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
    private LocalDate transactionDate;

    @NotBlank
    private String assetId;

    @NotBlank
    private String categoryIdLevel1;

    private String categoryIdLevel2;
    private String memo;
    private List<String> tagIds;
}
