package com.igatn.extranet.rest.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
public class RequestDataEncryptionException extends RuntimeException {
    
    public RequestDataEncryptionException() {
        super("An error has occured while trying to encrypt the request data");
    }
    
    public RequestDataEncryptionException(String message) {
        super(message);
    }
}
