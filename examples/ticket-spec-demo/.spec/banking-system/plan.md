---
status: approved
---
# Plan: Simple banking system

## Stack
No existing code in the repo. Python 3, standard library only (no external
runtime deps): `json`, `decimal`, `datetime`, `pathlib`. Matches
`stack_notes` (minimal, no DB, no framework).

## Architecture
- `bank/models.py` — `Account`, `Transaction` dataclasses; (de)serialization
  to/from plain dicts for JSON storage.
- `bank/storage.py` — load/save the whole data file as JSON. Atomic write
  (write to a temp file, `os.replace` into place) so a crash mid-write can't
  corrupt the data file.
- `bank/bank.py` — `BankSystem`, the public API and the test seam. Constructed
  with a file path; loads existing state from that file on construction
  (or starts empty if the file doesn't exist) and saves after every mutating
  call. Re-constructing a `BankSystem` against the same path simulates an
  app restart.
- Data file: single JSON file, `{ accounts: { <id>: { name, balance,
  transactions: [ {type, amount, timestamp}, ... ] } } }`. Balances and
  transaction amounts stored as strings (via `Decimal`) to avoid float
  precision issues.

## Public API (the seam)
- `BankSystem(path)`
- `create_account(name, starting_balance=Decimal("0")) -> account_id`
- `deposit(account_id, amount)`
- `withdraw(account_id, amount)` — raises on insufficient funds
- `get_balance(account_id) -> Decimal`
- `get_history(account_id) -> list[Transaction]`

## Testing seam
Single seam (per ticket): every behavior test drives `BankSystem` end to
end through its public methods — real file I/O against a temp directory
per test, no mocking of `storage.py`. Persistence-across-restart is tested
by constructing a second `BankSystem` against the same file path and
asserting balance/history are unchanged. Assertions land on return values
and on-disk file content, never on internal calls. Test runner: stdlib
`unittest` (no external test framework, consistent with stdlib-only stack).

## API docs
Not applicable — no HTTP API in this feature (per ticket: `api_docs: false`).

## Risks
- Float precision on money — mitigated by using `Decimal` everywhere and
  serializing as strings.
- Partial/corrupt writes on crash — mitigated by atomic write (temp file +
  `os.replace`).
- No concurrency/locking — explicit non-goal, single-process use assumed.
