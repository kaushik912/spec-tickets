# Large task workflow: ticket-split + next-ticket + ticket-spec

Goal: build something big (e.g. ecommerce store, frontend + backend) with flaky network and precious context. Resume any time in a fresh session with `/next-ticket`.

Principle: **disk is memory, session is disposable.** Divide and conquer + ticket records on disk.

Skills live in the repo-root `.agents/skills/` directory:

| Skill | Job |
|-------|-----|
| `ticket-split` | Big request -> interview -> slice table -> `docs/spec.md`, `docs/api-contract.md`, `tickets/*.yaml`, `tickets/INDEX.md`, commit on `epic/<slug>` |
| `next-ticket` | Pick next ready ticket from INDEX, run it via `ticket-spec` in a worktree, merge, mark done. One ticket per call |
| `ticket-spec` | Run ONE complete ticket: spec -> plan -> tasks -> implement, commit per task. Unchanged |
| `spec` | Required by ticket-spec (`../spec/SKILL.md`) |

Brainstorming happens once, in `ticket-split`. Tickets come out complete, so `ticket-spec` never re-interviews.

## Layout

```
docs/spec.md             problem, goal, non-goals, stack, slices
docs/api-contract.md     endpoints/schemas/errors (or openapi.yaml)
tickets/TICKET-00N.yaml  ticket-spec schema, every field filled
tickets/INDEX.md         id|slug|title|status|depends_on|branch|worktree|attempts
tickets/PAUSED           (optional) touch to stop next-ticket
.agents/worktrees/<slug> or .claude/worktrees/<slug>   one worktree per ticket
```

- Status: `todo | doing | done | blocked`. Lives ONLY in INDEX.md (main checkout, epic branch).
- Code lives in worktrees; tracking lives in main checkout. Worktree branches never edit INDEX.
- `done` set only after merge into epic branch, so dependents see the code.

## Flow

1. `/ticket-split <request>` -> answer interview -> approve slice table -> check output.
2. `/clear`, `/next-ticket` -> runs one ticket (worktree from epic branch, ticket-spec inside).
3. Finish -> confirm merge (`--no-ff` into epic) -> worktree removed -> INDEX `done`.
4. Repeat 2-3. Crash/network drop: `/clear`, `/next-ticket` resumes the `doing` ticket from `.spec/<slug>/tasks.md`.
5. Merge epic branch into main once the whole thing works.

## next-ticket rules

- Select order: id arg > `doing` ticket > first `todo` with all `depends_on` done. None -> report why.
- `attempts` >= 3 -> `blocked`, human decides.
- `doing` with no activity > 30 min = stale; flagged, resumed anyway.
- Refuses if branch not `epic/*`, tree dirty, or `tickets/PAUSED` exists.
- Worktree: looked up via `git worktree list` by branch `worktree-<slug>`; reuses `.agents/` or `.claude/` path (e.g. from `claude -w`); creates `.agents/worktrees/<slug>` only if none. Gitignore both dirs.
- Merge conflict -> stop, leave `doing`, never auto-resolve.

## Ticket-split slice rules

- Vertical slices (capability through backend + frontend); split by layer only if too big for half a context.
- TICKET-001 = contract + scaffold; everything depends on it.
- ~3-6 tasks per ticket; dependencies only where code is needed.
- Recommend `auto_approve: true` (slices already approved at split time); `false` to review each plan.

## Parallel vs sequential

- Default sequential: `/next-ticket` picks one ready ticket per call.
- Parallel is opt-in: two terminals, `/next-ticket <id>` each, for independent tickets (deps `done`). Merge one at a time; conflicts only if they touch the same files.
- Force order: set `depends_on` in INDEX.md, or tell ticket-split ("frontend after backend").
- Frontend/backend in parallel works if both code against `docs/api-contract.md` only:
  - frontend runs against a **mock** (see below),
  - last ticket = **integration** (depends on both): real frontend vs real backend, catches contract drift.
- First attempts: force order backend -> frontend -> integration. Try parallel later.

## Mock ticket

A small ticket (or part of TICKET-001) that builds a fake backend from the contract: canned responses in contract shape (e.g. Prism from OpenAPI: `prism mock openapi.yaml` -> `localhost:4010`). Lets frontend tickets run/test before the real backend exists. Integration ticket repoints the frontend to the real backend. Skip it if backend is built first.

## Hand-test recipe (TodoMVC pilot, in scratchpad/todomvc)

1. `git init -b main`, `.gitignore` with `.agents/worktrees/` and `.claude/worktrees/`, initial commit (worktrees need a commit). Symlink `spec ticket-spec ticket-split next-ticket` from the repo-root `.agents/skills/` into `.claude/skills/`.
2. `claude`, then `/ticket-split Simple TodoMVC ... about 4 tickets`. Answers: Node+Express+JSON file, vanilla JS; testing_seam single; api_docs false; auto_approve true; non-goals auth/multi-user/DB.
3. Verify: branch `epic/*`, `git log`, `tickets/INDEX.md`, ticket YAMLs complete, contract covers every endpoint.
4. `/clear`, `/next-ticket`; crash test: Ctrl+C mid-ticket, restart, `/next-ticket` resumes same ticket.
5. Confirm merge; check `git log --graph`, `git worktree list`, INDEX `done`.
6. Repeat; optional: PAUSED test, parallel test.
7. Note friction: re-explaining, bad resumes, overlapping tickets, conflicts.

## Ideas borrowed from agency-os (reference_repos/agency-os)

Zombie detection, retry counter -> blocked ("stasis"), pause marker. Not taken: single long supervisor session, no worktrees, skip-permissions, UI-test agents. Possible later: independent QA/verify step after implement (ticket-spec self-verifies today).

## Status / open items

- Skills written but untested; pilot pending.
- Possible add: ticket-split proposes mock ticket or sequential order when splitting frontend + backend.
- Possible add: document `depends_on`/INDEX fields in ticket-spec schema docs (not required).
