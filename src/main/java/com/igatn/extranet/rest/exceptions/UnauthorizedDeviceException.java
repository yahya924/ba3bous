package com.igatn.extranet.rest.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
public class UnauthorizedDeviceException  extends RuntimeException {

    public UnauthorizedDeviceException() {super("Unknown device attempting to login");}
}
