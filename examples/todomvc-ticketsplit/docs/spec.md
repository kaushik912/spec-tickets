# TodoMVC

## Problem
Need a simple todo app to manage tasks with a REST backend and web UI.

## Goal
Add, complete, delete todos; filter all/active/completed; clear completed.

## Non-goals
Auth, multi-user, persistence across restarts, drag-reorder, editing title, pagination.

## Stack
- Backend: Spring Boot (Java), springdoc Swagger (`/swagger-ui.html`, `/v3/api-docs`)
- Frontend: React + Vite (TypeScript), dev proxy `/api` -> backend
- DB: H2 in-memory via Spring Data JPA
- Auth: none
- Testing: layered (service unit tests + RestAssured API tests, BDD Given/When/Then); frontend Vitest + Testing Library

## Entities
Todo: `id` (long), `title` (string, 1-200 chars, trimmed), `completed` (bool, default false), `createdAt` (ISO-8601).

## Slices
| id | slug | goal | depends_on |
|----|------|------|-----------|
| TICKET-001 | scaffold | Contract + scaffold | - |
| TICKET-002 | add-list-todos | Add + list todos | TICKET-001 |
| TICKET-003 | toggle-delete | Toggle complete + delete | TICKET-002 |
| TICKET-004 | filter-clear | Filter + clear completed | TICKET-002 |
