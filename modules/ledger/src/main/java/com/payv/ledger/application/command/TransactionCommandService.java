package com.payv.ledger.application.command;

import com.payv.ledger.application.command.model.CreateTransactionCommand;
import com.payv.ledger.application.command.model.UpdateTransactionCommand;
import com.payv.ledger.application.port.AssetValidationPort;
import com.payv.ledger.application.port.ClassificationValidationPort;
import com.payv.ledger.domain.model.Transaction;
import com.payv.ledger.domain.model.TransactionId;
import com.payv.ledger.domain.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TransactionCommandService {

    private final TransactionRepository transactionRepository;

    private final ClassificationValidationPort classificationValidationPort;
    private final AssetValidationPort assetValidationPort;

    @Transactional
    public TransactionId createManual(CreateTransactionCommand command, String ownerUserId) {

        // 1) ACL 검증(존재/활성/소유권)
        /*
        if (command.getTagIds() != null && !command.getTagIds().isEmpty()) {
            classificationValidationPort.validateTagIds(command.getTagIds(), ownerUserId);
        }
        classificationValidationPort.validateCategoryId(...);
        assetValidationPort.validateAssertId(command.getAssetId(), ownerUserId);
        */

        // 2) 도메인 생성( Mandatory Fields )
        Transaction transaction = Transaction.createManual(
                ownerUserId,
                command.getTransactionType(),
                command.getAmount(),
                command.getTransactionDate(),
                command.getAssetId(),
                command.getCategoryIdLevel1()
        );

        // 3) 상세 추가( Optional Fields )
        if (command.getMemo() != null) {
            transaction.updateMemo(command.getMemo());
        }
        if (command.getCategoryIdLevel2() != null) {
            transaction.updateCategorize(transaction.getCategoryIdLevel1(), command.getCategoryIdLevel2());
        }
        if (command.getTagIds() != null) {
            transaction.updateTags(command.getTagIds());
        }

        transactionRepository.save(transaction);
        return transaction.getId();
    }

    @Transactional
    public void updateTransaction(TransactionId transactionId, UpdateTransactionCommand command, String ownerUserId) {
        Transaction tx = transactionRepository.findById(transactionId, ownerUserId)
                .orElseThrow(() -> new IllegalStateException("transaction not found"));

        tx.updateBasics(
                command.getTransactionType(),
                command.getAmount(),
                command.getTransactionDate(),
                command.getAssetId()
        );

        if (command.getMemo() != null) {
            tx.updateMemo(command.getMemo());
        }
        if (command.getCategoryIdLevel1() != null) {
            tx.updateCategorize(command.getCategoryIdLevel1(), command.getCategoryIdLevel2());
        }
        if (command.isTagIdsProvided()) {
            tx.updateTags(command.getTagIds());
        }

        transactionRepository.save(tx);
    }
}
