package com.payv.ledger.application.query;

import com.payv.common.application.query.PageRequest;
import com.payv.common.application.query.PagedResult;
import com.payv.ledger.application.port.AssetQueryPort;
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
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TransferQueryService {

    private final TransferMapper transferMapper;
    private final AssetQueryPort assetQueryPort;

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
                : assetQueryPort.getAssetNames(assetIds, ownerUserId);

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

    public TransferDetailView detail(String transferId, String ownerUserId) {
        TransferRecord row = transferMapper.selectDetail(transferId, ownerUserId);
        if (row == null) throw new NoSuchElementException("transfer not found");

        Set<String> assetIds = new HashSet<>();
        if (row.getFromAssetId() != null) assetIds.add(row.getFromAssetId());
        if (row.getToAssetId() != null) assetIds.add(row.getToAssetId());

        Map<String, String> assetNames = assetIds.isEmpty()
                ? Collections.emptyMap()
                : assetQueryPort.getAssetNames(assetIds, ownerUserId);

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
}
