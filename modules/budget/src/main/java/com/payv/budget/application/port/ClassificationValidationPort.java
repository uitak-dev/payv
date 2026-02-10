package com.payv.budget.application.port;

import java.util.Collection;

public interface ClassificationValidationPort {
    void validateCategorization(Collection<String> categoryIds, String ownerUserId);
}
