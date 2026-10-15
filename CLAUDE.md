# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this repo is

Showcase of a spec-first, ticket-driven workflow for coding agents: the skills (`.agents/skills/`, symlinked from `.claude/skills/`) plus `examples/` snapshots of projects built with them. Not an app; no root build, lint or test. See `README.md` for the workflow, artifact formats and examples table.

## Structure

- `.agents/skills/` holds the 7 workflow skills (`spec`, `ticket-split`, `next-ticket`, `ticket-spec`, `ticket-fix`, `e2e-grill`, `e2e-implement`). They are installed copies managed by `skill-sync`; `skills-lock.json` tracks hashes. Source of truth is `my-claude-lib/skills/` outside this repo, so edit there then `skill-sync pull`, not here.
- `examples/` has independent projects, each with own build; `cd` into one first. No shared tooling between them.
  - `todomvc-ticketsplit` (flagship epic: Spring Boot + React): `cd backend && ./mvnw test`, `cd frontend && npm test`; has a `Makefile`.
  - `ecommerce-checkout` (Spring Boot 4): `./mvnw test` / `make test`; single test `-Dtest=Class#method`. Has own `CLAUDE.md`.
  - `ticket-spec-demo` (Python): has `Makefile` and own `CLAUDE.md`.
  - `custom-spec-repo`: `tinyurl/`, `todo-app/` (Maven wrapper) and `quote-board/` (Flask, `pytest`); has own `CLAUDE.md`.
- Examples were copied without git history and without build output (`target/`, `node_modules/`, `.venv/`); build before running.

## Workflow artifacts (shared conventions across examples)

- `tickets/TICKET-NNN.yaml`: input to `ticket-spec`. `tickets/INDEX.md` is the only place ticket status (`todo|doing|done|blocked`) lives, edited on the `epic/<slug>` branch in the main checkout, never from a worktree.
- `.spec/<slug>/{spec,plan,tasks}.md`: per-ticket artifacts; the `tasks.md` checkbox list is the resume point.
- Tickets run in worktrees on branch `worktree-<slug>`, merged `--no-ff` into the epic branch before marked `done`.
- Skill dependency: `ticket-spec` reads `../spec/SKILL.md`, so `spec` must be installed with it.

## Repo rules

- Fresh repo kept as a single squashed commit; no remote yet. Do not push without asking.
- Keep personal paths (`/home/...`) and per-user files (`.claude/settings.local.json`, `*.db`, worktrees) out of the tree; `.gitignore` covers the build/venv dirs.
