---
status: approved
---
# Tasks: Simple banking system

- [x] Scaffold `bank/` package (`__init__.py`, empty `models.py`,
      `storage.py`, `bank.py`) and `tests/` dir with `unittest` runner
- [x] Write failing test: `create_account` returns a unique account id and
      `get_balance` returns the given starting balance (default 0)
- [x] Implement `Account`/`Transaction` models and `create_account`/
      `get_balance` to pass
- [x] Write failing test: `deposit` increases balance by the deposited amount
- [x] Implement `deposit` to pass
- [x] Write failing test: `deposit` rejects non-positive amounts with a clear
      error and leaves balance unchanged
- [x] Implement amount validation in `deposit` to pass
- [x] Write failing test: `withdraw` decreases balance when funds are
      sufficient
- [x] Implement `withdraw` success path to pass
- [x] Write failing test: `withdraw` raises a clear error on insufficient
      funds and leaves balance unchanged
- [x] Implement insufficient-funds check in `withdraw` to pass
- [x] Write failing test: `get_history` returns deposit/withdraw entries in
      order with type, amount, and timestamp
- [x] Implement transaction recording on deposit/withdraw to pass
- [x] Write failing test: balances and history persist across a simulated
      restart (new `BankSystem` constructed against the same file path)
- [x] Implement file persistence (`storage.py` load on construct, atomic
      save via temp file + `os.replace` after every mutation) to pass
- [x] Write failing test: operating on an unknown account id (`deposit`,
      `withdraw`, `get_balance`, `get_history`) raises a clear error
- [x] Implement account-id validation across all methods to pass
