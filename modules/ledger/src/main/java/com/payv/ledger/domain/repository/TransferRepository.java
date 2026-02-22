package com.payv.ledger.domain.repository;

import com.payv.ledger.domain.model.Transfer;
import com.payv.ledger.domain.model.TransferId;

import java.util.Optional;

public interface TransferRepository {

    void save(Transfer transfer);

    Optional<Transfer> findById(TransferId transferId, String ownerUserId);

    void deleteById(TransferId transferId, String ownerUserId);
}
