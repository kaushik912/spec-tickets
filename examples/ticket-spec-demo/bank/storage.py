import json
import os
from decimal import Decimal
from pathlib import Path

from bank.models import Account, Transaction


def load(path: Path) -> dict[str, Account]:
    if not path.exists():
        return {}

    with open(path) as f:
        raw = json.load(f)

    accounts = {}
    for account_id, data in raw.get("accounts", {}).items():
        transactions = [
            Transaction(
                type=t["type"],
                amount=Decimal(t["amount"]),
                timestamp=t["timestamp"],
            )
            for t in data["transactions"]
        ]
        accounts[account_id] = Account(
            id=account_id,
            name=data["name"],
            balance=Decimal(data["balance"]),
            transactions=transactions,
        )
    return accounts


def save(path: Path, accounts: dict[str, Account]) -> None:
    raw = {
        "accounts": {
            account_id: {
                "name": account.name,
                "balance": str(account.balance),
                "transactions": [
                    {
                        "type": t.type,
                        "amount": str(t.amount),
                        "timestamp": t.timestamp,
                    }
                    for t in account.transactions
                ],
            }
            for account_id, account in accounts.items()
        }
    }

    path.parent.mkdir(parents=True, exist_ok=True)
    tmp_path = path.with_suffix(path.suffix + ".tmp")
    with open(tmp_path, "w") as f:
        json.dump(raw, f, indent=2)
    os.replace(tmp_path, path)
