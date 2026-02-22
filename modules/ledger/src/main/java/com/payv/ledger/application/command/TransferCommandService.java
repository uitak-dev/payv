package com.payv.ledger.application.command;

import com.payv.ledger.application.command.model.CreateTransferCommand;
import com.payv.ledger.application.command.model.UpdateTransferCommand;
import com.payv.ledger.application.port.AssetValidationPort;
import com.payv.ledger.domain.model.Transfer;
import com.payv.ledger.domain.model.TransferId;
import com.payv.ledger.domain.repository.TransferRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TransferCommandService {

    private final TransferRepository transferRepository;
    private final AssetValidationPort assetValidationPort;

    @Transactional
    public TransferId create(CreateTransferCommand command, String ownerUserId) {
        validateAssetPair(command.getFromAssetId(), command.getToAssetId(), ownerUserId);

        Transfer transfer = Transfer.create(
                ownerUserId,
                command.getFromAssetId(),
                command.getToAssetId(),
                command.getAmount(),
                command.getTransferDate(),
                command.getMemo()
        );

        transferRepository.save(transfer);
        return transfer.getId();
    }

    @Transactional
    public void update(TransferId transferId, UpdateTransferCommand command, String ownerUserId) {
        validateAssetPair(command.getFromAssetId(), command.getToAssetId(), ownerUserId);

        Transfer transfer = transferRepository.findById(transferId, ownerUserId)
                .orElseThrow(() -> new IllegalStateException("transfer not found"));

        transfer.update(
                command.getFromAssetId(),
                command.getToAssetId(),
                command.getAmount(),
                command.getTransferDate()
        );
        transfer.updateMemo(command.getMemo());

        transferRepository.save(transfer);
    }

    @Transactional
    public void delete(TransferId transferId, String ownerUserId) {
        Transfer transfer = transferRepository.findById(transferId, ownerUserId)
                .orElseThrow(() -> new IllegalStateException("transfer not found"));
        transferRepository.deleteById(transfer.getId(), ownerUserId);
    }

    private void validateAssetPair(String fromAssetId, String toAssetId, String ownerUserId) {
        if (fromAssetId == null || fromAssetId.trim().isEmpty()) {
            throw new IllegalArgumentException("fromAssetId must not be blank");
        }
        if (toAssetId == null || toAssetId.trim().isEmpty()) {
            throw new IllegalArgumentException("toAssetId must not be blank");
        }
        if (fromAssetId.equals(toAssetId)) {
            throw new IllegalArgumentException("fromAssetId and toAssetId must be different");
        }

        assetValidationPort.validateAssertId(fromAssetId, ownerUserId);
        assetValidationPort.validateAssertId(toAssetId, ownerUserId);
    }
}
