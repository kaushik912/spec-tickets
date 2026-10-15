package com.example.ecommercecheckout.payment;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Random;
import java.util.UUID;

@Component
public class PaymentGatewayClient {

    private final double authFailureRate;
    private final double captureFailureRate;
    private final Random random = new Random();

    public PaymentGatewayClient(
            @Value("${payment.auth-failure-rate:${payment.failure-rate:0.3}}") double authFailureRate,
            @Value("${payment.capture-failure-rate:${payment.failure-rate:0.3}}") double captureFailureRate) {
        this.authFailureRate = authFailureRate;
        this.captureFailureRate = captureFailureRate;
    }

    public PaymentResult authorize(BigDecimal amount) {
        boolean success = outcome(authFailureRate);
        String transactionId = "txn_" + UUID.randomUUID();
        System.out.println("[PaymentGateway] AUTH txn=" + transactionId + " amount=" + amount
                + " -> " + (success ? "SUCCESS" : "FAILURE"));
        return new PaymentResult(success, transactionId);
    }

    public boolean capture(String transactionId) {
        boolean success = outcome(captureFailureRate);
        System.out.println("[PaymentGateway] CAPTURE txn=" + transactionId
                + " -> " + (success ? "SUCCESS" : "FAILURE"));
        return success;
    }

    public boolean voidPayment(String transactionId) {
        System.out.println("[PaymentGateway] VOID txn=" + transactionId + " -> SUCCESS");
        return true;
    }

    public boolean refund(String transactionId) {
        System.out.println("[PaymentGateway] REFUND txn=" + transactionId + " -> SUCCESS");
        return true;
    }

    private boolean outcome(double failureRate) {
        return random.nextDouble() >= failureRate;
    }
}
