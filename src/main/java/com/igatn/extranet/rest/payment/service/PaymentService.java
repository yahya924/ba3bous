package com.igatn.extranet.rest.payment.service;

import com.igatn.extranet.rest.payment.models.WsPaymentParams;
import com.igatn.extranet.rest.payment.models.WsPaymentResponse;

public interface PaymentService {
    
    String STRIPE_PUBLISHABLE_KEY = "STRIPE_PUBLISHABLE_KEY";
    String STRIPE_SECRET_KEY = "STRIPE_SECRET_KEY";
    
    String amount_key = "amount";
    String currency_key = "currency";
    String payment_method_key = "payment_method";
    String confirm_key = "confirm";
    String confirmation_method_key = "confirmation_method";
    String use_stripe_sdk_key = "use_stripe_sdk";
    
    String getStripePublishableKey();

    WsPaymentResponse createPaymentIntent(WsPaymentParams parameters);
}
