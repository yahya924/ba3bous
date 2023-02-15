package com.igatn.extranet.rest.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
public class AESKeyGenerationException extends RuntimeException {
    
    public AESKeyGenerationException() {
        super("An error has occured while trying to generate the internal AES key");
    }
    
    public AESKeyGenerationException(String message) {
        super(message);
    }
}
