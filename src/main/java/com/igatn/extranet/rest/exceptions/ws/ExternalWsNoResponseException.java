package com.igatn.extranet.rest.exceptions.ws;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
public class ExternalWsNoResponseException extends RuntimeException {

    public ExternalWsNoResponseException() {
        super();
    }
    public ExternalWsNoResponseException(String message, Throwable cause) {
        super(message, cause);
    }
    public ExternalWsNoResponseException(String message) { super(message); }
    public ExternalWsNoResponseException(Throwable cause) {
        super(cause);
    }
}
