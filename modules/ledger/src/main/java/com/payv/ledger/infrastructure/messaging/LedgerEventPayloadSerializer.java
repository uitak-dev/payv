package com.payv.ledger.infrastructure.messaging;

import com.payv.common.event.ledger.LedgerTransactionChangedEvent;

import java.io.*;

final class LedgerEventPayloadSerializer {

    private LedgerEventPayloadSerializer() {
    }

    static byte[] serialize(LedgerTransactionChangedEvent event) {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(event);
            oos.flush();
            return bos.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("failed to serialize ledger event", e);
        }
    }

    static LedgerTransactionChangedEvent deserialize(byte[] payload) {
        if (payload == null || payload.length == 0) {
            throw new IllegalArgumentException("payload must not be empty");
        }

        try (ByteArrayInputStream bis = new ByteArrayInputStream(payload);
             ObjectInputStream ois = new ObjectInputStream(bis)) {
            Object value = ois.readObject();
            if (!(value instanceof LedgerTransactionChangedEvent)) {
                throw new IllegalStateException("invalid payload type: " + value.getClass().getName());
            }
            return (LedgerTransactionChangedEvent) value;
        } catch (IOException | ClassNotFoundException e) {
            throw new IllegalStateException("failed to deserialize ledger event", e);
        }
    }
}
