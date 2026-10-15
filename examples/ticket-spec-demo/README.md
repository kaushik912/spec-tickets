# ticket-spec-demo

Minimal Python banking library: create accounts, deposit/withdraw funds, persist state to JSON, track per-account transaction history.

## Why

Demo of a spec/ticket-driven workflow: `tickets/TICKET-001.yaml` → `.spec/banking-system/{spec,plan,tasks}.md` → implementation. No DB, no framework, single-user, no auth.

## Layout

```
bank/
  bank.py       BankSystem — create_account, deposit, withdraw, get_balance, get_history
  models.py     Account, Transaction dataclasses
  storage.py    JSON load/save (atomic write via tmp file + os.replace)
  errors.py     BankError, InvalidAmountError, InsufficientFundsError, UnknownAccountError
tests/
  test_bank.py  unittest suite covering all acceptance criteria
tickets/, .spec/  ticket + spec/plan/tasks docs driving the implementation
```

## Usage

```python
from pathlib import Path
from decimal import Decimal
from bank.bank import BankSystem

bank = BankSystem(Path("bank_data.json"))
acct = bank.create_account("Alice", Decimal("100"))
bank.deposit(acct, Decimal("25"))
bank.withdraw(acct, Decimal("10"))
bank.get_balance(acct)   # Decimal("115")
bank.get_history(acct)   # [Transaction(type, amount, timestamp), ...]
```

Amounts are `Decimal`. Invalid (≤0) amounts raise `InvalidAmountError`; over-withdrawal raises `InsufficientFundsError`; unknown account id raises `UnknownAccountError`. State is written to disk on every mutation, so balances/history survive process restarts.

## Test

```
make test
```

Runs `unittest discover` via the project's `.venv`.

## Non-goals

Multi-currency, interest, auth, concurrent/multi-user access, interbank transfers.
