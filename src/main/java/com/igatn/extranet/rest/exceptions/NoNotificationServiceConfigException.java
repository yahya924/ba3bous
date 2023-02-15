package com.igatn.extranet.rest.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
public class NoNotificationServiceConfigException extends RuntimeException {
    
    public NoNotificationServiceConfigException() {
        super("The notification service configuration file cannot be found.");
    }
    
    public NoNotificationServiceConfigException(String message) {
        super(message);
    }
}
