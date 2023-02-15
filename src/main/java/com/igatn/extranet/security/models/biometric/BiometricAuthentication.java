package com.igatn.extranet.security.models.biometric;

import com.igatn.extranet.rest.user.models.BiometricParameters.BiometricCredentials;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

public class BiometricAuthentication extends UsernamePasswordAuthenticationToken {

    public BiometricAuthentication(BiometricCredentials biometricAuthCredentials) {
        super(null, biometricAuthCredentials);
    }

    public BiometricAuthentication(String username) {
        super(username, null);
    }
}
