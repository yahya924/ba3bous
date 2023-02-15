package com.igatn.extranet.rest.user.models;

import lombok.Data;

public interface BiometricParameters {
    
    @Data
    class BiometricConfig {
        private String deviceId;
        private String publicKey;
        private Boolean state;
    }

    @Data
    class BiometricCredentials {
        private final String signature;
        private final String deviceId;
        private final String payload;
    }

}
