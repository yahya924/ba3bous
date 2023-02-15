package com.igatn.extranet.security.models.pin;

import com.igatn.extranet.security.models.pin.dto.PinReceiverAuthCredentials;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

public class PinReceiverAuthentication extends UsernamePasswordAuthenticationToken {

    public PinReceiverAuthentication(String username, PinReceiverAuthCredentials pinAuthCredentials) {
        super(username,pinAuthCredentials);
    }

    public PinReceiverAuthentication(String token) {
        super(token,null);
    }
}

