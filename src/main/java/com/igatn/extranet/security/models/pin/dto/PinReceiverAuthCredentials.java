package com.igatn.extranet.security.models.pin.dto;

public record PinReceiverAuthCredentials(
    String pinAuthorization, 
    String deviceId,
    String pinCode
) { }
