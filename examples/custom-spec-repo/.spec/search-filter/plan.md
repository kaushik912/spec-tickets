---
status: approved
---
# Search and filter quotes — Plan

## Architecture
Flask app, single `app.py`, in-memory `quotes` list. Modify `list_quotes()`
handler for `GET /quotes` to read optional `author` and `q` query params via
`request.args.get()` and filter the in-memory list before returning JSON.
No new modules, no DB, no new routes.

## Components touched
- `quote-board/app.py` — `list_quotes()`
- `quote-board/tests/test_app.py` — new test cases

## Key decisions
- Filtering done in Python with a list comprehension; `author` compared via
  case-insensitive exact match (`.lower() ==`), `q` via case-insensitive
  substring (`in`) on `text`.
- Both filters combine with AND when both provided.
- No query params -> return `quotes` unchanged (existing behavior preserved).

## Risks
- None significant; small, isolated change to a pure in-memory list.

## Testing seam
Single seam (per ticket): tests drive the Flask test client end-to-end via
`GET /quotes?...`, asserting on response status/JSON body only, matching the
existing pattern in `test_app.py`.

## API docs
None (per ticket `api_docs: false`).
