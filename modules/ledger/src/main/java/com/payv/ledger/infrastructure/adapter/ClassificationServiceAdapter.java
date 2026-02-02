package com.payv.ledger.infrastructure.adapter;

import com.payv.ledger.application.port.ClassificationValidationPort;

import java.util.Collection;

public class ClassificationServiceAdapter implements ClassificationValidationPort {

    @Override
    public void validateTagIds(Collection<String> tagIds, String ownerUserId) {

    }

    @Override
    public void validateCategoryId(String categoryId, String ownerUserId) {

    }
}
