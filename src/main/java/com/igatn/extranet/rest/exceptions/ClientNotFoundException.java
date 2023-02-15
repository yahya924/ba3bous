package com.igatn.extranet.rest.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
public class ClientNotFoundException extends RuntimeException {

    public ClientNotFoundException() {
        super("The external host can't be found in the database !");
    }
    
    public ClientNotFoundException(String message) {
        super(message);
    }
}
