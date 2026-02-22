package com.payv.classification.application.exception;

import com.payv.common.error.NotFoundException;

public class TagNotFoundException extends NotFoundException {

    public TagNotFoundException() {
        super("CLASSIFICATION-TAG-404", "tag not found");
    }
}
