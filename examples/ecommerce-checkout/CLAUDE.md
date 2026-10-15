# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

Spring Boot 4.1 / Java 17 / Maven backend demoing checkout with simulated payment (AUTH -> CAPTURE, VOID/REFUND) and `@Transactional` boundaries. H2 in-memory DB. Package root `com.example.ecommercecheckout`. Spec-driven: design lives in `.spec/checkout/{spec,plan,tasks}.md`, ticket in `tickets/TICKET-001.yaml`.

## Commands

- Test all: `./mvnw test` (or `make test`)
- Single test class: `./mvnw test -Dtest=CheckoutCaptureFailureTest`
- Single method: `./mvnw test -Dtest=CheckoutCaptureFailureTest#methodName`
- Run app (port 8080): `./mvnw spring-boot:run -Dspring-boot.run.arguments="--payment.failure-rate=0.0"`
- Manual curl walkthrough: `MANUAL_TEST.md`
- Bruno smoke collection: `bruno/` (`npx -y @usebruno/cli run checkout/smoke --env local -r` from `bruno/`; CI workflow `bruno-smoke.yml` is manual-dispatch and builds a Docker image)
- No linter configured.

## Architecture

Packages: `cart`, `order`, `checkout`, `idempotency`, `payment`.

**Transaction split is the core design; do not merge services or collapse into one `@Transactional`.**
- `CheckoutOrchestrator` is deliberately NOT `@Transactional`. It calls `OrderAuthService.authorizeAndCreateOrder` (tx 1: AUTH, persist Order `PENDING_CONFIRMATION` + idempotency record), then `OrderCaptureService.captureAndConfirm` (tx 2: CAPTURE -> `CONFIRMED`, or VOID -> `PAYMENT_FAILED`).
- Separate beans so each call crosses a proxy; never call one `@Transactional` method from another via `this` (self-invocation skips the proxy). Capture failure must not roll back the persisted order.
- AUTH failure throws before anything is persisted, and is intentionally not recorded as idempotent (retry with same key may re-attempt).
- `OrderCancelService.cancel`: only valid from `CONFIRMED`; calls REFUND -> `CANCELLED`.

**Idempotency**: optional `Idempotency-Key` header, passed to services as nullable `String`. `IdempotencyRecord` (scope CHECKOUT|CANCEL, key, fingerprint = cartId / orderId) has a unique constraint on (scope, key) as the concurrency guard: the insert loser catches `DataIntegrityViolationException` and reads the winner's row (no explicit locking). Same key + different fingerprint -> `IdempotencyConflictException` -> 409 via `IdempotencyExceptionHandler`. Replays return the existing order without calling the gateway again.

**Payment simulation**: `PaymentGatewayClient` is a single component, no HTTP; random outcomes with `System.out.println("[PaymentGateway] ...")`. Failure rates: `payment.failure-rate` (default 0.3 in `application.properties`), overridable per-step via `payment.auth-failure-rate` / `payment.capture-failure-rate`. Env var `PAYMENT_FAILURE_RATE` works too.

## Testing convention

Single seam: `@SpringBootTest` + `@AutoConfigureMockMvc`, real services/repos/H2, no mocking of internals or the gateway. Determinism comes from `@TestPropertySource` failure rates (0.0 / 1.0) per test class (e.g. `auth-failure-rate=0.0` + `capture-failure-rate=1.0`). Assert on HTTP responses / read-back state only. Jackson is 3.x (`tools.jackson.databind.ObjectMapper`); MockMvc autoconfig lives in `org.springframework.boot.webmvc.test.autoconfigure`.

## Notes

- Endpoints: `POST /carts`, `POST /carts/{id}/items`, `GET /carts/{id}`, `POST /carts/{id}/checkout`, `GET /orders/{id}`, `POST /orders/{id}/cancel`.
- springdoc-openapi is in the pom, though the ticket set `api_docs: false` (plan says no OpenAPI wiring).
- Lombok via annotation processor configured explicitly in `maven-compiler-plugin` in `pom.xml`.
