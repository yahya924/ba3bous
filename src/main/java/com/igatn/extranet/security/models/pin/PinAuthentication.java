package com.igatn.extranet.security.models.pin;

import com.igatn.extranet.security.models.pin.dto.PinAuthCredentials;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

public class PinAuthentication extends UsernamePasswordAuthenticationToken {
    
    public PinAuthentication(PinAuthCredentials pinAuthCredentials) {
        super(null, pinAuthCredentials);
    }
    
    public PinAuthentication(String username) {
        super(username, null);
    }
}
