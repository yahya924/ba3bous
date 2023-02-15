package com.igatn.extranet.rest.payment.models;

import lombok.Data;

@Data
public class WsPublishableKeyResponse {
    
    Boolean success;
    String publishableKey;
}
