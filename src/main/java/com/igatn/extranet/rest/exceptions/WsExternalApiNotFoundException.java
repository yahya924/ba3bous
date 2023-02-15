package com.igatn.extranet.rest.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * FRE - Custom exception handler
 */
@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
public class WsExternalApiNotFoundException extends RuntimeException {
    
    public WsExternalApiNotFoundException(String message) {
        super(message);
    }
}
