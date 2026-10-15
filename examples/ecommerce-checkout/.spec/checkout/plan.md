---
status: approved
---
# Plan: Cart checkout with simulated payment auth/capture/void/refund, idempotent charge

## Stack
Spring Boot (maven, Java 17), already scaffolded: web, data-jpa, h2, validation, lombok.
H2 in-memory DB. Package root `com.example.ecommercecheckout`.

## Architecture

### Entities (JPA)
- `Cart` (id, status: OPEN/CHECKED_OUT) has-many `CartItem` (productName, unitPrice, quantity)
- `Order` (id, cartId, status, totalAmount, authTransactionId, createdAt) has-many `OrderItem` (snapshot copy of cart items at checkout time)
- `OrderStatus` enum: `PENDING_CONFIRMATION`, `CONFIRMED`, `PAYMENT_FAILED`, `CANCELLED`
- `IdempotencyRecord` (id, scope: `CHECKOUT`|`CANCEL`, idempotencyKey, fingerprint [the cartId for CHECKOUT / orderId for CANCEL], orderId, createdAt) — `@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"scope", "idempotencyKey"}))`. The unique constraint is the actual concurrency guard: two genuinely concurrent duplicate requests both attempt the insert, only one wins, the loser catches `DataIntegrityViolationException` and falls back to reading the winner's row — no explicit locking needed.

### Payment simulation
`PaymentGatewayClient` (single component, no real HTTP):
- `authorize(BigDecimal amount) -> PaymentResult(success, transactionId)`
- `capture(String transactionId) -> boolean`
- `voidPayment(String transactionId) -> boolean`
- `refund(String transactionId) -> boolean`

Each method uses `Random` for outcome and does `System.out.println("[PaymentGateway] AUTH txn=... amount=... -> SUCCESS")` (per user's ask for mock print statements, not a logger). Failure rate is externalized to `application.properties` (e.g. `payment.failure-rate=0.3`) purely so tests can force deterministic outcomes (0.0 or 1.0) without mocking the gateway — keeps the "single seam" testing approach honest.

### Services — this is the crux of the exercise
Split into **separate Spring beans** so a later transaction's rollback can never touch an earlier one's committed write, and so we avoid the classic Spring self-invocation pitfall (calling another `@Transactional` method on `this` bypasses the proxy and the boundary silently doesn't apply):

- `OrderAuthService.authorizeAndCreateOrder(cartId, idempotencyKey)` — `@Transactional`. Calls `paymentGatewayClient.authorize()` first; if it fails, throws (nothing persisted — no order row, no idempotency record, no transaction to roll back — a failed AUTH is deliberately *not* recorded as idempotent, so a retry with the same key is allowed to actually attempt a charge). If it succeeds, persists `Order` with `PENDING_CONFIRMATION` **and**, only when `idempotencyKey != null`, an `IdempotencyRecord(scope=CHECKOUT, idempotencyKey, fingerprint=cartId, orderId)` in the same transaction, then commits. On a unique-constraint violation on that insert (lost the race to a concurrent duplicate), catches it, re-reads the winning record, and returns its order instead of the one just built.
- `OrderCaptureService.captureAndConfirm(orderId, transactionId)` — `@Transactional`, separate bean from the above, invoked as a fresh top-level call (fresh transaction) by the orchestrator — never called via `this` from `OrderAuthService`. Calls `paymentGatewayClient.capture()`; on success, updates order to `CONFIRMED`; on failure, calls `voidPayment()` then updates order to `PAYMENT_FAILED` — both writes commit in this second transaction regardless of the first transaction's fate.
- `OrderCancelService.cancel(orderId, idempotencyKey)` — `@Transactional`. First, if `idempotencyKey != null`, looks up `IdempotencyRecord(scope=CANCEL, idempotencyKey)`: found + fingerprint (orderId) matches -> return the current order as-is, `refund()` is **not** called again; found + fingerprint mismatches -> throw `IdempotencyConflictException`; not found -> proceed. Only valid from `CONFIRMED`; calls `refund()`, updates status to `CANCELLED`, and (when key present) persists the `IdempotencyRecord(scope=CANCEL, ..., orderId)` in the same transaction, with the same unique-constraint race fallback as above.
- `CheckoutOrchestrator.checkout(cartId, idempotencyKey)` (plain `@Service`, **not** `@Transactional` itself) — first, if `idempotencyKey != null`, looks up `IdempotencyRecord(scope=CHECKOUT, idempotencyKey)`: found + fingerprint (cartId) matches -> load and return that order directly, skipping AUTH and CAPTURE entirely; found + fingerprint mismatches -> throw `IdempotencyConflictException`; not found -> calls `orderAuthService.authorizeAndCreateOrder(...)` then, only if that succeeded, `orderCaptureService.captureAndConfirm(...)`. Because the orchestrator itself carries no transaction, each call opens/commits its own — this is what delivers the "capture failure doesn't erase the persisted order" requirement, no `REQUIRES_NEW` needed.

### Idempotency-key handling detail
The `Idempotency-Key` header is read at the controller layer and passed down as a plain `String` (null when absent — dedup is opt-in, per spec). Conflict (key reused against a different cart/order) surfaces as `IdempotencyConflictException`, mapped by a `@RestControllerAdvice` to `409 Conflict` with a clear message. This keeps dedup logic entirely in the service layer (testable through the single seam) and the controller layer thin.

### REST endpoints
- `POST /carts` — create cart
- `POST /carts/{cartId}/items` — add item `{productName, unitPrice, quantity}`
- `GET /carts/{cartId}` — view cart
- `POST /carts/{cartId}/checkout` — optional `Idempotency-Key` header; runs the orchestrator, returns resulting order (id + status)
- `GET /orders/{orderId}` — view order + status
- `POST /orders/{orderId}/cancel` — optional `Idempotency-Key` header; runs `OrderCancelService`

### Validation
`jakarta.validation` on request DTOs (positive quantity/price, non-blank product name) — matches the `validation` dependency already pulled in.

## Testing seam
**single** (per ticket) — `MockMvc` end-to-end through the real stack (real services, real repositories, real H2), no mocking of internal layers or the `PaymentGatewayClient`. Deterministic branch coverage comes from setting `payment.failure-rate` per test class/method (`@TestPropertySource` or `@DynamicPropertySource`) rather than mocking — e.g. one test class with rate=0.0 to prove the happy path (AUTH→CONFIRMED), one with rate=1.0 on capture only to prove VOID+PAYMENT_FAILED. Kept minimal per the ticket's "no extensive tests" instruction — a handful of smoke tests, not full coverage.

Idempotency tests stay within the single-seam rule (assert on HTTP response/read-back state, never on internal calls) since there's nothing to mock-and-verify on the gateway anyway:
- Two `POST /checkout` calls with the same `Idempotency-Key` on the same cart -> both responses carry the same order id; only one `Order` row exists for that cart afterward (read back via `GET /orders/{id}` / repository count).
- Same `Idempotency-Key` reused with a different cart id -> second call returns `409`.
- Two `POST /cancel` calls with the same `Idempotency-Key` on the same CONFIRMED order -> both responses show `CANCELLED`, order stays cancelled (no error from a "double refund").
- Checkout/cancel without the header at all -> unaffected, existing behavior (control case, reuses an existing test).

## API docs
Not requested (`api_docs: false` on the ticket) — no OpenAPI/Swagger wiring.

## Risks / tradeoffs
- **Self-invocation trap**: addressed structurally by splitting auth/capture/cancel into separate beans rather than relying on `REQUIRES_NEW` inside one class — simpler to reason about and demonstrates the boundary explicitly.
- **In-memory H2**: state doesn't survive restart — acceptable, out of scope per ticket non-goals.
- **Randomness in tests**: mitigated via the `payment.failure-rate` property rather than mocking, keeping the single-seam approach intact.
- **Idempotency scope is per-endpoint, not global**: a `CHECKOUT` key and a `CANCEL` key are independent namespaces (separate `scope` column) — reusing the same key string for both is fine and not a conflict, since they protect different operations.
- **Failed-AUTH retries are intentionally not deduped**: since no order/charge exists yet, retrying with the same key after an AUTH failure is allowed to actually re-attempt payment — only a successful AUTH creates the idempotency record. Worth calling out since it's the one branch where "same key" doesn't mean "return cached result".
- **No distributed idempotency store**: a single H2 instance with a unique constraint is sufficient for this exercise; a multi-instance deployment would need the same guarantee from a shared DB, which is naturally already the architecture here (out of scope to elaborate further per non-goals).
