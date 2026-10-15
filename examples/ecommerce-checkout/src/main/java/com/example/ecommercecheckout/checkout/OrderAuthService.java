package com.example.ecommercecheckout.checkout;

import com.example.ecommercecheckout.cart.Cart;
import com.example.ecommercecheckout.cart.CartItem;
import com.example.ecommercecheckout.cart.CartNotFoundException;
import com.example.ecommercecheckout.cart.CartRepository;
import com.example.ecommercecheckout.idempotency.IdempotencyRecord;
import com.example.ecommercecheckout.idempotency.IdempotencyRecordRepository;
import com.example.ecommercecheckout.idempotency.IdempotencyScope;
import com.example.ecommercecheckout.order.Order;
import com.example.ecommercecheckout.order.OrderItem;
import com.example.ecommercecheckout.order.OrderNotFoundException;
import com.example.ecommercecheckout.order.OrderRepository;
import com.example.ecommercecheckout.order.OrderStatus;
import com.example.ecommercecheckout.payment.PaymentGatewayClient;
import com.example.ecommercecheckout.payment.PaymentResult;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class OrderAuthService {

    private final CartRepository cartRepository;
    private final OrderRepository orderRepository;
    private final IdempotencyRecordRepository idempotencyRecordRepository;
    private final PaymentGatewayClient paymentGatewayClient;

    public OrderAuthService(CartRepository cartRepository, OrderRepository orderRepository,
                             IdempotencyRecordRepository idempotencyRecordRepository,
                             PaymentGatewayClient paymentGatewayClient) {
        this.cartRepository = cartRepository;
        this.orderRepository = orderRepository;
        this.idempotencyRecordRepository = idempotencyRecordRepository;
        this.paymentGatewayClient = paymentGatewayClient;
    }

    @Transactional
    public Order authorizeAndCreateOrder(Long cartId, String idempotencyKey) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new CartNotFoundException(cartId));

        BigDecimal totalAmount = cart.getItems().stream()
                .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        PaymentResult authResult = paymentGatewayClient.authorize(totalAmount);
        if (!authResult.success()) {
            throw new PaymentAuthorizationFailedException(cartId);
        }

        Order order = new Order();
        order.setCartId(cartId);
        order.setStatus(OrderStatus.PENDING_CONFIRMATION);
        order.setTotalAmount(totalAmount);
        order.setAuthTransactionId(authResult.transactionId());
        for (CartItem cartItem : cart.getItems()) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProductName(cartItem.getProductName());
            orderItem.setUnitPrice(cartItem.getUnitPrice());
            orderItem.setQuantity(cartItem.getQuantity());
            order.getItems().add(orderItem);
        }

        order = orderRepository.save(order);

        if (idempotencyKey != null) {
            try {
                idempotencyRecordRepository.save(
                        new IdempotencyRecord(IdempotencyScope.CHECKOUT, idempotencyKey, cartId, order.getId()));
                idempotencyRecordRepository.flush();
            } catch (DataIntegrityViolationException raceLost) {
                IdempotencyRecord winner = idempotencyRecordRepository
                        .findByScopeAndIdempotencyKey(IdempotencyScope.CHECKOUT, idempotencyKey)
                        .orElseThrow(() -> raceLost);
                return orderRepository.findById(winner.getOrderId())
                        .orElseThrow(() -> new OrderNotFoundException(winner.getOrderId()));
            }
        }

        return order;
    }
}
