package com.igatn.extranet.rest.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
public class EnvVariableNotFoundException extends RuntimeException {
    
    public EnvVariableNotFoundException(String envVariableName) {
        super("The environment variable" + 
                " " +
                envVariableName +
                " " +
                "can't be found in the system.");
    }
}
