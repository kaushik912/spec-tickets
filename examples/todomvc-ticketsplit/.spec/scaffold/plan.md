---
status: approved
---
# Plan
- backend/: `spring init` Maven, Java 21, deps web, data-jpa, h2, validation + springdoc 2.8.6. Port 8080.
- frontend/: Vite react-ts, Vitest + Testing Library + jsdom; proxy /api -> http://localhost:8080.
- .github/workflows/ci.yml runs both suites. README documents commands.

## Testing seam
Layered. Backend: JUnit5 `@SpringBootTest` + RestAssured (BDD Given/When/Then); `cd backend && ./mvnw test`. Frontend: Vitest; `cd frontend && npm test`.

## API docs
springdoc-openapi-starter-webmvc-ui; `/swagger-ui.html`, `/v3/api-docs`.
