package com.igatn.extranet.app;


import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

/**
 * FRE - Custom properties config mapper
 * 
 */
@ConstructorBinding
@RequiredArgsConstructor
@Getter
@ConfigurationProperties(prefix = "igatn.extranet")
public class AppProperties {

    // App title
    private final String name;

    // external api object
    private final ExternalApi externalApi;

    private final Security security;
    
    private final Notifications notifications;

    @RequiredArgsConstructor
    @Getter
    @ToString
    public static class ExternalApi { private final String url; }

    @RequiredArgsConstructor
    @Getter
    @ToString
    public static class Security {

        private final InMemoryUser inMemoryUser;
        private final IGASecurityHeaders headers;
        private final IGASecurityExpirations expiration;
        private final IGASecurityKeys keys;

        @RequiredArgsConstructor
        @Getter
        @ToString
        public static class InMemoryUser {
            private final String username, password, role;
        }

        @RequiredArgsConstructor
        @Getter
        @ToString
        public static class IGASecurityHeaders {
            private final String noInterceptorHeader;
            private final String internalEncryptionKeyHeader;
        }

        @RequiredArgsConstructor
        @Getter
        @ToString
        public static class IGASecurityExpirations {
            private final int forgetPasswordPeriod;
            private final int otpCode;
            private final int jwtToken;
        }

        @RequiredArgsConstructor
        @Getter
        @ToString
        public static class IGASecurityKeys {
            private final String aesEncryptionSharedKey;
            private final String stripePublishableKey;
            private final String stripeSecretKey;
            private final String signing;
        }
    }

    @RequiredArgsConstructor
    @Getter
    @ToString
    public static class Notifications { 
        private final String url;
        private final String scope;
        private final String configPath;
        private final String apnSandboxUrl;
        private final String firebaseServerKey;
        private final String iosBundleId;
    }
}
