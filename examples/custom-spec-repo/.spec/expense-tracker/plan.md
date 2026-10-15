---
status: approved
---
# expense-tracker — plan

## Stack
Spring Boot 3.x, Java 17, Maven (via `spring init`), project dir `expense-tracker/` (sibling to `tinyurl/`). Dependencies: `web`, `data-jpa`, `postgresql` (driver), `validation`.

## Architecture
Single Spring Boot app, package `com.example.expensetracker`:

- `Expense` — JPA entity: `id` (generated), `amount` (`BigDecimal`, positive), `category` (String, non-blank), `date` (`LocalDate`), `note` (String, nullable)
- `ExpenseRepository` — `JpaRepository<Expense, Long>`
- `ExpenseController` — REST controller
  - `POST /api/expenses` → request body validated (`@Valid`) → save → `201` with created `Expense` (incl. `id`)
  - `GET /api/expenses` → `200` with JSON array of all expenses
- Bean Validation annotations on a request DTO (`ExpenseRequest`): `@NotNull @Positive amount`, `@NotBlank category`, `@NotNull date`, optional `note`
- `GlobalExceptionHandler` (`@RestControllerAdvice`) — maps `MethodArgumentNotValidException` → `400` with a small JSON error body `{ "error": "<message>" }`
- `application.properties` — Postgres datasource config (`spring.datasource.url/username/password`), `spring.jpa.hibernate.ddl-auto=update` for v1

## Key decisions
- **DTO vs entity on request**: use a separate `ExpenseRequest` DTO for the POST body so Bean Validation annotations don't leak onto the JPA entity; controller maps DTO → entity before saving.
- **Validation via `@Valid` + Bean Validation**: declarative, matches Spring idioms, keeps controller thin.
- **`ddl-auto=update`**: acceptable for v1 (no migrations tooling requirement stated); schema is trivial (one table).
- **No dedup/edit/delete**: matches spec non-goals, keeps v1 minimal.

## Testing seam
**Single seam.** One test type: full-stack HTTP tests via `TestRestTemplate` (or `MockMvc` against the full `@SpringBootTest` context) driving the real service, real repository, and a real Postgres instance via Testcontainers (`org.testcontainers:postgresql`, `org.testcontainers:junit-jupiter`). No mocking of internal layers, no `@WebMvcTest`/`@DataJpaTest` slices. Assertions land on HTTP status and response body / read-back state (e.g. POST then GET to confirm persistence), never on internal method calls.

## Risks
- Requires Docker available in the environment for Testcontainers to run tests — flag if unavailable and fall back to a local/dev Postgres for manual verification.
</content>
