package com.igatn.extranet.rest.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
public class NoUserFoundByUsernameProvidedException extends RuntimeException {
    
    public NoUserFoundByUsernameProvidedException() {
        super("No user can be found with the username provided.");
    }
}
