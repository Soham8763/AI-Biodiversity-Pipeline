package com.edna.biodiversity.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
@Getter
public class DataValidationException extends RuntimeException {

    private final String field;

    public DataValidationException(String message) {
        super(message);
        this.field = null;
    }

    public DataValidationException(String field, String message) {
        super(message);
        this.field = field;
    }
}
