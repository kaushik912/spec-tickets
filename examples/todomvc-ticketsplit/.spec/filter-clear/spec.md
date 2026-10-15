---
status: approved
---
# Filter and clear completed

## Problem
Users cannot narrow the list by state or bulk-remove finished todos.

## Goal
Filter all/active/completed and clear completed, end to end.

## Non-goals
- Create/list (TICKET-002)
- Single toggle/delete (TICKET-003)

## Acceptance criteria
- GET /api/todos?filter=active returns only completed=false; filter=completed only completed=true; filter=all or omitted returns all
- GET with invalid filter returns 400
- DELETE /api/todos/completed returns 200 {deleted:n}, removes only completed; n=0 when none
- UI has All/Active/Completed links; selected highlighted
- UI 'Clear completed' button shown only when completed todos exist; click removes them
- Service unit tests and RestAssured tests cover the above
