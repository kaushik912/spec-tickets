package com.example.ecommercecheckout.checkout;

import com.example.ecommercecheckout.order.Order;
import com.example.ecommercecheckout.order.OrderNotFoundException;
import com.example.ecommercecheckout.order.OrderRepository;
import com.example.ecommercecheckout.order.OrderStatus;
import com.example.ecommercecheckout.payment.PaymentGatewayClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderCaptureService {

    private final OrderRepository orderRepository;
    private final PaymentGatewayClient paymentGatewayClient;

    public OrderCaptureService(OrderRepository orderRepository, PaymentGatewayClient paymentGatewayClient) {
        this.orderRepository = orderRepository;
        this.paymentGatewayClient = paymentGatewayClient;
    }

    @Transactional
    public Order captureAndConfirm(Long orderId, String transactionId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        boolean captured = paymentGatewayClient.capture(transactionId);
        if (captured) {
            order.setStatus(OrderStatus.CONFIRMED);
        } else {
            paymentGatewayClient.voidPayment(transactionId);
            order.setStatus(OrderStatus.PAYMENT_FAILED);
        }

        return orderRepository.save(order);
    }
}
