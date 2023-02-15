package com.igatn.extranet.security.models.pin.dto;

public record PinAuthCredentials(
    String deviceId,
    String pinCode
) { }
