package com.example.ecommercecheckout.checkout;

import com.example.ecommercecheckout.idempotency.IdempotencyConflictException;
import com.example.ecommercecheckout.idempotency.IdempotencyRecord;
import com.example.ecommercecheckout.idempotency.IdempotencyRecordRepository;
import com.example.ecommercecheckout.idempotency.IdempotencyScope;
import com.example.ecommercecheckout.order.Order;
import com.example.ecommercecheckout.order.OrderNotFoundException;
import com.example.ecommercecheckout.order.OrderRepository;
import com.example.ecommercecheckout.order.OrderStatus;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CheckoutOrchestrator {

    private final OrderAuthService orderAuthService;
    private final OrderCaptureService orderCaptureService;
    private final IdempotencyRecordRepository idempotencyRecordRepository;
    private final OrderRepository orderRepository;

    public CheckoutOrchestrator(OrderAuthService orderAuthService, OrderCaptureService orderCaptureService,
                                 IdempotencyRecordRepository idempotencyRecordRepository,
                                 OrderRepository orderRepository) {
        this.orderAuthService = orderAuthService;
        this.orderCaptureService = orderCaptureService;
        this.idempotencyRecordRepository = idempotencyRecordRepository;
        this.orderRepository = orderRepository;
    }

    public Order checkout(Long cartId, String idempotencyKey) {
        if (idempotencyKey != null) {
            Optional<IdempotencyRecord> existing = idempotencyRecordRepository
                    .findByScopeAndIdempotencyKey(IdempotencyScope.CHECKOUT, idempotencyKey);
            if (existing.isPresent()) {
                IdempotencyRecord record = existing.get();
                if (!record.getFingerprint().equals(cartId)) {
                    throw new IdempotencyConflictException(
                            "Idempotency-Key already used for a different cart");
                }
                return orderRepository.findById(record.getOrderId())
                        .orElseThrow(() -> new OrderNotFoundException(record.getOrderId()));
            }
        }

        Order order = orderAuthService.authorizeAndCreateOrder(cartId, idempotencyKey);
        if (order.getStatus() != OrderStatus.PENDING_CONFIRMATION) {
            // Lost a concurrent idempotency-key race: authorizeAndCreateOrder returned the
            // winner's already-progressed order, so CAPTURE was (or will be) handled by that
            // request instead — nothing left to do here.
            return order;
        }
        return orderCaptureService.captureAndConfirm(order.getId(), order.getAuthTransactionId());
    }
}
