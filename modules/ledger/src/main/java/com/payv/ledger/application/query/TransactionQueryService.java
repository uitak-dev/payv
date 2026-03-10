package com.payv.ledger.application.query;

import com.payv.common.application.query.PageRequest;
import com.payv.common.application.query.PagedResult;
import com.payv.common.cache.CacheNames;
import com.payv.contracts.common.dto.IdNamePublicDto;
import com.payv.ledger.application.port.AssetQueryPort;
import com.payv.ledger.application.port.ClassificationQueryPort;
import com.payv.ledger.application.exception.TransactionNotFoundException;
import com.payv.ledger.domain.model.Attachment;
import com.payv.ledger.domain.model.AttachmentId;
import com.payv.ledger.domain.model.TransactionId;
import com.payv.ledger.domain.model.TransactionSourceType;
import com.payv.ledger.domain.repository.AttachmentRepository;
import com.payv.ledger.infrastructure.persistence.mybatis.mapper.TransactionMapper;
import com.payv.ledger.infrastructure.persistence.mybatis.record.TransactionRecord;
import com.payv.ledger.presentation.dto.viewmodel.TransactionDetailView;
import com.payv.ledger.presentation.dto.viewmodel.TransactionSummaryView;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
/**
 * Ledger BC의 거래 조회 서비스.
 * - 거래 목록/상세/첨부파일 조회와 합계성 조회를 제공한다.
 * - 거래 원본 레코드에 자산/분류명을 조합한 읽기 모델을 반환해,
 *   화면 계층에서 중복 조인 로직을 줄이고 조회 성능 최적화(캐시) 적용.
 */
public class TransactionQueryService {

    private final TransactionMapper txMapper;
    private final AttachmentRepository attachmentRepository;

    private final ClassificationQueryPort classificationQueryPort;
    private final AssetQueryPort assetQueryPort;

    /**
     * 거래 목록을 페이지 단위로 조회한다.
     *
     * Business logic:
     * - 1페이지 + 자산 필터 미사용 조건에서 최근 거래 캐시를 사용한다.
     * - 자산명/카테고리명은 ACL 포트를 통해 이름을 매핑한다.
     *
     * @param ownerUserId 소유 사용자 ID
     * @param from 시작 거래일(포함)
     * @param to 종료 거래일(포함)
     * @param assetId 자산 필터(없으면 전체)
     * @param page 페이지 번호(1-base)
     * @param size 페이지 크기
     * @return 페이징된 거래 요약 목록
     */
    @Cacheable(
            cacheNames = CacheNames.LEDGER_RECENT_FIRST_PAGE,
            key = "T(com.payv.common.cache.CacheKeys).ledgerRecentFirstPageKey(#ownerUserId, #from, #to, #size)",
            condition = "#page <= 1 && (#assetId == null || #assetId.trim().isEmpty())"
    )
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
                : toNameMap(assetQueryPort.getAssetNames(assetIds, ownerUserId));

        Map<String, String> categoryNames = catIds.isEmpty()
                ? Collections.emptyMap()
                : toNameMap(classificationQueryPort.getCategoryNames(catIds, ownerUserId));

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

    /**
     * 거래 상세를 조회한다.
     *
     * @param transactionId 거래 ID
     * @param ownerUserId 소유 사용자 ID
     * @return 거래 상세 뷰(태그/첨부파일/분류명 포함)
     * @throws TransactionNotFoundException 거래를 찾지 못한 경우
     */
    @Transactional(readOnly = true)
    public TransactionDetailView detail(TransactionId transactionId, String ownerUserId) {

        TransactionRecord tr = txMapper.selectDetail(transactionId.getValue(), ownerUserId);
        if (tr == null) throw new TransactionNotFoundException();

        // 1) 태그 목록 구성
        List<String> tagIds = txMapper.selectTagIds(transactionId.getValue(), ownerUserId);
        Map<String, String> tagNames = (tagIds == null || tagIds.isEmpty())
                ? Collections.emptyMap()
                : toNameMap(classificationQueryPort.getTagNames(tagIds, ownerUserId));

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
                toNameMap(assetQueryPort.getAssetNames(Collections.singleton(tr.getAssetId()), ownerUserId));
        String assetName = assetNameMap.get(tr.getAssetId());

        // 4) 카테고리(L1/L2) 구성
        Set<String> cats = new HashSet<>();
        if (tr.getCategoryIdLevel1() != null) cats.add(tr.getCategoryIdLevel1());
        if (tr.getCategoryIdLevel2() != null) cats.add(tr.getCategoryIdLevel2());
        Map<String, String> categoryNames = cats.isEmpty()
                ? Collections.emptyMap()
                : toNameMap(classificationQueryPort.getCategoryNames(cats, ownerUserId));

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

    /**
     * 특정 거래에 속한 STORED 상태 첨부파일을 조회한다.
     *
     * @param transactionId 거래 ID
     * @param attachmentId 첨부파일 ID
     * @param ownerUserId 소유 사용자 ID
     * @return 첨부파일. 없거나 조건 불일치면 {@link Optional#empty()}
     */
    @Transactional(readOnly = true)
    public Optional<Attachment> findStoredAttachment(TransactionId transactionId, AttachmentId attachmentId, String ownerUserId) {
        return attachmentRepository.findById(attachmentId, ownerUserId)
                .filter(attachment -> attachment.getTransactionId().equals(transactionId))
                .filter(attachment -> attachment.getStatus() == Attachment.Status.STORED);
    }

    /**
     * 조건 범위 내 지출 합계를 계산한다.
     *
     * @param ownerUserId 소유 사용자 ID
     * @param from 시작 거래일(포함)
     * @param to 종료 거래일(포함)
     * @param categoryIdLevel1 1-depth 카테고리 ID(선택)
     * @param categoryIdLevel2 2-depth 카테고리 ID(선택)
     * @return 지출 합계(없으면 0)
     */
    @Transactional(readOnly = true)
    public long sumExpenseAmount(String ownerUserId, LocalDate from, LocalDate to,
                                 String categoryIdLevel1, String categoryIdLevel2) {
        Long value = txMapper.sumExpenseAmount(ownerUserId, from, to, categoryIdLevel1, categoryIdLevel2);
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

    private Map<String, String> toNameMap(List<IdNamePublicDto> rows) {
        if (rows == null || rows.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, String> result = new LinkedHashMap<>();
        for (IdNamePublicDto row : rows) {
            result.put(row.getId(), row.getName());
        }
        return result;
    }
}
