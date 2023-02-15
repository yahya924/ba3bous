package com.igatn.extranet.rest.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
public class InvalidUserPlatformException extends RuntimeException {
    
    public InvalidUserPlatformException() {
        super("The user is probably trying to connect from a platform that doesn't support notifications.");
    }
}
