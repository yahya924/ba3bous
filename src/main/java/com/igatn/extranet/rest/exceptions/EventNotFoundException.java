package com.igatn.extranet.rest.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
public class EventNotFoundException extends RuntimeException {
    
    public EventNotFoundException() {
        super("No event was found with the identifier provided");
    }
}
