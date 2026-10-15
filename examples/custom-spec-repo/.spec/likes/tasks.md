---
status: approved
---
# Tasks: Like and unlike quotes

- [x] Write failing test: POST /quotes/<id>/like increments likes by 1, returns 200 + updated quote, and a follow-up GET reflects it
- [x] Implement POST /quotes/<int:quote_id>/like to make it pass
- [x] Write failing test: POST /quotes/<id>/unlike decrements likes by 1, returns 200 + updated quote, and a follow-up GET reflects it
- [x] Implement POST /quotes/<int:quote_id>/unlike to make it pass
- [x] Write failing test: POST /quotes/<id>/unlike on a quote with likes == 0 floors at 0 (stays 0, not negative)
- [x] Implement flooring at 0 in unlike to make it pass
- [x] Write failing test: POST /quotes/999/like and /quotes/999/unlike both return 404
- [x] Implement 404 handling for unknown quote id on both routes to make it pass
