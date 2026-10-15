import tempfile
import unittest
from decimal import Decimal
from pathlib import Path

from bank.bank import BankSystem
from bank.errors import InsufficientFundsError, InvalidAmountError, UnknownAccountError


class BankSystemTest(unittest.TestCase):
    def setUp(self):
        self._tmpdir = tempfile.TemporaryDirectory()
        self.addCleanup(self._tmpdir.cleanup)
        self.data_path = Path(self._tmpdir.name) / "bank_data.json"

    def make_bank(self):
        return BankSystem(self.data_path)

    def test_create_account_default_starting_balance_is_zero(self):
        bank = self.make_bank()
        account_id = bank.create_account("Alice")
        self.assertEqual(bank.get_balance(account_id), Decimal("0"))

    def test_create_account_with_starting_balance(self):
        bank = self.make_bank()
        account_id = bank.create_account("Bob", Decimal("100"))
        self.assertEqual(bank.get_balance(account_id), Decimal("100"))

    def test_create_account_ids_are_unique(self):
        bank = self.make_bank()
        id1 = bank.create_account("Alice")
        id2 = bank.create_account("Bob")
        self.assertNotEqual(id1, id2)

    def test_deposit_increases_balance(self):
        bank = self.make_bank()
        account_id = bank.create_account("Alice", Decimal("50"))
        bank.deposit(account_id, Decimal("25"))
        self.assertEqual(bank.get_balance(account_id), Decimal("75"))

    def test_deposit_rejects_non_positive_amount(self):
        bank = self.make_bank()
        account_id = bank.create_account("Alice", Decimal("50"))
        with self.assertRaises(InvalidAmountError):
            bank.deposit(account_id, Decimal("0"))
        with self.assertRaises(InvalidAmountError):
            bank.deposit(account_id, Decimal("-10"))
        self.assertEqual(bank.get_balance(account_id), Decimal("50"))

    def test_withdraw_decreases_balance(self):
        bank = self.make_bank()
        account_id = bank.create_account("Alice", Decimal("50"))
        bank.withdraw(account_id, Decimal("20"))
        self.assertEqual(bank.get_balance(account_id), Decimal("30"))

    def test_withdraw_rejects_insufficient_funds(self):
        bank = self.make_bank()
        account_id = bank.create_account("Alice", Decimal("50"))
        with self.assertRaises(InsufficientFundsError):
            bank.withdraw(account_id, Decimal("100"))
        self.assertEqual(bank.get_balance(account_id), Decimal("50"))

    def test_get_history_records_deposits_and_withdrawals_in_order(self):
        bank = self.make_bank()
        account_id = bank.create_account("Alice", Decimal("50"))
        bank.deposit(account_id, Decimal("25"))
        bank.withdraw(account_id, Decimal("10"))

        history = bank.get_history(account_id)

        self.assertEqual(len(history), 2)
        self.assertEqual(history[0].type, "deposit")
        self.assertEqual(history[0].amount, Decimal("25"))
        self.assertTrue(history[0].timestamp)
        self.assertEqual(history[1].type, "withdraw")
        self.assertEqual(history[1].amount, Decimal("10"))
        self.assertTrue(history[1].timestamp)

    def test_state_persists_across_restart(self):
        bank = self.make_bank()
        account_id = bank.create_account("Alice", Decimal("50"))
        bank.deposit(account_id, Decimal("25"))
        bank.withdraw(account_id, Decimal("10"))

        restarted_bank = self.make_bank()

        self.assertEqual(restarted_bank.get_balance(account_id), Decimal("65"))
        history = restarted_bank.get_history(account_id)
        self.assertEqual([t.type for t in history], ["deposit", "withdraw"])
        self.assertEqual([t.amount for t in history], [Decimal("25"), Decimal("10")])

    def test_operations_on_unknown_account_raise_clear_error(self):
        bank = self.make_bank()
        with self.assertRaises(UnknownAccountError):
            bank.get_balance("does-not-exist")
        with self.assertRaises(UnknownAccountError):
            bank.deposit("does-not-exist", Decimal("10"))
        with self.assertRaises(UnknownAccountError):
            bank.withdraw("does-not-exist", Decimal("10"))
        with self.assertRaises(UnknownAccountError):
            bank.get_history("does-not-exist")


if __name__ == "__main__":
    unittest.main()
