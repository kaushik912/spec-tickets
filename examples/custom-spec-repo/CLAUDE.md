# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

When reporting information to me, be extremely concise and sacrifice grammar for sake of concision.

## Overview

Sandbox for spec-driven development experiments. Three independent, unrelated apps (no root build) plus spec artifacts. Each app is its own project; `cd` into it first.

- `tinyurl/` - URL shortener. Spring Boot 4.1.0, Java 17, Maven wrapper. In-memory store (`InMemoryUrlStore`). Layers: `UrlShortenerController` -> `UrlShortenerService` -> store, `ShortCodeGenerator`, `GlobalExceptionHandler` maps `InvalidUrlException`/`ShortCodeNotFoundException` to `ErrorResponse`.
- `todo-app/` - Todo CRUD API. Spring Boot 4.1.0, Java 17, SQLite (`todo.db` in cwd), springdoc Swagger. See `todo-app/USAGE.md` for endpoints. Tests use `src/test/resources/application-test.properties`.
- `quote-board/` - Flask app, single file `app.py`, in-memory `quotes` list (no DB). Used by `parallel-tutorial.md` dry run.

## Commands

Java apps (run inside `tinyurl/` or `todo-app/`):
- Run: `./mvnw spring-boot:run` (port 8080)
- All tests: `./mvnw test`
- Single test: `./mvnw test -Dtest=UrlShortenerControllerTest` or `-Dtest=UrlShortenerControllerTest#methodName`
- Swagger (todo-app): `/swagger-ui.html`, `/v3/api-docs`

quote-board (use venv; `.venv` at repo root exists, or create one in `quote-board/`):
- `cd quote-board && python3 -m venv .venv && .venv/bin/pip install -r requirements.txt`
- All tests: `.venv/bin/pytest -q` (expects 4 base tests passing)
- Single test: `.venv/bin/pytest tests/test_app.py::test_name`

## Spec-driven workflow

- `.spec/<feature>/{spec,plan,tasks}.md` - per-feature spec artifacts (tinyurl, todo-app, expense-tracker, likes, search-filter). Spec -> plan -> tasks -> implement.
- `quote-board/tickets/*.yaml` - tickets fed to the `ticket-spec` skill (`/ticket-spec <ticket.yaml>`); fields include `acceptance`, `stack_notes`, `testing_seam`, `auto_approve`. `parallel-tutorial.md` describes running tickets in parallel via `claude -w <slug>` worktrees (created under `.claude/worktrees/`, branch from `origin/<default>` unless `worktree.baseRef` is `head`).

## Gotchas

- Spring Boot 4 moved test infra; see `docs/spring-boot-4-notes.md` before touching tests: `@WebMvcTest` package is `org.springframework.boot.webmvc.test.autoconfigure`; use `@MockitoBean`; `TestRestTemplate` needs `@AutoConfigureTestRestTemplate` and the `spring-boot-starter-restclient` test dep, and follows redirects by default.
- `.mcp.json` defines `openapi-mcp` (local path, disabled in `.claude/settings.local.json`).
