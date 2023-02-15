package com.igatn.extranet.rest.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
public class InvalidWsParametersException extends RuntimeException {

    public InvalidWsParametersException() {
        super("The parameters that has to be sent within the request body are invalid.");
    }
}
