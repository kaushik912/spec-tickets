# TodoMVC

Spring Boot backend + React/Vite frontend. See `docs/spec.md`, `docs/api-contract.md`.

## Run
- Backend (port 8080): `cd backend && ./mvnw spring-boot:run`
  - Swagger UI: http://localhost:8080/swagger-ui.html, spec: `/v3/api-docs`
- Frontend (proxies `/api` to 8080): `cd frontend && npm install && npm run dev`

## Running tests
- Backend: `cd backend && ./mvnw test`
- Frontend: `cd frontend && npm test`
