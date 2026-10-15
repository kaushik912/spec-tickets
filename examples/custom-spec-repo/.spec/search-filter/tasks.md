---
status: approved
---
# Search and filter quotes — Tasks

- [x] Write failing test: GET /quotes?author=<name> returns only exact case-insensitive author matches
- [x] Implement author filter in list_quotes() to pass the test
- [x] Write failing test: GET /quotes?q=<keyword> returns only quotes with case-insensitive substring match in text
- [x] Implement q filter in list_quotes() to pass the test
- [x] Write failing test: GET /quotes?author=<name>&q=<keyword> combines both filters (AND)
- [x] Verify combined filter passes (should already pass if both filters compose)
- [x] Write failing test: GET /quotes with no query params still returns all quotes unchanged
- [x] Verify no-params case passes (should already pass; confirms no regression)
