---
status: approved
---
# Complete and delete todos
## Problem
Users cannot mark todos done or remove them.
## Goal
Toggle completion and delete individual todos, end to end.
## Non-goals
- Create/list (TICKET-002)
- Filtering and clear completed (TICKET-004)
## Acceptance criteria
- PATCH /api/todos/{id} {completed:true|false} returns 200 with updated Todo
- PATCH unknown id returns 404; missing completed returns 400
- DELETE /api/todos/{id} returns 204 and todo no longer listed; unknown id returns 404
- UI checkbox toggles completed and shows strikethrough styling
- UI delete button removes the item; items-left count updates
- Service unit tests and RestAssured tests cover the above
