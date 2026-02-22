package com.payv.ledger.application.command.model;

import com.payv.ledger.domain.model.Money;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
public final class CreateTransferCommand {

    private String fromAssetId;
    private String toAssetId;
    private Money amount;
    private LocalDate transferDate;
    private String memo;

    @Builder
    public CreateTransferCommand(String fromAssetId,
                                 String toAssetId,
                                 Money amount,
                                 LocalDate transferDate,
                                 String memo) {
        this.fromAssetId = fromAssetId;
        this.toAssetId = toAssetId;
        this.amount = amount;
        this.transferDate = transferDate;
        this.memo = memo;
    }
}
