---
status: approved
---
# Plan
Spring Boot + JPA backend, React/Vitest frontend (existing). Per docs/api-contract.md.
- Repo: `findByCompletedOrderByIdAsc(boolean)`, `deleteByCompleted(boolean)` returning long (@Transactional in service).
- Service: `list(String filter)` null/all/active/completed else IllegalArgumentException (-> 400 via existing handler); `clearCompleted()`.
- Controller: `@RequestParam(required=false) filter`; `DELETE /completed` -> `{deleted:n}`.
- Tests set completed state via ReflectionTestUtils (no Todo.java change; avoids conflict with TICKET-003).
- UI: filter state; fetch `/api/todos?filter=`; links All/Active/Completed; Clear completed button.

## Testing seam
Layered: Mockito service unit tests + RestAssured full-stack API tests (`./mvnw test`), Vitest + Testing Library for UI (`npm test`).

## API docs
Already present (springdoc, /swagger-ui.html, /v3/api-docs); add @Operation to new endpoints.
