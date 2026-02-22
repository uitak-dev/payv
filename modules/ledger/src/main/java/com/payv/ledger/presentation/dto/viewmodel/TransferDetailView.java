package com.payv.ledger.presentation.dto.viewmodel;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class TransferDetailView {

    private final String transferId;
    private final long amount;
    private final LocalDate transferDate;

    private final String fromAssetId;
    private final String fromAssetName;

    private final String toAssetId;
    private final String toAssetName;

    private final String memo;
}
