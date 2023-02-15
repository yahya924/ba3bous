package com.igatn.extranet.rest.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
public class NullAuthenticationPrincipalException extends RuntimeException {

    public NullAuthenticationPrincipalException() {
        super("The authentication principal (User/UserDetails) can't be extracted correctly from the Spring Context.");
    }
}
