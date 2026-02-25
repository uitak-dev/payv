package com.payv.ledger.presentation.dto.viewmodel;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
public class TransactionDetailView {

    private final String transactionId;
    private final String transactionType;
    private final long amount;
    private final LocalDate transactionDate;

    private final String assetId;
    private final String assetName;

    private final String memo;

    private final String categoryIdLevel1;
    private final String categoryNameLevel1;
    private final String categoryIdLevel2;
    private final String categoryNameLevel2;
    private final String sourceType;
    private final String sourceDisplayName;
    private final String fixedCostTemplateId;

    private final List<TagView> tags;
    private final List<AttachmentView> attachments;

    @Data
    @AllArgsConstructor
    public static class TagView {
        private final String tagId;
        private final String tagName;
    }

    @Data
    @AllArgsConstructor
    public static class AttachmentView {
        private final String attachmentId;
        private final String uploadFileName;
        private final String status;
    }
}
