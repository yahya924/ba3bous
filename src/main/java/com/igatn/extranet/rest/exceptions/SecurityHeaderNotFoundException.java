package com.igatn.extranet.rest.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
public class SecurityHeaderNotFoundException extends RuntimeException {
    
    public SecurityHeaderNotFoundException(String header) {
        super(header + " cannot be found in the project configuration.");
    }
}
