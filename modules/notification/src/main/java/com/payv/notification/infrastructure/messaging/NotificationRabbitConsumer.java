package com.payv.notification.infrastructure.messaging;

import com.payv.common.event.ledger.LedgerTransactionChangedEvent;
import com.payv.notification.application.listener.NotificationPolicyHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component("notificationRabbitConsumer")
@RequiredArgsConstructor
public class NotificationRabbitConsumer {

    private final NotificationPolicyHandler notificationPolicyHandler;

    public void handleLedgerTransactionChanged(LedgerTransactionChangedEvent event) {
        notificationPolicyHandler.handleLedgerTransactionChanged(event);
    }
}
