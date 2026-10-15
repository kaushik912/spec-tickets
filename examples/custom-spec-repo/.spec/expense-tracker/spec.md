---
status: approved
---
# expense-tracker

## Problem
No way to record and review personal expenses within this repo.

## Goal
A Spring Boot backend API service to add expenses and list all recorded expenses, persisted in PostgreSQL.

## Non-goals
- Categories/budgets management
- Summary stats / totals
- Filtering or pagination on list
- Auth / multi-user support
- Editing or deleting expenses (v1 is add + list only)

## Acceptance criteria
- `POST /api/expenses` with JSON body `{ "amount": <positive number>, "category": "<non-empty string>", "date": "<ISO-8601 date, YYYY-MM-DD>", "note": "<optional string>" }` returns `201` with the created expense as JSON, including a generated `id`.
- `POST /api/expenses` with `amount <= 0` returns `400` with a JSON error body.
- `POST /api/expenses` with missing/empty `category` returns `400` with a JSON error body.
- `POST /api/expenses` with missing or malformed `date` returns `400` with a JSON error body.
- `POST /api/expenses` with `note` omitted succeeds (note is optional).
- `GET /api/expenses` returns `200` with a JSON array of all recorded expenses (each with `id`, `amount`, `category`, `date`, `note`).
- `GET /api/expenses` with no expenses recorded returns `200` with an empty array.
- Data is persisted in PostgreSQL; restarting the service does not lose data (given the same DB instance).
</content>
