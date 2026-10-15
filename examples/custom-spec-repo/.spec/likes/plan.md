---
status: approved
---
# Plan: Like and unlike quotes

## Architecture
Flask app in `quote-board/app.py`, in-memory `quotes` list. Add two new routes, `POST /quotes/<int:quote_id>/like` and `POST /quotes/<int:quote_id>/unlike`, following the existing pattern (`find_quote`, `abort(404)`, `jsonify`).

## Components touched
- `quote-board/app.py`: add `like_quote` and `unlike_quote` view functions.
- `quote-board/tests/test_app.py`: add tests for like/unlike behavior.

## Key decisions
- Mutate the quote dict in place (`quote["likes"] += 1` / `-= 1`, floored at 0) — consistent with existing in-memory, no-DB style.
- Reuse `find_quote` + `abort(404)` for unknown ids, matching `get_quote`.
- Return `jsonify(quote)` with default 200 status, matching `get_quote`.

## Testing seam
Single seam (from ticket): HTTP via Flask test client, no mocking, asserting on status code + response body + read-back state via a follow-up `GET`. Matches existing `tests/test_app.py` style.

## API docs
None (ticket: api_docs: false).

## Risks
Low — small, additive change to an existing in-memory Flask app; no concurrency concerns given single-process test client usage.
