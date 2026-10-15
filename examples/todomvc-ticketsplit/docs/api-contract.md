# API Contract

Base path `/api`. JSON. No auth.

## Todo schema
```json
{ "id": 1, "title": "Buy milk", "completed": false, "createdAt": "2026-10-01T10:00:00Z" }
```

## Endpoints

### GET /api/todos?filter=all|active|completed
- `filter` optional, default `all`.
- 200: `Todo[]` ordered by `id` ascending.
- 400: invalid filter value.

### POST /api/todos
- Body: `{ "title": "string" }` (trimmed, 1-200 chars).
- 201: `Todo` (completed=false), `Location: /api/todos/{id}`.
- 400: blank/missing/too-long title.

### PATCH /api/todos/{id}
- Body: `{ "completed": true|false }`.
- 200: updated `Todo`.
- 400: missing/non-boolean `completed`.
- 404: unknown id.

### DELETE /api/todos/{id}
- 204: deleted.
- 404: unknown id.

### DELETE /api/todos/completed
- Removes all completed todos.
- 200: `{ "deleted": <count> }` (0 if none).

## Errors
Shape for 400/404:
```json
{ "status": 400, "error": "Bad Request", "message": "title must not be blank" }
```
