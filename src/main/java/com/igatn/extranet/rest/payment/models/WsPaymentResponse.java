package com.igatn.extranet.rest.payment.models;

import lombok.Data;

@Data
public class WsPaymentResponse {
    
    Boolean success;
    Boolean requiresAction;
    Boolean requiresConfirmation;
    Boolean stillProcessing;
    String clientSecret;
    String paymentIntentId;
}
