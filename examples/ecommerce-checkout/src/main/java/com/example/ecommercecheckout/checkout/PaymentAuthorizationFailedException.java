package com.example.ecommercecheckout.checkout;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
public class PaymentAuthorizationFailedException extends RuntimeException {

    public PaymentAuthorizationFailedException(Long cartId) {
        super("Payment authorization failed for cart: " + cartId);
    }
}
