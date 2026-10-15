---
status: approved
---
# Contract + scaffold

## Problem
No project structure; backend/frontend tickets need shared layout, test runners, contract.

## Goal
Runnable Spring Boot backend and React/Vite frontend skeletons with test runners, Swagger, CI stub, contract in place.

## Non-goals
- Todo endpoints/UI (TICKET-002/003/004)

## Acceptance criteria
- backend/ Spring Boot app starts; /v3/api-docs 200 and /swagger-ui.html loads
- App context test passes via ./mvnw test
- frontend/ builds (npm run build) and npm test runs a smoke test green
- Vite dev server proxies /api to backend port
- docs/api-contract.md present; README documents run/test commands
- CI stub workflow runs backend and frontend tests
