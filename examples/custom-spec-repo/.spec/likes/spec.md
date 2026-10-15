---
status: approved
---
# Like and unlike quotes

## Problem
Quotes have a likes counter but no way to change it — the API can only read the seeded value of 0.

## Goal
Let a client like or unlike a quote by id, with the count reflected on subsequent reads.

## Non-goals
- Per-user like tracking (no auth, no duplicate-like prevention)
- Persisting likes to a database (in-memory is fine, same as the rest of the app)

## Acceptance criteria
- POST /quotes/<id>/like increments that quote's likes by 1, returns 200 and the updated quote
- POST /quotes/<id>/unlike decrements likes by 1, floored at 0, returns 200 and the updated quote
- Both routes return 404 for an unknown quote id
- GET /quotes/<id> reflects the updated likes count after a like/unlike
