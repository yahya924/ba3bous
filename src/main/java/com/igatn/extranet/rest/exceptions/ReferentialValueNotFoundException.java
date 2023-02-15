package com.igatn.extranet.rest.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
public class ReferentialValueNotFoundException extends RuntimeException {
    
    public ReferentialValueNotFoundException() {
        super("Referential Object is not found in the database.");
    }

    public ReferentialValueNotFoundException(String label) {
        super(label + " referential Object is not found in the database.");
    }
}
