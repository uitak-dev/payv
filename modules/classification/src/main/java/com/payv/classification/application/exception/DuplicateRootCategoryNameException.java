package com.payv.classification.application.exception;

import com.payv.common.error.ConflictException;

public class DuplicateRootCategoryNameException extends ConflictException {

    public DuplicateRootCategoryNameException() {
        super("CLASSIFICATION-CATEGORY-409", "duplicate root category name");
    }
}
