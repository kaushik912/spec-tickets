---
status: approved
---
# Search and filter quotes

## Problem
GET /quotes always returns every quote — there's no way to find quotes by
author or by a keyword in the text.

## Goal
Let a client narrow the quote list via query params on the existing
GET /quotes endpoint.

## Non-goals
- Fuzzy/typo-tolerant search
- Pagination

## Acceptance criteria
- GET /quotes?author=<name> returns only quotes with an exact (case-insensitive) author match
- GET /quotes?q=<keyword> returns only quotes whose text contains the keyword (case-insensitive substring)
- author and q can be combined; result matches both filters
- GET /quotes with no query params still returns every quote, unchanged from current behavior
