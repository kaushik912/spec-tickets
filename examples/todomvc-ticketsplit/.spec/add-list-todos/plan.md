---
status: approved
---
# Plan
Backend package com.example.todomvc.todo: Todo entity, TodoRepository, TodoService, TodoController, ApiError + exception handler. Frontend: api.ts (fetch), App.tsx todo input + list + count.
## Testing seam
Layered: Mockito service unit tests, RestAssured API tests (@SpringBootTest RANDOM_PORT), Vitest component tests with mocked fetch. `make test`.
## API docs
springdoc already wired; add @Tag/@Operation on controller.
