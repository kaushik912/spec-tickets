---
status: approved
---
# tinyurl — tasks

- [x] Scaffold Spring Boot project via `spring init` (Maven, Java 17, `web` dependency, group `com.example`, artifact `tinyurl`)
- [x] Add `app.base-url` config property (default `http://localhost:8080`) to `application.properties`
- [x] Write failing test for `ShortCodeGenerator` producing a 6-char base62 code
- [x] Implement `ShortCodeGenerator` to pass the test
- [x] Write failing test for `InMemoryUrlStore` save/retrieve and collision detection
- [x] Implement `InMemoryUrlStore` to pass the test
- [x] Write failing test for `UrlShortenerService.shorten()` returning a unique short code for a valid URL and storing the mapping
- [x] Implement `UrlShortenerService.shorten()` to pass the test
- [x] Write failing test for `UrlShortenerService.shorten()` throwing `InvalidUrlException` on a malformed/non-http(s) URL
- [x] Implement URL validation in `UrlShortenerService.shorten()` to pass the test
- [x] Write failing test for `UrlShortenerService.resolve()` returning the long URL for a known code
- [x] Implement `UrlShortenerService.resolve()` to pass the test
- [x] Write failing test for `UrlShortenerService.resolve()` throwing `ShortCodeNotFoundException` on an unknown code
- [x] Implement not-found handling in `UrlShortenerService.resolve()` to pass the test
- [x] Write failing `@WebMvcTest` for `POST /api/shorten` returning 201 with `{shortCode, shortUrl}` for a valid URL
- [x] Implement `UrlShortenerController.shorten()` to pass the test
- [x] Write failing `@WebMvcTest` for `POST /api/shorten` returning 400 with JSON error body for an invalid URL
- [x] Implement `GlobalExceptionHandler` mapping for `InvalidUrlException` to pass the test
- [x] Write failing `@WebMvcTest` for `GET /{shortCode}` returning 302 with correct `Location` header for a known code
- [x] Implement `UrlShortenerController.redirect()` to pass the test
- [x] Write failing `@WebMvcTest` for `GET /{shortCode}` returning 404 with JSON error body for an unknown code
- [x] Implement `GlobalExceptionHandler` mapping for `ShortCodeNotFoundException` to pass the test
- [x] Write test confirming two `POST /api/shorten` calls with the same URL produce different short codes (no-dedup behavior, should already pass)
- [x] Write `@SpringBootTest` end-to-end test hitting the real app over HTTP (`TestRestTemplate`, real service + store) covering shorten→redirect round trip, unknown-code 404, and invalid-URL 400
