package com.payv.ledger.application.query;

import com.payv.ledger.application.port.AssetQueryPort;
import com.payv.ledger.application.port.ClassificationQueryPort;
import com.payv.ledger.domain.model.Attachment;
import com.payv.ledger.domain.model.Transaction;
import com.payv.ledger.domain.model.TransactionId;
import com.payv.ledger.domain.repository.AttachmentRepository;
import com.payv.ledger.domain.repository.TransactionRepository;
import com.payv.ledger.infrastructure.persistence.mybatis.mapper.TransactionMapper;
import com.payv.ledger.infrastructure.persistence.mybatis.record.TransactionRecord;
import com.payv.ledger.presentation.dto.viewmodel.TransactionDetailView;
import com.payv.ledger.presentation.dto.viewmodel.TransactionSummaryView;
import lombok.AllArgsConstructor;
import lombok.Data;
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

        int safePage = Math.max(page, 1);
        int safeSize = Math.min(Math.max(size, 10), 100);
        int offset = (safePage - 1) * safeSize;

        List<TransactionRecord> rows = txMapper.selectList(ownerUserId, from, to, assetId, offset, safeSize);
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
                    r.getMemo()
            ));
        }

        return new PagedResult<>(items, total, safePage, safeSize);
    }

    @Transactional(readOnly = true)
    public TransactionDetailView detail(TransactionId transactionId, String ownerUserId) {

        TransactionRecord tr = txMapper.selectDetail(transactionId.getValue(), ownerUserId);
        if (tr == null) throw new NoSuchElementException("transaction not found");

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
                tags,
                attachments
        );
    }

    @Data
    @AllArgsConstructor
    public static class PagedResult<T> {
        private final List<T> items;
        private final int total;
        private final int page;
        private final int size;
    }
}
