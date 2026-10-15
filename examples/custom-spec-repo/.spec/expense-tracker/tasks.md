---
status: draft
---
# expense-tracker — tasks

- [ ] Scaffold Spring Boot project via `spring init` (Maven, Java 17, `web`+`data-jpa`+`postgresql`+`validation` deps, group `com.example`, artifact `expense-tracker`, dir `expense-tracker/`)
- [ ] Add Testcontainers deps (`org.testcontainers:postgresql`, `org.testcontainers:junit-jupiter`, `spring-boot-testcontainers`) to `pom.xml`
- [ ] Configure `application.properties` with Postgres datasource placeholders and `spring.jpa.hibernate.ddl-auto=update`
- [ ] Write failing full-stack test: `POST /api/expenses` with valid body returns `201` with created expense (incl. `id`), and a follow-up `GET /api/expenses` includes it
- [ ] Implement `Expense` entity, `ExpenseRequest` DTO, `ExpenseRepository`, `ExpenseController.create()` to pass the test
- [ ] Write failing full-stack test: `POST /api/expenses` with `amount <= 0` returns `400` with JSON error body
- [ ] Write failing full-stack test: `POST /api/expenses` with missing/empty `category` returns `400` with JSON error body
- [ ] Write failing full-stack test: `POST /api/expenses` with missing/malformed `date` returns `400` with JSON error body
- [ ] Implement `GlobalExceptionHandler` mapping validation errors to `400` JSON to pass the above three tests
- [ ] Write failing full-stack test: `POST /api/expenses` with `note` omitted succeeds (`201`)
- [ ] Confirm `note` is optional on `ExpenseRequest`/`Expense` to pass the test (implement if needed)
- [ ] Write failing full-stack test: `GET /api/expenses` with no expenses recorded returns `200` with empty array
- [ ] Implement `ExpenseController.list()` to pass the test
- [ ] Write full-stack test confirming data persists across a repository-level restart simulation (e.g. save via one request, fetch via another, using the same Testcontainers Postgres instance) — should already pass given prior implementation
</content>
