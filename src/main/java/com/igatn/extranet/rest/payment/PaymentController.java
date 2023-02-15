package com.igatn.extranet.rest.payment;

import com.igatn.extranet.rest.payment.models.WsPaymentParams;
import com.igatn.extranet.rest.payment.models.WsPaymentResponse;
import com.igatn.extranet.rest.payment.models.WsPublishableKeyResponse;
import com.igatn.extranet.rest.payment.service.PaymentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping(path = "/payment", produces = "application/json")
public class PaymentController {
    
    @Autowired
    private PaymentService paymentService;

    /**
     * test the controller
     *
     * @return
     */
    @GetMapping
    public String helloPayment() {
        log.info("/payment");

        return "Hello from /payment";
    }

    /**
     * get stripe publishable key endpoint
     *
     * @return
     */
    @GetMapping("publishableKey")
    public ResponseEntity<WsPublishableKeyResponse> getPublishableKey() {
        log.info("-- /payment/getPublishableKey endpoint called! --");

        String stripePublishableKey = paymentService.getStripePublishableKey();

        WsPublishableKeyResponse response = new WsPublishableKeyResponse();
        
        response.setSuccess(stripePublishableKey.length() > 0);
        response.setPublishableKey(stripePublishableKey);
        
        return ResponseEntity.ok(response);
    }

    /**
     * make payment operation by creating a payment intent
     *
     * @return
     */
    @PostMapping("makePayment")
    public ResponseEntity<WsPaymentResponse> makePayment(@RequestBody WsPaymentParams parameters) {
        log.info("-- /payment/makePayment endpoint called! --");
        
        return ResponseEntity.ok(paymentService.createPaymentIntent(parameters));
    }
}
