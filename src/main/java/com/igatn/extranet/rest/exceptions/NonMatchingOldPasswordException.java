package com.igatn.extranet.rest.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
public class NonMatchingOldPasswordException extends RuntimeException {
    
    public NonMatchingOldPasswordException() {
        super("The password provided does not match the user's current password.");
    }
}
