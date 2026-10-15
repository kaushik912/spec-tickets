package com.example.ecommercecheckout.checkout;

import com.example.ecommercecheckout.order.Order;
import com.example.ecommercecheckout.order.OrderNotFoundException;
import com.example.ecommercecheckout.order.OrderRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CheckoutController {

    private final CheckoutOrchestrator checkoutOrchestrator;
    private final OrderCancelService orderCancelService;
    private final OrderRepository orderRepository;

    public CheckoutController(CheckoutOrchestrator checkoutOrchestrator, OrderCancelService orderCancelService,
                               OrderRepository orderRepository) {
        this.checkoutOrchestrator = checkoutOrchestrator;
        this.orderCancelService = orderCancelService;
        this.orderRepository = orderRepository;
    }

    @PostMapping("/carts/{cartId}/checkout")
    public Order checkout(@PathVariable Long cartId,
                           @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
        return checkoutOrchestrator.checkout(cartId, idempotencyKey);
    }

    @PostMapping("/orders/{orderId}/cancel")
    public Order cancel(@PathVariable Long orderId,
                         @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
        return orderCancelService.cancel(orderId, idempotencyKey);
    }

    @GetMapping("/orders/{orderId}")
    public Order getOrder(@PathVariable Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
    }
}
