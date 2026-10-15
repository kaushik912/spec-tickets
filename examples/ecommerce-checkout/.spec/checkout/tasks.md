---
status: approved
---
# Tasks: Cart checkout with simulated payment auth/capture/void/refund, idempotent charge

- [x] Cart/CartItem JPA entities + CartRepository
- [x] Order/OrderItem JPA entities + OrderStatus enum + OrderRepository
- [x] IdempotencyRecord entity (scope enum CHECKOUT/CANCEL, idempotencyKey, fingerprint, orderId) with unique constraint on (scope, idempotencyKey) + IdempotencyRecordRepository
- [x] Failing test: POST /carts creates a cart; POST /carts/{cartId}/items adds a validated item; GET /carts/{cartId} returns cart with items
- [x] Implement CartController + CartService (with jakarta.validation on the item DTO) to pass the above
- [x] PaymentGatewayClient (authorize/capture/voidPayment/refund) using Random + `payment.failure-rate` property, print-statement logging
- [x] Failing test (payment.failure-rate=0.0): POST /carts/{cartId}/checkout succeeds end-to-end, order ends CONFIRMED, visible via GET /orders/{orderId}
- [x] Implement OrderAuthService.authorizeAndCreateOrder + OrderCaptureService.captureAndConfirm + CheckoutOrchestrator + checkout endpoint (happy path, no idempotency yet) to pass the above
- [x] Failing test (AUTH forced to fail): POST /checkout returns a clear failure response and no Order row is persisted
- [x] Implement/adjust AUTH-failure handling to pass the above (already satisfied by OrderAuthService's early throw-before-save; no code change needed, test went green immediately)
- [x] Failing test (AUTH forced success, CAPTURE forced fail): order ends PAYMENT_FAILED (not lost/rolled back from PENDING_CONFIRMATION), VOID path exercised
- [x] Implement CAPTURE-failure -> VOID -> PAYMENT_FAILED handling to pass the above (split `payment.failure-rate` into `payment.auth-failure-rate`/`payment.capture-failure-rate`, both defaulting to `payment.failure-rate`, so AUTH and CAPTURE outcomes can be controlled independently in tests without mocking the gateway; OrderCaptureService's void/fail logic itself was already correct from the happy-path implementation)
- [x] Failing test: POST /orders/{orderId}/cancel on a CONFIRMED order triggers simulated REFUND and moves status to CANCELLED
- [x] Implement OrderCancelService + cancel endpoint to pass the above
- [x] Failing test: two POST /checkout calls with the same Idempotency-Key header on the same cart return the same order id both times, and only one Order row exists for that cart
- [x] Implement checkout idempotency (header read in controller, IdempotencyRecord lookup/insert with unique-constraint race fallback in OrderAuthService/CheckoutOrchestrator) to pass the above
- [x] Failing test: reusing the same Idempotency-Key on POST /checkout against a different cart id returns 409 Conflict
- [x] Implement IdempotencyConflictException + @RestControllerAdvice mapping to 409 to pass the above (already implemented alongside the checkout-idempotency task; test went green immediately)
- [x] Failing test: two POST /cancel calls with the same Idempotency-Key on the same CONFIRMED order both return CANCELLED with no error (single REFUND, not two)
- [x] Implement cancel idempotency (IdempotencyRecord CANCEL-scope check + insert with race fallback in OrderCancelService) to pass the above
- [x] Write Makefile with a `test` target running `./mvnw test` (README "Running tests" section instead if a Makefile doesn't fit the project convention)
