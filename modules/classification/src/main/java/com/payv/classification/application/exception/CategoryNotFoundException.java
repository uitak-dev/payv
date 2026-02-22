package com.payv.classification.application.exception;

import com.payv.common.error.NotFoundException;

public class CategoryNotFoundException extends NotFoundException {

    public CategoryNotFoundException(String message) {
        super("CLASSIFICATION-CATEGORY-404", message);
    }
}
