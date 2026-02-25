package com.payv.ledger.application.query;

import com.payv.common.application.query.PageRequest;
import com.payv.common.application.query.PagedResult;
import com.payv.ledger.application.port.AssetQueryPort;
import com.payv.ledger.application.port.ClassificationQueryPort;
import com.payv.ledger.application.exception.TransactionNotFoundException;
import com.payv.ledger.domain.model.Attachment;
import com.payv.ledger.domain.model.AttachmentId;
import com.payv.ledger.domain.model.Transaction;
import com.payv.ledger.domain.model.TransactionId;
import com.payv.ledger.domain.model.TransactionSourceType;
import com.payv.ledger.domain.repository.AttachmentRepository;
import com.payv.ledger.domain.repository.TransactionRepository;
import com.payv.ledger.infrastructure.persistence.mybatis.mapper.TransactionMapper;
import com.payv.ledger.infrastructure.persistence.mybatis.record.TransactionRecord;
import com.payv.ledger.presentation.dto.viewmodel.TransactionDetailView;
import com.payv.ledger.presentation.dto.viewmodel.TransactionSummaryView;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TransactionQueryService {

    private final TransactionMapper txMapper;
    private final AttachmentRepository attachmentRepository;

    private final ClassificationQueryPort classificationQueryPort;
    private final AssetQueryPort assetQueryPort;

    @Transactional(readOnly = true)
    public PagedResult<TransactionSummaryView> list(String ownerUserId,
                                                    LocalDate from, LocalDate to,
                                                    String assetId,
                                                    int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size);

        List<TransactionRecord> rows = txMapper.selectList(
                ownerUserId, from, to, assetId, pageRequest.getOffset(), pageRequest.getSize()
        );
        int total = txMapper.countList(ownerUserId, from, to, assetId);

        Set<String> assetIds = new HashSet<String>();
        Set<String> catIds = new HashSet<String>();
        for (TransactionRecord r : rows) {
            if (r.getAssetId() != null) assetIds.add(r.getAssetId());
            if (r.getCategoryIdLevel1() != null) catIds.add(r.getCategoryIdLevel1());
        }

        Map<String, String> assetNames = assetIds.isEmpty()
                ? Collections.emptyMap()
                : assetQueryPort.getAssetNames(assetIds, ownerUserId);

        Map<String, String> categoryNames = catIds.isEmpty()
                ? Collections.emptyMap()
                : classificationQueryPort.getCategoryNames(catIds, ownerUserId);

        List<TransactionSummaryView> items = new ArrayList<>(rows.size());
        for (TransactionRecord r : rows) {
            items.add(new TransactionSummaryView(
                    r.getTransactionId(),
                    r.getTransactionType(),
                    r.getAmount(),
                    r.getTransactionDate(),
                    r.getAssetId(),
                    assetNames.get(r.getAssetId()),
                    r.getCategoryIdLevel1(),
                    categoryNames.get(r.getCategoryIdLevel1()),
                    r.getMemo(),
                    r.getSourceType(),
                    toSourceDisplayName(r.getSourceType())
            ));
        }

        return new PagedResult<>(items, total, pageRequest.getPage(), pageRequest.getSize());
    }

    @Transactional(readOnly = true)
    public TransactionDetailView detail(TransactionId transactionId, String ownerUserId) {

        TransactionRecord tr = txMapper.selectDetail(transactionId.getValue(), ownerUserId);
        if (tr == null) throw new TransactionNotFoundException();

        // 1) 태그 목록 구성
        List<String> tagIds = txMapper.selectTagIds(transactionId.getValue(), ownerUserId);
        Map<String, String> tagNames = (tagIds == null || tagIds.isEmpty())
                ? Collections.emptyMap()
                : classificationQueryPort.getTagNames(tagIds, ownerUserId);

        List<TransactionDetailView.TagView> tags = new ArrayList<>();
        if (tagIds != null) {
            for (String tagId : tagIds) {
                tags.add(new TransactionDetailView.TagView(tagId, tagNames.get(tagId)));
            }
        }

        // 2) 첨부파일 목록 구성
        List<Attachment> atts = attachmentRepository.findStoredByTransactionId(transactionId, ownerUserId);
        List<TransactionDetailView.AttachmentView> attachments = new ArrayList<>(atts.size());
        for (Attachment a : atts) {
            attachments.add(new TransactionDetailView.AttachmentView(
                    a.getId().getValue(),
                    a.getUploadFileName(),
                    a.getStatus().name()
            ));
        }

        // 3) 자산(출처) 구성
        Map<String, String> assetNameMap =
                assetQueryPort.getAssetNames(Collections.singleton(tr.getAssetId()), ownerUserId);
        String assetName = assetNameMap.get(tr.getAssetId());

        // 4) 카테고리(L1/L2) 구성
        Set<String> cats = new HashSet<>();
        if (tr.getCategoryIdLevel1() != null) cats.add(tr.getCategoryIdLevel1());
        if (tr.getCategoryIdLevel2() != null) cats.add(tr.getCategoryIdLevel2());
        Map<String, String> categoryNames = cats.isEmpty()
                ? Collections.emptyMap()
                : classificationQueryPort.getCategoryNames(cats, ownerUserId);

        return new TransactionDetailView(
                tr.getTransactionId(),
                tr.getTransactionType(),
                tr.getAmount(),
                tr.getTransactionDate(),
                tr.getAssetId(),
                assetName,
                tr.getMemo(),
                tr.getCategoryIdLevel1(),
                categoryNames.get(tr.getCategoryIdLevel1()),
                tr.getCategoryIdLevel2(),
                categoryNames.get(tr.getCategoryIdLevel2()),
                tr.getSourceType(),
                toSourceDisplayName(tr.getSourceType()),
                tr.getFixedCostTemplateId(),
                tags,
                attachments
        );
    }

    @Transactional(readOnly = true)
    public Optional<Attachment> findStoredAttachment(TransactionId transactionId, AttachmentId attachmentId, String ownerUserId) {
        return attachmentRepository.findById(attachmentId, ownerUserId)
                .filter(attachment -> attachment.getTransactionId().equals(transactionId))
                .filter(attachment -> attachment.getStatus() == Attachment.Status.STORED);
    }

    @Transactional(readOnly = true)
    public long sumExpenseAmount(String ownerUserId, LocalDate from, LocalDate to,
                                 String categoryIdLevel1, String categoryIdLevel2) {
        Long value = txMapper.sumExpenseAmount(ownerUserId, from, to, categoryIdLevel1, categoryIdLevel2);
        return value == null ? 0L : value;
    }

    @Transactional(readOnly = true)
    public long sumAmountByType(String ownerUserId, LocalDate from, LocalDate to, String transactionType) {
        Long value = txMapper.sumAmountByType(ownerUserId, from, to, transactionType);
        return value == null ? 0L : value;
    }

    private String toSourceDisplayName(String sourceType) {
        if (sourceType == null || sourceType.trim().isEmpty()) {
            return TransactionSourceType.MANUAL.getDisplayName();
        }
        try {
            return TransactionSourceType.valueOf(sourceType).getDisplayName();
        } catch (IllegalArgumentException e) {
            return sourceType;
        }
    }
}
