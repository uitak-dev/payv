package com.payv.automation.application.port;

import java.util.Collection;

public interface ClassificationValidationPort {

    void validateCategorization(Collection<String> categoryIds, String ownerUserId);
}
