---
status: approved
---
# Todo App CRUD API — Plan

## Stack
Spring Boot 4.1.0, Java 17, Maven — matches `tinyurl/pom.xml` conventions in this repo.
New module: `todo-app/` at repo root.

## Architecture
- `TodoAppApplication` — main class (`spring init`)
- `Todo` — JPA entity: `id` (Long, @GeneratedValue), `title` (String, @NotBlank), `completed` (boolean)
- `TodoRepository` — `JpaRepository<Todo, Long>`
- `TodoController` — REST controller, `/todos`, maps to entity directly (no separate DTO layer — CRUD is simple enough that the entity doubles as request/response body)
- `GlobalExceptionHandler` (`@RestControllerAdvice`) — maps `EntityNotFoundException` → 404, `MethodArgumentNotValidException` → 400

## Dependencies (via `spring add`)
- `web` (spring-boot-starter-web) — REST endpoints
- `data-jpa` (spring-boot-starter-data-jpa) — repository
- `sqlite` — `org.xerial:sqlite-jdbc` + `com.github.gwenn:sqlite-dialect` (Hibernate SQLite dialect) — not a `spring add` starter, added manually to pom.xml
- `validation` (spring-boot-starter-validation) — `@NotBlank`
- `springdoc-openapi-starter-webmvc-ui` — Swagger UI, added manually to pom.xml (no spring-cli shortcut)
- test scope: `spring-boot-starter-test` (MockMvc)

## Config
`application.properties`:
- `spring.datasource.url=jdbc:sqlite:todo.db`
- `spring.jpa.hibernate.ddl-auto=update`
- separate `application-test.properties` pointing at a throwaway test SQLite file, cleaned between test runs

## Endpoints
| Method | Path | Body | Response |
|---|---|---|---|
| POST | /todos | `{title}` | 201 + Todo, or 400 |
| GET | /todos | — | 200 + Todo[] |
| GET | /todos/{id} | — | 200 + Todo, or 404 |
| PUT | /todos/{id} | `{title, completed}` | 200 + Todo, or 404 |
| DELETE | /todos/{id} | — | 204, or 404 |

## Swagger
`springdoc-openapi-starter-webmvc-ui` auto-generates OpenAPI spec from controller + entity annotations. No manual spec needed. Exposed at `/swagger-ui.html` and `/v3/api-docs`.

## Testing seam
**Single seam**: MockMvc driving `/todos` end-to-end through the real Spring context, real `TodoRepository`, real SQLite file (test-scoped file, wiped before each test class via `@DirtiesContext` or manual delete in `@BeforeEach`). No mocking, no slice tests. Assertions on HTTP status + JSON response body + follow-up GET to confirm persisted state.

## Risks
- SQLite JDBC + Hibernate dialect combo is less common than Postgres/H2 — flagged in case dependency resolution has friction; fallback is `community-dialects` Hibernate module if the standalone dialect jar is unavailable in Maven Central.
