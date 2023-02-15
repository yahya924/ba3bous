package com.igatn.extranet.security.models.otp;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

public class OtpSenderAuthentication extends UsernamePasswordAuthenticationToken {

    public OtpSenderAuthentication(String username, String otpAuthorization) {
        super(username, otpAuthorization);
    }

    public OtpSenderAuthentication(String username) {
        super(username, null);
    }
}
