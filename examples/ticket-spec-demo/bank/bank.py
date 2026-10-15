import uuid
from datetime import datetime, timezone
from decimal import Decimal

from bank import storage
from bank.errors import InsufficientFundsError, InvalidAmountError, UnknownAccountError
from bank.models import Account, Transaction


class BankSystem:
    def __init__(self, path):
        self.path = path
        self._accounts: dict[str, Account] = storage.load(path)

    def create_account(self, name, starting_balance=Decimal("0")):
        account_id = str(uuid.uuid4())
        self._accounts[account_id] = Account(
            id=account_id, name=name, balance=starting_balance
        )
        self._save()
        return account_id

    def get_balance(self, account_id):
        return self._get_account(account_id).balance

    def deposit(self, account_id, amount):
        if amount <= 0:
            raise InvalidAmountError("deposit amount must be positive")
        account = self._get_account(account_id)
        account.balance += amount
        self._record(account, "deposit", amount)
        self._save()

    def withdraw(self, account_id, amount):
        if amount <= 0:
            raise InvalidAmountError("withdraw amount must be positive")
        account = self._get_account(account_id)
        if amount > account.balance:
            raise InsufficientFundsError("insufficient funds for withdrawal")
        account.balance -= amount
        self._record(account, "withdraw", amount)
        self._save()

    def get_history(self, account_id):
        return list(self._get_account(account_id).transactions)

    def _get_account(self, account_id):
        try:
            return self._accounts[account_id]
        except KeyError:
            raise UnknownAccountError(f"no account with id {account_id!r}")

    def _record(self, account, type_, amount):
        timestamp = datetime.now(timezone.utc).isoformat()
        account.transactions.append(Transaction(type_, amount, timestamp))

    def _save(self):
        storage.save(self.path, self._accounts)
