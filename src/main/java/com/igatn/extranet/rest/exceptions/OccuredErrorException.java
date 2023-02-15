package com.igatn.extranet.rest.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
public class OccuredErrorException extends RuntimeException {
    
    public OccuredErrorException(String message) {
        super(message);
    }
}
