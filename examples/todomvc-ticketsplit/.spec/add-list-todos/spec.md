---
status: approved
---
# Add and list todos
## Problem
Users cannot create or see todos.
## Goal
Create a todo and see the list, end to end (API + UI).
## Non-goals
- Toggle/delete (TICKET-003)
- Filtering, clear completed (TICKET-004)
## Acceptance criteria
- POST /api/todos valid title -> 201, Todo completed=false, Location header
- POST blank/missing/>200 title -> 400 error shape
- Title trimmed
- GET /api/todos ordered by id asc ([] when none)
- UI: Enter adds, clears input, shows item; blank adds nothing
- UI shows items-left count
- Service unit tests + RestAssured tests
