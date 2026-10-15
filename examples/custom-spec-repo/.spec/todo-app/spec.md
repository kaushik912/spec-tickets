---
status: approved
---
# Todo App CRUD API

## Problem
No API exists to create/track simple todo items.

## Goal
A REST API (Spring Boot) with full CRUD for todos, backed by SQLite, documented via Swagger/OpenAPI.

## Non-goals
- Auth / multi-user scoping
- Due dates, priority, description fields (title + completed only)
- Frontend UI

## Acceptance criteria
- Todo has: `id` (Long, generated), `title` (String, required, non-blank), `completed` (boolean, default false)
- `POST /todos` creates a todo from `{title}`, returns 201 + created todo (completed=false)
- `POST /todos` with blank/missing title returns 400
- `GET /todos` returns 200 + list of all todos
- `GET /todos/{id}` returns 200 + todo, or 404 if id doesn't exist
- `PUT /todos/{id}` updates title/completed, returns 200 + updated todo, or 404 if id doesn't exist
- `DELETE /todos/{id}` returns 204, or 404 if id doesn't exist
- Data persists in a SQLite file across app restarts
- Swagger UI reachable at `/swagger-ui.html` listing all 5 endpoints with request/response schemas
