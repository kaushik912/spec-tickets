Using `scratchpad/todomvc` as the project dir, so scratchpad stays clean.

## Step 1: Set up the project (terminal)

```bash
mkdir -p ~/scratchpad/todomvc && cd ~/scratchpad/todomvc
git init -b main
printf '.agents/worktrees/\n' > .gitignore
git add . && git commit -m "init"

mkdir -p .claude/skills
for s in spec ticket-spec ticket-split next-ticket; do
  ln -s <path-to-this-repo>/.agents/skills/$s .claude/skills/$s
done
ls -l .claude/skills
```

Worktrees need at least one commit, which is why the init commit is there. `ticket-spec` reads `../spec/SKILL.md`, so `spec` has to be linked too.

## Step 2: Start Claude

```bash
claude
```

Check that `/ticket-split` and `/next-ticket` show up in the slash menu. If they don't, restart `claude`.

## Step 3: Split the epic

Type:
```
/ticket-split Simple TodoMVC app: add, complete, delete todos; filter all/active/completed; clear completed. Backend REST API + frontend. Keep it to about 4 tickets.
```

Answer the interview with these:
- **Stack:** Node + Express with a JSON-file store for the backend. Vanilla JS, one HTML page, for the frontend.
- **testing_seam:** `single`
- **api_docs:** `false`
- **auto_approve:** `true`
- **Non-goals:** auth, multi-user, a database.

At the slice table, push for something like:
1. Contract + scaffold
2. Todos API (CRUD)
3. Frontend list/add/complete/delete
4. Filters + clear completed

Approve the table.

## Step 4: Check the output (second terminal)

```bash
cd ~/scratchpad/todomvc
git branch --show-current     # epic/<slug>
git log --oneline             # init + epic commit
ls docs tickets
cat tickets/INDEX.md          # all todo, depends_on set, TICKET-001 root
cat tickets/TICKET-002.yaml   # all fields filled, non_goals name neighbours
```

Pass criteria: every ticket YAML is complete, and the contract names every endpoint the tickets mention. If either fails, note it and tell Claude to fix it.

## Step 5: Run ticket 1

In Claude, type `/clear`, then `/next-ticket`. Expect:
- `INDEX.md` shows TICKET-001 as `doing`, with a branch and a worktree.
- It does **not** interview you again.
- It runs spec → plan → tasks → implement, with commits per task.

Meanwhile, in terminal 2:
```bash
git worktree list
git -C .agents/worktrees/<slug> log --oneline
```

## Step 6: Crash test (the main point)

While TICKET-001 is mid-implementation, hit `Ctrl+C` to kill Claude. Then:
```bash
claude
```
Type `/next-ticket`. Expected: it picks the same `doing` ticket, resumes from `.spec/<slug>/tasks.md`, and doesn't redo finished tasks.

## Step 7: Close the ticket

When it finishes, `/next-ticket` asks you to confirm the merge. Say yes. Then check:
```bash
git log --oneline --graph      # merge commit on epic branch
git worktree list              # worktree removed
cat tickets/INDEX.md           # TICKET-001 done
```

## Step 8: Repeat

`/clear`, then `/next-ticket` for each remaining ticket. Test the dependency rule: TICKET-002 and 003 should only start after 001 is `done`.

## Optional extras
- **Pause:** `touch tickets/PAUSED`, then `/next-ticket` should refuse. `rm tickets/PAUSED` afterwards.
- **Parallel:** after 001, open two terminals and run `/next-ticket TICKET-002` in one and `/next-ticket TICKET-003` in the other. Merge them one at a time.
- **Final check:** start the app on the epic branch and click through it.
---

TICKET-003 (toggle-delete) and TICKET-004 (filter-clear) can both start now. 
/next-ticket runs 003 next.
 To run them in parallel, use /next-ticket TICKET-004 in a separate session.

### Ticket enhancements
- Add DB persistence instead of pure in-memory
- Add request logging filter (to check whats coming-in for every controller request), i don't want it to be in the controller code if it can be done via filter or AOP patterns.
- Cost / Token tracking per task and also ticket-stats skills that reads these and tells the cost incurred
- 
