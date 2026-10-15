package com.example.ecommercecheckout.checkout;

import com.example.ecommercecheckout.idempotency.IdempotencyConflictException;
import com.example.ecommercecheckout.idempotency.IdempotencyRecord;
import com.example.ecommercecheckout.idempotency.IdempotencyRecordRepository;
import com.example.ecommercecheckout.idempotency.IdempotencyScope;
import com.example.ecommercecheckout.order.Order;
import com.example.ecommercecheckout.order.OrderNotFoundException;
import com.example.ecommercecheckout.order.OrderRepository;
import com.example.ecommercecheckout.order.OrderStatus;
import com.example.ecommercecheckout.payment.PaymentGatewayClient;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class OrderCancelService {

    private final OrderRepository orderRepository;
    private final IdempotencyRecordRepository idempotencyRecordRepository;
    private final PaymentGatewayClient paymentGatewayClient;

    public OrderCancelService(OrderRepository orderRepository,
                               IdempotencyRecordRepository idempotencyRecordRepository,
                               PaymentGatewayClient paymentGatewayClient) {
        this.orderRepository = orderRepository;
        this.idempotencyRecordRepository = idempotencyRecordRepository;
        this.paymentGatewayClient = paymentGatewayClient;
    }

    @Transactional
    public Order cancel(Long orderId, String idempotencyKey) {
        if (idempotencyKey != null) {
            Optional<IdempotencyRecord> existing = idempotencyRecordRepository
                    .findByScopeAndIdempotencyKey(IdempotencyScope.CANCEL, idempotencyKey);
            if (existing.isPresent()) {
                IdempotencyRecord record = existing.get();
                if (!record.getFingerprint().equals(orderId)) {
                    throw new IdempotencyConflictException(
                            "Idempotency-Key already used for a different order");
                }
                return orderRepository.findById(record.getOrderId())
                        .orElseThrow(() -> new OrderNotFoundException(record.getOrderId()));
            }
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        if (order.getStatus() != OrderStatus.CONFIRMED) {
            throw new OrderNotCancellableException(orderId);
        }

        paymentGatewayClient.refund(order.getAuthTransactionId());
        order.setStatus(OrderStatus.CANCELLED);
        order = orderRepository.save(order);

        if (idempotencyKey != null) {
            try {
                idempotencyRecordRepository.save(
                        new IdempotencyRecord(IdempotencyScope.CANCEL, idempotencyKey, orderId, order.getId()));
                idempotencyRecordRepository.flush();
            } catch (DataIntegrityViolationException raceLost) {
                IdempotencyRecord winner = idempotencyRecordRepository
                        .findByScopeAndIdempotencyKey(IdempotencyScope.CANCEL, idempotencyKey)
                        .orElseThrow(() -> raceLost);
                return orderRepository.findById(winner.getOrderId())
                        .orElseThrow(() -> new OrderNotFoundException(winner.getOrderId()));
            }
        }

        return order;
    }
}
