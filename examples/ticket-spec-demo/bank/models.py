from dataclasses import dataclass, field
from decimal import Decimal


@dataclass
class Transaction:
    type: str
    amount: Decimal
    timestamp: str


@dataclass
class Account:
    id: str
    name: str
    balance: Decimal
    transactions: list[Transaction] = field(default_factory=list)
