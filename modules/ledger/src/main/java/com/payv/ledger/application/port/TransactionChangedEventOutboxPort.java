package com.payv.ledger.application.port;

import com.payv.common.event.ledger.LedgerTransactionChangedEvent;

/**
 * 거래 변경 이벤트를 Outbox에 저장하는 포트.
 * - 비즈니스 트랜잭션 안에서 이벤트를 메시지 브로커 대신 DB Outbox로 적재한다.
 * - DB 커밋 성공과 이벤트 발행 요청의 원자성을 확보하여, 메시지 유실 가능성을 줄인다.
 */
public interface TransactionChangedEventOutboxPort {

    /**
     * 거래 변경 이벤트를 Outbox에 저장한다.
     *
     * @param event 거래 변경 이벤트
     */
    void enqueue(LedgerTransactionChangedEvent event);
}
