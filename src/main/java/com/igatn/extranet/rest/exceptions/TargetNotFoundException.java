package com.igatn.extranet.rest.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Custom exception class for api calls out of control
 */
@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class TargetNotFoundException extends RuntimeException {

    public TargetNotFoundException() {
        super();
    }

    public TargetNotFoundException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public TargetNotFoundException(final String message) {
        super(message);
    }

    public TargetNotFoundException(final Throwable cause) {
        super(cause);
    }
}
