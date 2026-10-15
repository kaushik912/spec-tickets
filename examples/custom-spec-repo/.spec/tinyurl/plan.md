---
status: approved
---
# tinyurl — plan

## Stack
Spring Boot 3.x, Java 17, Maven (via `spring init`). Dependencies: `web` (Spring MVC), no DB driver (in-memory store).

## Architecture
Single Spring Boot app, package `com.example.tinyurl` (or repo-appropriate group id):

- `UrlShortenerController` — REST controller
  - `POST /api/shorten` → `ShortenRequest { url }` → `ShortenResponse { shortCode, shortUrl }`
  - `GET /{shortCode}` → 302 redirect via `Location` header, or 404
- `UrlShortenerService` — business logic: validate URL, generate unique 6-char base62 code, store/retrieve mapping
- `ShortCodeGenerator` — random base62 code generation (6 chars, `[A-Za-z0-9]`)
- `InMemoryUrlStore` — `ConcurrentHashMap<String, String>` (code → long URL), collision check on insert
- `GlobalExceptionHandler` (`@RestControllerAdvice`) — maps `InvalidUrlException` → 400, `ShortCodeNotFoundException` → 404, both with a small JSON error body `{ "error": "<message>" }`
- `application.properties` — configurable `app.base-url` (default `http://localhost:8080`) used to build `shortUrl` in responses

## Key decisions
- **In-memory store**: `ConcurrentHashMap` is sufficient for v1 (no persistence requirement); thread-safe for concurrent requests without extra locking.
- **Collision handling**: generate a candidate code, check `store.containsKey`, retry if taken (map is small relative to 62^6 keyspace, retries expected to be rare).
- **URL validation**: use `java.net.URI` + check scheme is `http`/`https` and host is non-empty; reject otherwise with `InvalidUrlException`.
- **No dedup**: service always generates a fresh code per shorten call, per spec.
- **Redirect via `ResponseEntity`**: build `302` manually with `Location` header rather than `RedirectView`, to keep controller behavior explicit and testable.

## Risks
- None significant for v1 scope — no external dependencies, no persistence, no auth surface.

## Testing approach
- `@SpringBootTest` + `MockMvc` (or `@WebMvcTest` for controller, plain unit tests for service/generator) covering each acceptance criterion from spec.md.
