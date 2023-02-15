package com.igatn.extranet.security.models.otp;

import com.igatn.extranet.security.models.otp.dto.OtpAuthCredentials;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

public class OtpReceiverAuthentication extends UsernamePasswordAuthenticationToken {

    public OtpReceiverAuthentication(OtpAuthCredentials otpAuthCredentials) {
        super(null, otpAuthCredentials);
    }

    public OtpReceiverAuthentication(String username, String otpCode) {
        super(username, otpCode);
    }
}
