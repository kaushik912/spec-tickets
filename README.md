# spec-tickets

A spec-first, ticket-driven workflow for coding agents. Big ask in, small resumable tickets out, each ticket built spec -> plan -> tasks -> TDD in its own git worktree.

**Principle: disk is memory, session is disposable.** Every decision lives in a file (`spec.md`, `tasks.md`, `INDEX.md`). Crash, `/clear`, or switch machines, then re-run `/next-ticket` and it resumes.

This repo holds the skills plus five worked examples.

## The workflow

```
big request
    |
    v
/ticket-split        interview once -> docs/spec.md, docs/api-contract.md,
                     tickets/TICKET-00N.yaml, tickets/INDEX.md   (branch epic/<slug>)
    |
    v
/next-ticket         pick next ready ticket from INDEX -> worktree-<slug>
    |
    v
/ticket-spec         spec -> plan -> tasks -> implement (TDD), commit per task
    |
    v
merge --no-ff into epic branch -> worktree removed -> INDEX status = done
    |
    v
repeat; merge epic into main when whole thing works
```

Small feature, no epic? Skip the split: write one ticket yaml and run `/ticket-spec tickets/TICKET-001.yaml`, or use `/spec` interactively.

## Skills

| Skill | Job |
|-------|-----|
| [`ticket-split`](.agents/skills/ticket-split/SKILL.md) | Big request -> interview -> slice table -> docs, tickets, INDEX, commit on `epic/<slug>` |
| [`next-ticket`](.agents/skills/next-ticket/SKILL.md) | Pick next ready ticket, run it via `ticket-spec` in a worktree, merge, mark done. One ticket per call |
| [`ticket-spec`](.agents/skills/ticket-spec/SKILL.md) | Run ONE ticket unattended: spec -> plan -> tasks -> implement, commit per task |
| [`spec`](.agents/skills/spec/SKILL.md) | Interactive, resumable spec/plan/tasks/TDD flow (used by `ticket-spec`) |
| [`ticket-fix`](.agents/skills/ticket-fix/SKILL.md) | Bug ticket: repro -> diagnosis -> tasks -> fix. No fix without a failing test |
| [`e2e-grill`](.agents/skills/e2e-grill/SKILL.md) | Interview to discover BDD scenarios, saved to `docs/scenarios.md` |
| [`e2e-implement`](.agents/skills/e2e-implement/SKILL.md) | Turn agreed scenarios into automated e2e tests |

## Artifacts

**Ticket** (`tickets/TICKET-00N.yaml`), every field filled so `ticket-spec` never re-interviews:

```yaml
id: TICKET-002
slug: add-list-todos
title: Add and list todos
problem: Users cannot create or see todos.
goal: Create a todo and see the list, end to end (API + UI).
non_goals: [Toggle/delete (TICKET-003)]
acceptance:
  - "POST /api/todos with valid title returns 201 ..."
stack_notes: Todo entity per docs/api-contract.md
testing_seam: layered     # single | layered
api_docs: true
auto_approve: true        # false = pause for plan review
```

**Per-ticket spec** (`.spec/<slug>/{spec,plan,tasks}.md`): `status: draft|approved` frontmatter; `tasks.md` is a `- [ ]` / `- [x]` checklist in TDD pairs (failing test, then implement). That checklist is the resume point.

**Index** (`tickets/INDEX.md`), the only place status lives:

```
| id | slug | title | status | depends_on | branch | worktree | attempts |
| TICKET-001 | scaffold | Contract + scaffold | done | - | worktree-scaffold | | 1 |
| TICKET-002 | add-list-todos | Add and list todos | done | TICKET-001 | worktree-add-list-todos | | 1 |
```

Status: `todo | doing | done | blocked`.

## Rules worth knowing

- `next-ticket` selects: explicit id, then `doing`, then first `todo` whose `depends_on` are all `done`.
- `attempts` >= 3 marks the ticket `blocked` for a human.
- `touch tickets/PAUSED` stops `next-ticket`.
- INDEX is edited only on the epic branch in the main checkout, never from a worktree.
- `done` is set only after the merge, so dependents see the code.
- Merge conflicts: stop, leave `doing`, never auto-resolved.
- TICKET-001 is always contract + scaffold; slices are vertical.

## Parallel

Sequential by default. For independent tickets, one terminal each:

```bash
claude -w likes          # terminal A
/ticket-spec quote-board/tickets/likes.yaml

claude -w search-filter  # terminal B
/ticket-spec quote-board/tickets/search-filter.yaml
```

Separate worktree, branch and `.spec/<slug>/` per ticket, so no cross-talk. Merge one at a time. Full dry run: [`examples/custom-spec-repo/parallel-tutorial.md`](examples/custom-spec-repo/parallel-tutorial.md).

## Examples

| Example | Stack | Shows |
|---------|-------|-------|
| [`todomvc-ticketsplit`](examples/todomvc-ticketsplit) | Spring Boot + React/Vite | **Flagship.** Full `ticket-split` -> 4 tickets -> `next-ticket` loop on an epic branch. Start with its [workflow notes](examples/todomvc-ticketsplit/large-task-ticketing-workflow.md) and [build steps](examples/todomvc-ticketsplit/TODO_BUILDING_STEPS.md) |
| [`ecommerce-checkout`](examples/ecommerce-checkout) | Spring Boot 4, H2 | Single ticket -> spec/plan/tasks with transaction-boundary design, Bruno smoke tests |
| [`ticket-spec-demo`](examples/ticket-spec-demo) | Python | Smallest end-to-end `ticket-spec` run (banking) |
| [`custom-spec-repo`](examples/custom-spec-repo) | Spring Boot 4, Flask | Earlier sandbox: `.spec/` per feature, parallel worktree tutorial, `e2e-grill`/`e2e-implement` on `todo-app` |

## Use it in your own project

```bash
# in your project, with at least one commit (worktrees need it)
mkdir -p .claude/skills
for s in spec ticket-spec ticket-split next-ticket; do
  ln -s /path/to/spec-tickets/.agents/skills/$s .claude/skills/$s
done
printf '.claude/worktrees/\n.agents/worktrees/\n' >> .gitignore
claude
```

Then:

```
/ticket-split <your big request>     # answer the interview, approve the slice table
/clear
/next-ticket                         # repeat until INDEX is all done
```

Add `ticket-fix` for bugs, `e2e-grill` + `e2e-implement` for end-to-end tests.

## Notes

- Examples are snapshots; each was copied without git history. Build output (`target/`, `node_modules/`, `.venv/`) is not included, so run each project's own build first.
