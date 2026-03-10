package com.payv.ledger.application.command;

import com.payv.ledger.application.command.model.CreateTransferCommand;
import com.payv.ledger.application.command.model.UpdateTransferCommand;
import com.payv.ledger.application.exception.InvalidTransferAssetPairException;
import com.payv.ledger.application.exception.TransferNotFoundException;
import com.payv.ledger.application.port.AssetValidationPort;
import com.payv.ledger.domain.model.Transfer;
import com.payv.ledger.domain.model.TransferId;
import com.payv.ledger.domain.repository.TransferRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;

@Service
@RequiredArgsConstructor
/**
 * 이체(Transfer) 명령을 처리하는 서비스.
 * - 이체 생성/수정/삭제를 수행한다.
 * - 출금/입금 자산 쌍 검증(동일 자산 금지, 자산 존재 검증)을 거래 저장 전에 보장해 데이터 무결성을 유지한다.
 */
public class TransferCommandService {

    private final TransferRepository transferRepository;
    private final AssetValidationPort assetValidationPort;

    /**
     * 이체를 생성한다.
     *
     * @param command 생성 요청(출금 자산, 입금 자산, 금액, 이체일, 메모)
     * @param ownerUserId 소유 사용자 ID
     * @return 생성된 이체 ID
     * @throws InvalidTransferAssetPairException 자산 쌍이 유효하지 않은 경우
     */
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

    /**
     * 기존 이체를 수정한다.
     *
     * @param transferId 수정 대상 이체 ID
     * @param command 수정 요청(자산 쌍/금액/일자/메모)
     * @param ownerUserId 소유 사용자 ID
     * @throws InvalidTransferAssetPairException 자산 쌍이 유효하지 않은 경우
     * @throws TransferNotFoundException 대상 이체를 찾지 못한 경우
     */
    @Transactional
    public void update(TransferId transferId, UpdateTransferCommand command, String ownerUserId) {
        validateAssetPair(command.getFromAssetId(), command.getToAssetId(), ownerUserId);

        Transfer transfer = transferRepository.findById(transferId, ownerUserId)
                .orElseThrow(TransferNotFoundException::new);

        transfer.update(
                command.getFromAssetId(),
                command.getToAssetId(),
                command.getAmount(),
                command.getTransferDate()
        );
        transfer.updateMemo(command.getMemo());

        transferRepository.save(transfer);
    }

    /**
     * 이체를 삭제한다.
     *
     * @param transferId 삭제 대상 이체 ID
     * @param ownerUserId 소유 사용자 ID
     * @throws TransferNotFoundException 대상 이체를 찾지 못한 경우
     */
    @Transactional
    public void delete(TransferId transferId, String ownerUserId) {
        Transfer transfer = transferRepository.findById(transferId, ownerUserId)
                .orElseThrow(TransferNotFoundException::new);
        transferRepository.deleteById(transfer.getId(), ownerUserId);
    }

    private void validateAssetPair(String fromAssetId, String toAssetId, String ownerUserId) {
        if (fromAssetId == null || fromAssetId.trim().isEmpty()) {
            throw new InvalidTransferAssetPairException("fromAssetId must not be blank");
        }
        if (toAssetId == null || toAssetId.trim().isEmpty()) {
            throw new InvalidTransferAssetPairException("toAssetId must not be blank");
        }
        if (fromAssetId.equals(toAssetId)) {
            throw new InvalidTransferAssetPairException("fromAssetId and toAssetId must be different");
        }

        assetValidationPort.validateAssetIds(Arrays.asList(fromAssetId, toAssetId), ownerUserId);
    }
}
