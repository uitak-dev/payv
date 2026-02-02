package com.payv.ledger.infrastructure.adapter;

import com.payv.ledger.application.port.ClassificationQueryPort;
import com.payv.ledger.application.port.ClassificationValidationPort;

import java.util.Collection;
import java.util.Map;

public class InProcessClassificationAclAdapter implements ClassificationValidationPort, ClassificationQueryPort {

    @Override
    public void validateTagIds(Collection<String> tagIds, String ownerUserId) {

    }

    @Override
    public void validateCategorization(String categoryIdLevel1, String categoryIdLevel2, String ownerUserId) {

    }

    @Override
    public Map<String, String> getTagNames(Collection<String> tagIds, String ownerUserId) {
        return null;
    }

    @Override
    public Map<String, String> getCategoryNames(Collection<String> categoryIds, String ownerUserId) {
        return null;
    }
}
