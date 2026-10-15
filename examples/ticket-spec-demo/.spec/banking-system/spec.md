---
status: approved
---
# Simple banking system

## Problem
No system exists for managing bank accounts. Users need a way to create
accounts, move money in/out, and see what happened to their money over time.

## Goal
A simple banking system that supports creating accounts, depositing and
withdrawing money, persisting balances to a file so they survive restarts,
and recording transaction history per account.

## Non-goals
- Multi-currency support
- Interest calculation
- Authentication/authorization
- Concurrent/multi-user access handling
- Interbank transfers or external payment integration

## Acceptance criteria
- Can create a new account with a unique id/name and an optional starting balance
- Can deposit a positive amount into an account, increasing its balance
- Can withdraw a positive amount from an account only if funds are sufficient;
  insufficient funds is rejected with a clear error
- Account balances are persisted to a file and are correct after a restart
- Every deposit/withdraw is recorded as a transaction (type, amount, timestamp)
  and transaction history can be retrieved per account
