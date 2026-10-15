---
status: approved
---
# Plan
Reuse Todo/TodoService/TodoController. Add NotFoundException -> 404 in ApiExceptionHandler; UpdateTodoRequest(Boolean completed); Todo.setCompleted; service.setCompleted/delete; controller PATCH/DELETE /{id}. Frontend: api.ts updateTodo/deleteTodo; App checkbox + delete button.
Note: DELETE /api/todos/completed (TICKET-004) must not clash; {id} is Long so path "completed" is a separate literal mapping added later.
## Testing seam
Layered: Mockito service unit tests, RestAssured API tests, Vitest component tests. `make test`.
## API docs
springdoc already wired; @Operation on new methods.
