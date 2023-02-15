package com.igatn.extranet.rest.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
public class APNSandboxInvalidResponseException extends RuntimeException {
    
    public APNSandboxInvalidResponseException() {
        super("APN Google Sandbox is returning an invalid response.");
    }
}
