package com.example.ecommercecheckout.checkout;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class OrderNotCancellableException extends RuntimeException {

    public OrderNotCancellableException(Long orderId) {
        super("Order is not in a cancellable state: " + orderId);
    }
}
