# Manual end-to-end test guide

Covers the full checkout lifecycle (cart -> AUTH -> CAPTURE -> cancel/REFUND) and the
idempotency behavior on `/checkout` and `/cancel`. All commands are `curl` against a
locally running instance.

## 1. Start the app

`PaymentGatewayClient` outcomes are random by default (`payment.failure-rate=0.3` in
`application.properties`). For deterministic manual testing, override the rate(s) at
startup:

```bash
# Happy-path run: everything succeeds
./mvnw spring-boot:run -Dspring-boot.run.arguments="--payment.failure-rate=0.0"

# Force AUTH to fail, leave CAPTURE alone
./mvnw spring-boot:run -Dspring-boot.run.arguments="--payment.auth-failure-rate=1.0 --payment.capture-failure-rate=0.0"

# Force CAPTURE to fail, AUTH always succeeds
./mvnw spring-boot:run -Dspring-boot.run.arguments="--payment.auth-failure-rate=0.0 --payment.capture-failure-rate=1.0"
```

App listens on `http://localhost:8080`. Watch stdout for `[PaymentGateway] ...` lines —
that's the simulated gateway logging AUTH/CAPTURE/VOID/REFUND outcomes.

## 2. Happy path: cart -> checkout -> CONFIRMED

Run with `payment.failure-rate=0.0`.

```bash
# Create a cart
curl -s -X POST localhost:8080/carts | tee /tmp/cart.json
CART_ID=$(jq .id /tmp/cart.json)

# Add an item
curl -s -X POST localhost:8080/carts/$CART_ID/items \
  -H 'Content-Type: application/json' \
  -d '{"productName":"Widget","unitPrice":9.99,"quantity":2}'

# View the cart
curl -s localhost:8080/carts/$CART_ID | jq

# Checkout
curl -s -X POST localhost:8080/carts/$CART_ID/checkout | tee /tmp/order.json | jq
ORDER_ID=$(jq .id /tmp/order.json)
```

**Expect:** `200 OK`, `status: "CONFIRMED"`, `items` array matches the cart.

```bash
# Confirm via GET
curl -s localhost:8080/orders/$ORDER_ID | jq
```

**Expect:** same order, `status: "CONFIRMED"`.

## 3. AUTH failure: no order created

Run with `payment.auth-failure-rate=1.0` (capture rate doesn't matter — AUTH never
gets past step one).

```bash
CART_ID=$(curl -s -X POST localhost:8080/carts | jq .id)
curl -s -X POST localhost:8080/carts/$CART_ID/items \
  -H 'Content-Type: application/json' \
  -d '{"productName":"Widget","unitPrice":9.99,"quantity":1}' > /dev/null

curl -s -o /dev/null -w '%{http_code}\n' -X POST localhost:8080/carts/$CART_ID/checkout
```

**Expect:** `422`. No `[PaymentGateway] CAPTURE ...` line in the server log — checkout
never got past AUTH.

## 4. CAPTURE failure: order survives as PAYMENT_FAILED

Run with `payment.auth-failure-rate=0.0 payment.capture-failure-rate=1.0`.

```bash
CART_ID=$(curl -s -X POST localhost:8080/carts | jq .id)
curl -s -X POST localhost:8080/carts/$CART_ID/items \
  -H 'Content-Type: application/json' \
  -d '{"productName":"Widget","unitPrice":9.99,"quantity":1}' > /dev/null

ORDER_ID=$(curl -s -X POST localhost:8080/carts/$CART_ID/checkout | tee /dev/stderr | jq .id)
```

**Expect:** response `status: "PAYMENT_FAILED"`. Server log shows AUTH SUCCESS, CAPTURE
FAILURE, then a VOID line. The order row still exists (not rolled back):

```bash
curl -s localhost:8080/orders/$ORDER_ID | jq .status
```

**Expect:** `"PAYMENT_FAILED"`, not a 404.

## 5. Cancel a CONFIRMED order

Using the order from step 2 (or a fresh happy-path checkout):

```bash
curl -s -X POST localhost:8080/orders/$ORDER_ID/cancel | jq
```

**Expect:** `200`, `status: "CANCELLED"`. Server log shows a `REFUND` line.

```bash
curl -s -o /dev/null -w '%{http_code}\n' -X POST localhost:8080/orders/$ORDER_ID/cancel
```

Cancelling again **without** an `Idempotency-Key` now that it's already CANCELLED:
**expect `409`** (not cancellable — this is the "not idempotent without a key" control
case, distinct from section 7 below).

## 6. Checkout idempotency: duplicate request, one order

Run with `payment.failure-rate=0.0`. Same cart, same key, twice:

```bash
CART_ID=$(curl -s -X POST localhost:8080/carts | jq .id)
curl -s -X POST localhost:8080/carts/$CART_ID/items \
  -H 'Content-Type: application/json' \
  -d '{"productName":"Widget","unitPrice":9.99,"quantity":1}' > /dev/null

curl -s -X POST localhost:8080/carts/$CART_ID/checkout \
  -H 'Idempotency-Key: demo-key-1' | jq '.id, .status'

curl -s -X POST localhost:8080/carts/$CART_ID/checkout \
  -H 'Idempotency-Key: demo-key-1' | jq '.id, .status'
```

**Expect:** both calls return the **same** `id`, both `CONFIRMED`. Server log shows only
**one** `AUTH`/`CAPTURE` pair total — the second call makes no gateway calls at all.

## 7. Checkout idempotency conflict: same key, different cart

```bash
CART_A=$(curl -s -X POST localhost:8080/carts | jq .id)
CART_B=$(curl -s -X POST localhost:8080/carts | jq .id)
for c in $CART_A $CART_B; do
  curl -s -X POST localhost:8080/carts/$c/items \
    -H 'Content-Type: application/json' \
    -d '{"productName":"Widget","unitPrice":9.99,"quantity":1}' > /dev/null
done

curl -s -o /dev/null -w '%{http_code}\n' -X POST localhost:8080/carts/$CART_A/checkout \
  -H 'Idempotency-Key: shared-key'

curl -s -o /dev/null -w '%{http_code}\n' -X POST localhost:8080/carts/$CART_B/checkout \
  -H 'Idempotency-Key: shared-key'
```

**Expect:** first call `200`, second call `409` (key reused against a different cart).

## 8. Cancel idempotency: duplicate cancel, no error

```bash
ORDER_ID=$(curl -s -X POST localhost:8080/carts/$CART_A/checkout \
  -H 'Idempotency-Key: another-key' | jq .id)
# (skip if CART_A was already checked out above — use a fresh cart+checkout if needed)

curl -s -X POST localhost:8080/orders/$ORDER_ID/cancel \
  -H 'Idempotency-Key: cancel-key-1' | jq '.status'

curl -s -X POST localhost:8080/orders/$ORDER_ID/cancel \
  -H 'Idempotency-Key: cancel-key-1' | jq '.status'
```

**Expect:** both calls `200`, both `"CANCELLED"`. Server log shows only **one** `REFUND`
line — the second call is a replay, not a re-cancel.

## 9. Validation: reject a bad item

```bash
CART_ID=$(curl -s -X POST localhost:8080/carts | jq .id)
curl -s -o /dev/null -w '%{http_code}\n' -X POST localhost:8080/carts/$CART_ID/items \
  -H 'Content-Type: application/json' \
  -d '{"productName":"","unitPrice":-1,"quantity":0}'
```

**Expect:** `400`.

## Quick reference

| Scenario | Endpoint | Expected status |
|---|---|---|
| Create cart | `POST /carts` | 201 |
| Add item (valid) | `POST /carts/{id}/items` | 201 |
| Add item (invalid) | `POST /carts/{id}/items` | 400 |
| View cart | `GET /carts/{id}` | 200 |
| Checkout, AUTH+CAPTURE succeed | `POST /carts/{id}/checkout` | 200, CONFIRMED |
| Checkout, AUTH fails | `POST /carts/{id}/checkout` | 422, no order created |
| Checkout, CAPTURE fails | `POST /carts/{id}/checkout` | 200, PAYMENT_FAILED (order kept) |
| View order | `GET /orders/{id}` | 200 |
| Cancel CONFIRMED order | `POST /orders/{id}/cancel` | 200, CANCELLED |
| Cancel non-CONFIRMED order (no key) | `POST /orders/{id}/cancel` | 409 |
| Duplicate checkout, same key+cart | `POST /carts/{id}/checkout` | 200, same order both times |
| Same key, different cart | `POST /carts/{id}/checkout` | 409 on the second cart |
| Duplicate cancel, same key+order | `POST /orders/{id}/cancel` | 200, CANCELLED both times |
