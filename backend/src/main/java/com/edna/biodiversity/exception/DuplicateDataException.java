package com.edna.biodiversity.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class DuplicateDataException extends RuntimeException {

    public DuplicateDataException(String message) {
        super(message);
    }

    public DuplicateDataException(String entityName, String identifier) {
        super(String.format("%s already exists with identifier: %s", entityName, identifier));
    }
}
