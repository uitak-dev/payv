package com.payv.ledger.application.port;

import java.util.Collection;

public interface ClassificationValidationPort {
    void validateTagIds(Collection<String> tagIds, String ownerUserId);
    void validateCategorization(String categoryIdLevel1, String categoryIdLevel2, String ownerUserId);
}
