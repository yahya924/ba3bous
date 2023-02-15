package com.igatn.extranet.rest.payment.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

@Data
public class WsPaymentParams {

    @AllArgsConstructor
    private enum currencies {
        EUR("eur"),
        USD("usd");
        
        @Getter
        private String code;
    }
    
    private double[] amounts;
    private String paymentMethodId;
    private Boolean useStripeSdk;
    private String paymentIntentId;
    private String currency = currencies.EUR.getCode();
    private String confirmationMethod = "manual";
}
