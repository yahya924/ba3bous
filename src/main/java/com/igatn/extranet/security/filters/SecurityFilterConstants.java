package com.igatn.extranet.security.filters;

/**
 * Http custom headers, typically used for security filters
 */
public interface SecurityFilterConstants {
    
    // available authorities
    String AUTHORITY_INSURED = "Insured";
    String AUTHORITY_INSURANCE_SYSTEM = "INSURANCE_SYSTEM";
    
    // basic
    String PARAMETER_USERNAME = "username";
    String PARAMETER_PASSWORD = "password";
    
    // otp
    String CUSTOM_HEADER_OTP_TYPE = "otp-type";
    String CUSTOM_HEADER_OTP_CODE = "otp-code";

    // Pin
    String CUSTOM_HEADER_OTP_AUTHORIZATION = "otp-authorization";
    String CUSTOM_HEADER_PIN_AUTHORIZATION = "pin-authorization";
    String CUSTOM_HEADER_PIN_CODE = "pin-code";
    String CUSTOM_HEADER_DEVICE_ID = "device-identity";

    // biometrics
    String CUSTOM_HEADER_SIGNATURE = "device-signature";
    String CUSTOM_HEADER_PAYLOAD = "device-payload";
    
    // separator
    String SEPARATOR = "%";
    
    
    // msgs
    String MSG_AUTHENTICATION_SUCCEEDED = "Authentication process succeeded!";
}
