package com.payv.ledger.application.port;

import java.util.Collection;

public interface ClassificationValidationPort {
    void validateTagIds(Collection<String> tagIds, String ownerUserId);
    void validateCategoryIds(Collection<String> categoryIds, String ownerUserId);
}
