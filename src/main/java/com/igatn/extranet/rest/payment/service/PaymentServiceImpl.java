package com.igatn.extranet.rest.payment.service;

import com.igatn.extranet.app.AppService;
import com.igatn.extranet.rest.exceptions.EnvVariableNotFoundException;
import com.igatn.extranet.rest.exceptions.StripePaymentException;
import com.igatn.extranet.rest.payment.models.WsPaymentParams;
import com.igatn.extranet.rest.payment.models.WsPaymentResponse;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
public class PaymentServiceImpl implements PaymentService {

    @Autowired
    private AppService appService;

    @PostConstruct
    public void init() {
        Stripe.apiKey = Optional.ofNullable(appService.getAppProperties().getSecurity().getKeys().getStripeSecretKey())
                .orElseThrow(() -> new EnvVariableNotFoundException(STRIPE_SECRET_KEY));
    }
    
    @Override
    public String getStripePublishableKey() {
        return Optional.ofNullable(appService.getAppProperties().getSecurity().getKeys().getStripePublishableKey())
                .orElseThrow(() -> new EnvVariableNotFoundException(STRIPE_PUBLISHABLE_KEY));
    }

    @Override
    public WsPaymentResponse createPaymentIntent(WsPaymentParams parameters) {
        Optional<String> paymentIntentId = Optional.ofNullable(parameters.getPaymentIntentId());
        
        try {
            if (!paymentIntentId.isPresent()) {
                Map<String, Object> paymentIntentParameters = new HashMap<>();
                
                int amount = (int) (Arrays.stream(parameters.getAmounts()).sum() * 100);

                if (amount > 0) {
                    paymentIntentParameters.put(amount_key, amount);
                    paymentIntentParameters.put(currency_key, parameters.getCurrency());
                    paymentIntentParameters.put(payment_method_key, parameters.getPaymentMethodId());
                    paymentIntentParameters.put(confirm_key, true);
                    paymentIntentParameters.put(confirmation_method_key, parameters.getConfirmationMethod());
                    paymentIntentParameters.put(use_stripe_sdk_key, parameters.getUseStripeSdk());

                    PaymentIntent paymentIntent = PaymentIntent.create(paymentIntentParameters);

                    return getResponseByIntentStatus(paymentIntent);
                }

                throw new StripePaymentException("The amount that would be retrieved from the payment is equal to 0. Please verify the payment data.");
            }
            else {
                PaymentIntent paymentIntent = PaymentIntent.retrieve(parameters.getPaymentIntentId());

                return getResponseByIntentStatus(paymentIntent);
            }
        }
        catch (StripeException e) {
            throw new StripePaymentException(e.getMessage());
        }
    }
    
    private WsPaymentResponse getResponseByIntentStatus(PaymentIntent intent) {
        WsPaymentResponse response = new WsPaymentResponse();
        
        switch (intent.getStatus()) {
            case "requires_action" -> {
                response.setSuccess(false);
                response.setRequiresAction(true);
                response.setClientSecret(intent.getClientSecret());
            }
            case "requires_confirmation" -> {
                response.setSuccess(false);
                response.setRequiresConfirmation(true);
                response.setPaymentIntentId(intent.getId());
            }
            case "processing" -> {
                response.setSuccess(false);
                response.setStillProcessing(true);
            }
            case "succeeded" -> {
                response.setSuccess(true);
            }
            default -> {
                throw new StripePaymentException("The user's card was denied, It is preferred to provide another payment method.");
            }
        }
        
        return response;
    }
}
