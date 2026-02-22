package com.payv.ledger.infrastructure.persistence.mybatis;

import com.payv.ledger.domain.model.Transfer;
import com.payv.ledger.domain.model.TransferId;
import com.payv.ledger.domain.repository.TransferRepository;
import com.payv.ledger.infrastructure.persistence.mybatis.mapper.TransferMapper;
import com.payv.ledger.infrastructure.persistence.mybatis.record.TransferRecord;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MyBatisTransferRepository implements TransferRepository {

    private final TransferMapper transferMapper;

    @Override
    public void save(Transfer transfer) {
        transferMapper.upsert(TransferRecord.toRecord(transfer));
    }

    @Override
    public Optional<Transfer> findById(TransferId transferId, String ownerUserId) {
        TransferRecord record = transferMapper.selectDetail(transferId.getValue(), ownerUserId);
        if (record == null) return Optional.empty();
        return Optional.of(record.toEntity());
    }

    @Override
    public void deleteById(TransferId transferId, String ownerUserId) {
        transferMapper.deleteByIdAndOwner(transferId.getValue(), ownerUserId);
    }
}
