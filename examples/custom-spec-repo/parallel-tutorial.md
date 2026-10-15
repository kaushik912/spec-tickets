# Dry run: parallel ticket-spec workflow

Tests: YAML tickets -> `claude -w <slug>` -> `/ticket-spec` -> unattended spec/plan/tasks/implement -> manual merge. App: `quote-board/` (Flask, in-memory, already scaffolded and passing 4 base tests). Two independent tickets already written: `quote-board/tickets/likes.yaml`, `quote-board/tickets/search-filter.yaml`.

All steps below are manual — run them yourself.

## 0. Prereqs

- `cd quote-board && python3 -m venv .venv && .venv/bin/pip install -r requirements.txt && .venv/bin/pytest -q` -> should show `4 passed`.
- Confirm the `ticket-spec` skill is present: `ls ~/.claude/skills/ticket-spec/SKILL.md` (already synced there for you).

## 1. Commit and push the base scaffold

`EnterWorktree`/`claude -w` create the new worktree branch from `origin/<default-branch>` by default (not your local uncommitted files), so the scaffold has to be on the remote before you branch off it:

```bash
cd examples/custom-spec-repo   # from the spec-tickets repo root
git add quote-board parallel-tutorial.md
git commit -m "Add quote-board dry-run app and tickets"
git push
```

(If you'd rather not push: add `{"worktree": {"baseRef": "head"}}` to `.claude/settings.local.json` instead, so the worktree branches from your local HEAD. Skip the `git push` if you do this.)

## 2. Kick off ticket 1 — likes (Terminal A)

```bash
cd examples/custom-spec-repo   # from the spec-tickets repo root
claude -w likes
```

This creates `.claude/worktrees/likes/` on branch `worktree-likes` and drops you into a session already inside it. First launch in a new worktree may ask you to trust the folder — accept.

Inside that session:

```
/ticket-spec quote-board/tickets/likes.yaml
```

With `auto_approve: true` in the ticket, it should run spec -> plan -> tasks -> implement without stopping for approval, TDD'ing the like/unlike endpoints. Let it run to "feature complete."

## 3. Kick off ticket 2 — search-filter (Terminal B, in parallel)

Open a **second terminal**, same repo root:

```bash
cd examples/custom-spec-repo   # from the spec-tickets repo root
claude -w search-filter
```

Inside that session:

```
/ticket-spec quote-board/tickets/search-filter.yaml
```

This should run alongside Terminal A untouched — different worktree, different branch, different `.spec/search-filter/` dir, so no cross-talk.

## 4. Watch / verify each run

Per worktree, once its session reports done:

```bash
cat .claude/worktrees/likes/.spec/likes/tasks.md          # all [x]?
cd .claude/worktrees/likes/quote-board && ../../.venv/bin/pytest -q 2>/dev/null || (python3 -m venv .venv && .venv/bin/pip install -r requirements.txt -q && .venv/bin/pytest -q)
```

(same for `search-filter`). Confirm the new endpoints/query params work as the ticket's `acceptance` list describes.

## 5. Review and merge back

`ticket-spec` commits after every task automatically (no `auto_commit` toggle — it's unconditional, since it's just a local worktree branch), so both branches already carry real commits by the time a run reports done — no manual commit step needed here.

```bash
cd examples/custom-spec-repo   # from the spec-tickets repo root
git log --oneline master..worktree-likes
git log --oneline master..worktree-search-filter
git diff master worktree-likes -- quote-board/app.py
git diff master worktree-search-filter -- quote-board/app.py
```

Then merge both (both touch `quote-board/app.py` in different functions — expect at most a trivial conflict, worth seeing how that resolves in this workflow):

```bash
git checkout master
git merge worktree-likes
git merge worktree-search-filter   # resolve any conflict here if it comes up
.venv/bin/pytest -q quote-board    # re-verify combined
```

## 6. Cleanup

```bash
git worktree remove .claude/worktrees/likes
git worktree remove .claude/worktrees/search-filter
git branch -d worktree-likes worktree-search-filter
```

## What this is testing

- Ticket YAML -> fully unattended spec/plan/tasks/implement (no interview, no approval stalls) via `auto_approve: true`.
- `claude -w <slug>` giving each ticket a deterministically-named, isolated worktree — not `EnterWorktree`'s random-name default.
- Two agents running genuinely in parallel (separate terminals/processes) with zero shared mutable state (`.spec/<slug>/` scoped per worktree).
- Whether the resumability contract actually holds if you Ctrl-C a session mid-task and re-run `claude -w <slug>` + `/ticket-spec ...` — worth trying deliberately on one of the two.
