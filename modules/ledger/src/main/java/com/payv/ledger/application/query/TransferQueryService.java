package com.payv.ledger.application.query;

import com.payv.common.application.query.PageRequest;
import com.payv.common.application.query.PagedResult;
import com.payv.contracts.common.dto.IdNamePublicDto;
import com.payv.ledger.application.port.AssetQueryPort;
import com.payv.ledger.application.exception.TransferNotFoundException;
import com.payv.ledger.infrastructure.persistence.mybatis.mapper.TransferMapper;
import com.payv.ledger.infrastructure.persistence.mybatis.record.TransferRecord;
import com.payv.ledger.presentation.dto.viewmodel.TransferDetailView;
import com.payv.ledger.presentation.dto.viewmodel.TransferSummaryView;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
/**
 * Ledger BC의 이체 조회 서비스.
 *
 * What:
 * - 이체 목록/상세 조회를 제공한다.
 *
 * Why:
 * - 이체 데이터에 자산명(출금/입금)을 결합한 읽기 모델을 제공해
 *   화면에서 별도 조회를 최소화하기 위함이다.
 */
public class TransferQueryService {

    private final TransferMapper transferMapper;
    private final AssetQueryPort assetQueryPort;

    /**
     * 이체 목록을 페이지 단위로 조회한다.
     *
     * @param ownerUserId 소유 사용자 ID
     * @param from 시작 이체일(포함)
     * @param to 종료 이체일(포함)
     * @param page 페이지 번호(1-base)
     * @param size 페이지 크기
     * @return 페이징된 이체 요약 목록
     */
    public PagedResult<TransferSummaryView> list(String ownerUserId,
                                                 LocalDate from,
                                                 LocalDate to,
                                                 int page,
                                                 int size) {
        PageRequest pageRequest = PageRequest.of(page, size);

        List<TransferRecord> rows = transferMapper.selectList(
                ownerUserId, from, to, pageRequest.getOffset(), pageRequest.getSize()
        );
        int total = transferMapper.countList(ownerUserId, from, to);

        Set<String> assetIds = new HashSet<>();
        for (TransferRecord row : rows) {
            if (row.getFromAssetId() != null) assetIds.add(row.getFromAssetId());
            if (row.getToAssetId() != null) assetIds.add(row.getToAssetId());
        }

        Map<String, String> assetNames = assetIds.isEmpty()
                ? Collections.emptyMap()
                : toNameMap(assetQueryPort.getAssetNames(assetIds, ownerUserId));

        List<TransferSummaryView> items = new ArrayList<>(rows.size());
        for (TransferRecord row : rows) {
            items.add(new TransferSummaryView(
                    row.getTransferId(),
                    row.getAmount(),
                    row.getTransferDate(),
                    row.getFromAssetId(),
                    assetNames.get(row.getFromAssetId()),
                    row.getToAssetId(),
                    assetNames.get(row.getToAssetId()),
                    row.getMemo()
            ));
        }

        return new PagedResult<>(items, total, pageRequest.getPage(), pageRequest.getSize());
    }

    /**
     * 이체 상세를 조회한다.
     *
     * @param transferId 이체 ID
     * @param ownerUserId 소유 사용자 ID
     * @return 이체 상세 뷰
     * @throws TransferNotFoundException 대상 이체를 찾지 못한 경우
     */
    public TransferDetailView detail(String transferId, String ownerUserId) {
        TransferRecord row = transferMapper.selectDetail(transferId, ownerUserId);
        if (row == null) throw new TransferNotFoundException();

        Set<String> assetIds = new HashSet<>();
        if (row.getFromAssetId() != null) assetIds.add(row.getFromAssetId());
        if (row.getToAssetId() != null) assetIds.add(row.getToAssetId());

        Map<String, String> assetNames = assetIds.isEmpty()
                ? Collections.emptyMap()
                : toNameMap(assetQueryPort.getAssetNames(assetIds, ownerUserId));

        return new TransferDetailView(
                row.getTransferId(),
                row.getAmount(),
                row.getTransferDate(),
                row.getFromAssetId(),
                assetNames.get(row.getFromAssetId()),
                row.getToAssetId(),
                assetNames.get(row.getToAssetId()),
                row.getMemo()
        );
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
