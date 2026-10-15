---
status: approved
---
# Cart checkout with simulated payment auth/capture/void/refund, idempotent charge

## Problem
There is no checkout flow. A user can't turn a cart into an order, and
there's no handling of the multi-step payment lifecycle (authorize ->
capture, with void/refund on failure or cancellation) or of the
transactional boundaries between order state and payment calls.

Additionally, a retried/duplicated checkout or cancel request (double click,
client retry after a timeout, etc.) must not result in the payment gateway
being charged/refunded twice or a second Order being created for the same
intent.

## Goal
A backend-only Spring Boot service demonstrating a realistic checkout flow
and Spring's @Transactional behavior across multiple steps:

1. User adds item(s) to a cart.
2. User checks out: a payment AUTH call is made to a simulated external
   gateway (randomly succeeds or fails, mocked with print/log statements,
   no real network call).
3. If AUTH fails, checkout fails and no order is created.
4. If AUTH succeeds, an Order is created and persisted with status
   PENDING_CONFIRMATION.
5. A payment CAPTURE call is then made (also randomly succeeds or fails).
6. If CAPTURE succeeds, the Order status is updated to CONFIRMED.
7. If CAPTURE fails, the existing authorized payment is VOIDed, and the
   Order reflects the failure (e.g. status PAYMENT_FAILED) — the Order row
   created in step 4 must NOT be lost/rolled back, only its status changes.
   This means AUTH+order-creation and CAPTURE+confirm are separate
   transactional units, not one big transaction — a design point worth
   being explicit about in plan.md.

Order cancellation:

8. A user can cancel an existing CONFIRMED order. This triggers a REFUND
   call against the captured payment (simulated) and the Order status
   becomes CANCELLED.

Idempotency (checkout and cancel):

9. Both POST /checkout and POST /cancel accept an optional client-supplied
   `Idempotency-Key` header (Stripe-style). A repeated request carrying a
   key already seen for that endpoint+key does NOT re-invoke the payment
   gateway (no second AUTH/CAPTURE or second REFUND) and does NOT create a
   second Order — it replays the stored result (same order id/status) from
   the first request.
10. Omitting the Idempotency-Key header means no dedup protection — the
    header is opt-in, matching how real payment gateways treat it.
11. Reusing an Idempotency-Key against a different cart/order than it was
    first recorded against is rejected as a conflict rather than silently
    replayed.

## Non-goals
- Real external payment gateway integration (all payment calls are simulated)
- Authentication/authorization, multi-user/session handling
- Extensive automated test suite (a couple of smoke tests only, if any)
- Frontend/UI of any kind
- Inventory/stock management, pricing/discount logic, tax calculation
- Distributed transaction patterns (sagas, outbox), automatic retry-with-backoff
- True concurrent-request race handling beyond a DB-level unique constraint
  on the idempotency key (e.g. no distributed locks) — a unique constraint
  is sufficient protection for this exercise

## Acceptance criteria
- Can add one or more items to a cart (in-memory per cart id is fine, or persisted — implementer's call)
- POST /checkout (or equivalent) on a cart triggers a simulated AUTH call that randomly succeeds or fails, logged via print/log statement
- When AUTH fails, checkout returns a clear failure response and no Order row is persisted
- When AUTH succeeds, an Order is persisted with status PENDING_CONFIRMATION before the CAPTURE call is attempted
- After a successful AUTH, a simulated CAPTURE call is made that randomly succeeds or fails, logged via print/log statement
- When CAPTURE succeeds, the Order's status is updated to CONFIRMED
- When CAPTURE fails, a simulated VOID call is made against the authorized payment, and the Order's status is updated to reflect the failure (e.g. PAYMENT_FAILED) rather than being deleted or left as PENDING_CONFIRMATION
- A CONFIRMED order can be cancelled via an endpoint; this triggers a simulated REFUND call and updates the Order's status to CANCELLED
- Order status transitions are visible by fetching the order (e.g. GET /orders/{id}) after each step
- @Transactional boundaries are structured so that a CAPTURE failure (and its VOID) does not roll back the already-persisted PENDING_CONFIRMATION order from the AUTH step
- POST /checkout accepts an optional Idempotency-Key header; a second checkout request with the same key on the same cart returns the same order (same id/status) without a second AUTH or CAPTURE call being made to the payment gateway
- POST /cancel accepts an optional Idempotency-Key header; a second cancel request with the same key on the same order returns the same (already-CANCELLED) order without a second REFUND call being made
- Checkout/cancel requests without an Idempotency-Key header behave as normal, non-deduplicated requests
- Reusing an Idempotency-Key against a different cart/order than it was first recorded against is rejected as a conflict, not silently replayed
