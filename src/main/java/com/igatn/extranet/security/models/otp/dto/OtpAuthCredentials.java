package com.igatn.extranet.security.models.otp.dto;

public record OtpAuthCredentials (
    String otpAuthorization,
    String otpCode
) { }