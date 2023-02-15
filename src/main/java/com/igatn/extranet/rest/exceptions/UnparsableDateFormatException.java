package com.igatn.extranet.rest.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
public class UnparsableDateFormatException extends RuntimeException {

    public UnparsableDateFormatException(String message) {
        super(message);
    }
}
