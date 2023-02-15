package com.igatn.extranet.rest.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
public class NoUserIdDetectedException extends RuntimeException{

    public NoUserIdDetectedException() {
        super();
    }
    public NoUserIdDetectedException(String message, Throwable cause) {
        super(message, cause);
    }
    public NoUserIdDetectedException(String message) {
        super(message);
    }
    public NoUserIdDetectedException(Throwable cause) {
        super(cause);
    }
}
