package com.payv.ledger.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.*;

@Getter
public final class Transaction {

    private static final int MAX_TAGS = 3;
    private static final int MAX_ATTACHMENTS = 2;

    private final TransactionId id;
    private final String ownerUserId;

    private TransactionType transactionType;
    private Money amount;
    private LocalDate transactionDate;
    private String assetId;

    private String memo;
    private String categoryIdLevel1;
    private String categoryIdLevel2;     // nullable

    private final LinkedHashSet<String> tagIds = new LinkedHashSet<>();
    private final List<Attachment> attachments = new ArrayList<>();

    private final TransactionSource transactionSource;

    @Builder
    private Transaction(TransactionId id, String ownerUserId, TransactionType transactionType,
                        Money amount, LocalDate transactionDate, String memo, String assetId,
                        String categoryIdLevel1, String categoryIdLevel2,
                        TransactionSource transactionSource) {

        this.id = id;
        this.ownerUserId = ownerUserId;
        this.transactionType = transactionType;
        this.amount = amount;
        this.transactionDate = transactionDate;
        this.memo = memo;
        this.assetId = assetId;
        this.categoryIdLevel1 = categoryIdLevel1;
        this.categoryIdLevel2 = categoryIdLevel2;
        this.transactionSource = transactionSource;
    }

    public static Transaction createManual(String ownerUserId, TransactionType type, Money amount,
                                 LocalDate date, String assetId, String categoryIdLevel1) {
        return Transaction.builder()
                .id(TransactionId.generate())
                .ownerUserId(ownerUserId)
                .transactionType(type)
                .amount(amount)
                .transactionDate(date)
                .assetId(assetId)
                .categoryIdLevel1(categoryIdLevel1)
                .transactionSource(TransactionSource.manual())
                .build();
    }

    public static Transaction createFixedCostAuto(String ownerUserId, String fixedCostTemplateId,
                                                  TransactionType type, Money amount, LocalDate date,
                                                  String assetId, String categoryIdLevel1) {
        return Transaction.builder()
                .id(TransactionId.generate())
                .ownerUserId(ownerUserId)
                .transactionSource(TransactionSource.fixedCost(fixedCostTemplateId))
                .transactionType(type)
                .amount(amount)
                .transactionDate(date)
                .assetId(assetId)
                .categoryIdLevel1(categoryIdLevel1)
                .build();
    }

    public static Transaction of(TransactionId id, String ownerUserId, TransactionType type, Money amount,
                                 LocalDate date, String memo, String assetId,
                                 String cat1, String cat2, TransactionSource source,
                                 List<String> tags, List<Attachment> attachments) {

        Transaction transaction = Transaction.builder()
                .id(id)
                .ownerUserId(ownerUserId)
                .transactionType(type)
                .amount(amount)
                .transactionDate(date)
                .memo(memo)
                .assetId(assetId)
                .categoryIdLevel1(cat1)
                .categoryIdLevel2(cat2)
                .transactionSource(source)
                .build();

        if (tags != null) {
            transaction.tagIds.addAll(tags);
        }
        if (attachments != null) {
            transaction.attachments.addAll(attachments);
        }

        return transaction;
    }

    public void updateMemo(String memo) {
        this.memo = memo;
    }

    public void updateCategorize(String level1, String level2) {
        if (level1 == null || level1.trim().isEmpty()) throw new IllegalArgumentException("categoryIdLevel1 required");
        this.categoryIdLevel1 = level1;
        this.categoryIdLevel2 = !(level2 == null || level2.trim().isEmpty()) ? level2 : null;
    }

    public boolean addTag(String tagId) {
        if (tagIds.size() >= MAX_TAGS) throw new IllegalStateException("tag limit exceeded");
        return tagIds.add(tagId);
    }

    public boolean removeTag(String tagId) {
        Objects.requireNonNull(tagId);
        return tagIds.remove(tagId);
    }

    public boolean addAttachment(Attachment attachment) {
        if (attachments.size() >= MAX_ATTACHMENTS)
            throw new IllegalStateException("attachment limit exceeded");
        return attachments.add(attachment);
    }

    public boolean removeAttachment(Attachment attachment) {
        Objects.requireNonNull(attachment);
        return attachments.remove(attachment);
    }

    public void updateTags(Set<String> newTagIds) {
        if (newTagIds != null && newTagIds.size() > MAX_TAGS) {
            throw new IllegalArgumentException("태그는 최대 " + MAX_TAGS + "개까지만 등록 가능합니다.");
        }
        this.tagIds.clear();
        if (newTagIds != null) {
            this.tagIds.addAll(newTagIds);
        }
    }

    public void updateAttachments(List<Attachment> newAttachments) {
        if (newAttachments != null && newAttachments.size() > MAX_ATTACHMENTS) {
            throw new IllegalArgumentException("첨부파일은 최대 " + MAX_ATTACHMENTS + "개까지만 등록 가능합니다.");
        }
        this.attachments.clear();
        if (newAttachments != null) {
            this.attachments.addAll(newAttachments);
        }
    }
}
