# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

Minimal Python banking library (no DB/framework/auth, single-user). Demo of ticket-driven workflow: `tickets/TICKET-001.yaml` -> `.spec/banking-system/{spec,plan,tasks}.md` -> implementation. Check these docs for acceptance criteria before changing behavior.

## Commands

- Test all: `make test` (runs `.venv/bin/python -m unittest discover -v`)
- Single test: `.venv/bin/python -m unittest tests.test_bank.<Class>.<test_name> -v`
- No linter/build configured; stdlib only.

## Architecture

- `bank/bank.py` `BankSystem(path)`: loads all accounts from JSON into memory at init; every mutation (`create_account`, `deposit`, `withdraw`) calls `_save()`, rewriting the whole file.
- `bank/storage.py`: JSON (de)serialization. `Decimal` stored as strings; atomic write via `.tmp` file + `os.replace`. Serialization is hand-written, so new `Account`/`Transaction` fields must be added to both `load` and `save`.
- `bank/models.py`: `Account`, `Transaction` dataclasses. Timestamps are UTC ISO strings.
- `bank/errors.py`: `BankError` base; `InvalidAmountError` (amount <= 0), `InsufficientFundsError`, `UnknownAccountError`.
- Amounts must be `Decimal`, never float. Non-goals: multi-currency, interest, auth, concurrency, transfers.
