# tinyurl

URL shortener API. Part of `spec-driven/custom-spec-repo` (see `../CLAUDE.md`).

## Spec
- `../.spec/tinyurl/{spec,plan,tasks}.md` (spec -> plan -> tasks -> implement).
- Spring Boot 4 test gotchas: `../docs/spring-boot-4-notes.md`.

## Stack
- Spring Boot 4.1.0, Java 17, Maven wrapper (`./mvnw`).
- Deps: webmvc only; test: webmvc-test, restclient. No DB, no Docker.

## Commands
- Run: `./mvnw spring-boot:run` (port 8080 default)
- Test: `./mvnw test`; single: `-Dtest=UrlShortenerControllerTest#method`

## Config
- `app.base-url=http://localhost:8080` (application.properties). No env vars.

## Layout
`com.example.tinyurl`: `UrlShortenerController` -> `UrlShortenerService` -> `InMemoryUrlStore`; `ShortCodeGenerator`; `GlobalExceptionHandler` maps `InvalidUrlException`/`ShortCodeNotFoundException` to `ErrorResponse`.

## Gotchas
- In-memory store: data lost on restart.
- Tests: `@WebMvcTest` pkg `org.springframework.boot.webmvc.test.autoconfigure`, `@MockitoBean`, `TestRestTemplate` needs `@AutoConfigureTestRestTemplate`.
