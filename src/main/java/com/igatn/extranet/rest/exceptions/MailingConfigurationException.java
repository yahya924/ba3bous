package com.igatn.extranet.rest.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
public class MailingConfigurationException extends RuntimeException {
    
    public MailingConfigurationException() {
        super("An error has occured while trying to configure the mail to send.");
    }
}
