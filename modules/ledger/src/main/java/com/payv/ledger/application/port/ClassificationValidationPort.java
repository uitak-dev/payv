package com.payv.ledger.application.port;

import java.util.Collection;

public interface ClassificationValidationPort {
    void validateTagIds(Collection<String> tagIds, String ownerUserId);
    void validateCategorization(Collection<String> categoryIds, String ownerUserId);
}
