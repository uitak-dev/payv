package com.payv.ledger.application.command;

import com.payv.ledger.domain.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;

@Service
@RequiredArgsConstructor
public class AttachmentCommandService {

    private final TransactionRepository transactionRepository;

    private final PlatformTransactionManager transactionManager;
}
