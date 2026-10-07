# Banking Transaction Processing System
## Object-Oriented Design & Implementation

**Project:** BankTransactionSystem
**Package:** `org.beltran`
**Language/Platform:** Java 25, Maven
**Entry point:** `Main.java`

This document describes the object-oriented refactor of the original procedural
banking program. Each section corresponds to one activity of the design exercise.

---

## Activity 1 – Identify Classes and Objects

The original Day-2 program was a single procedural class (`Main`) that held all
accounts as string maps, kept a global transaction log, and implemented
authentication, validation, and money movement as static methods.

The major objects in the banking system were identified and narrowed down to the
classes that are actually necessary:

| Class                  | Reason it is necessary                                                        |
|------------------------|------------------------------------------------------------------------------|
| `Customer`             | Represents a bank customer; the account holder. Enables the *owns* relationship. |
| `Account`              | Domain entity that holds the money. Base class for account types.            |
| `SavingsAccount`       | An account type with a stricter rule: single transactions capped at PHP 50,000.00. |
| `CheckingAccount`      | An account type with no per-transaction cap beyond the balance.              |
| `Transaction`          | A single ledger entry: what happened to an account, when, and the resulting balance. |
| `TransactionType`      | Enum describing the kind of transaction (deposit, withdrawal, transfer in/out). |
| `Bank`                 | Owns the accounts and the ledger; the facade for all banking operations.     |
| `AuthenticationService`| Owns the sign-in business rules (credential check plus lockout policy).      |
| `OperationResult`      | Carries the outcome of an operation (success flag, message, balance, account). |
| `Format`               | Shared formatting helpers (currency, padding, rounding, repeating characters). |
| `Main`                 | Console/UI layer: menu loop, prompts, and display of inquiry/history.        |

`Customer`, `Account`, `Transaction`, `Bank`, `AuthenticationService` were all
retained from the exercise list. A separate `TransactionService` was considered
but **deliberately excluded**: the necessary transaction logic is already owned
by `Bank` (orchestration) and `Account` (balance mutation), so a third service
layer would be redundant.

**Result:** one package `org.beltran` containing 11 Java source files.

---

## Activity 2 – Identify Attributes and Behaviors

For each class, the information it maintains (attributes) and the actions it
performs (behaviors) are listed below.

### Customer

| Attributes           | Behaviors                |
|----------------------|--------------------------|
| `customerId` (String) | `getCustomerId()`        |
| `name` (String)       | `getName()`              |

### Account (abstract base)

| Attributes                    | Behaviors                                             |
|-------------------------------|-------------------------------------------------------|
| `number` (String)             | `getNumber()`                                         |
| `owner` (Customer)            | `getOwner()` / `getHolderName()`                      |
| `pin` (String)                | `matchesPin(candidate)`                               |
| `balance` (double)            | `getBalance()` `deposit(amount)` `withdraw(amount)`   |
| `failedLoginAttempts` (int)   | `getFailedLoginAttempts()` `recordFailedLogin()` `resetFailedLogins()` |
| `locked` (boolean)            | `isLocked()` `lock()`                                 |
| `active` (boolean)            | `isActive()`                                          |
| type-specific limit           | `maxWithdrawable()` (abstract)                        |
| account type label            | `getAccountType()` (abstract)                         |
| status label                  | `getStatusLabel()`                                    |

### SavingsAccount (extends Account)

| Attributes                          | Behaviors                 |
|-------------------------------------|---------------------------|
| `MAX_PER_TRANSACTION = 50,000.00`   | `getAccountType()` -> "SAVINGS" |
| (inherits all Account attributes)   | `maxWithdrawable()` = `min(balance, 50,000.00)` |

### CheckingAccount (extends Account)

| Attributes                        | Behaviors                 |
|-----------------------------------|---------------------------|
| (inherits all Account attributes) | `getAccountType()` -> "CHECKING" |
|                                   | `maxWithdrawable()` = `balance` |

### Transaction

| Attributes                                   | Behaviors                              |
|----------------------------------------------|----------------------------------------|
| `accountNumber` (String)                     | `getAccountNumber()`                   |
| `type` (TransactionType)                     | `getType()` / `getType().getLabel()`   |
| `amount` (double)                            | `getAmount()`                          |
| `balanceAfter` (double)                      | `getBalanceAfter()`                    |
| `timestamp` (LocalDateTime)                  | `getTimestamp()` / `getTimestampFormatted()` |
| `counterparty` (String)                      | `getCounterparty()`                    |

### TransactionType (enum)

| Constants                              | Behaviors         |
|----------------------------------------|-------------------|
| `DEPOSIT` ("DEPOSIT")                   | `getLabel()`      |
| `WITHDRAWAL` ("WITHDRAWAL")             |                   |
| `TRANSFER_IN` ("TRANSFER-IN")           |                   |
| `TRANSFER_OUT` ("TRANSFER-OUT")         |                   |

### Bank

| Attributes                              | Behaviors                                                       |
|-----------------------------------------|-----------------------------------------------------------------|
| `accounts` (List<Account>)              | `findAccount(number)`                                           |
| `ledger` (List<Transaction>)            | `validateAccount(number)` `validateAmount(amount)`              |
| `MAX_FAILED_ATTEMPTS = 3`               | `deposit(number, amount)` `withdraw(number, amount)`            |
| `MAX_SINGLE_AMOUNT = 1,000,000.00`      | `transfer(source, destination, amount)` `transactionsFor(number)` |
| (seed data in constructor)              | `postTransaction(...)` (private) `seedAccounts()` (private)     |

### AuthenticationService

| Attributes           | Behaviors                                 |
|----------------------|-------------------------------------------|
| `bank` (Bank)        | `authenticate(accountNumber, pin)`        |

### OperationResult

| Attributes                 | Behaviors                        |
|----------------------------|----------------------------------|
| `success` (boolean)        | `isSuccess()`                    |
| `message` (String)         | `getMessage()`                   |
| `balanceAfter` (Double, nullable) | `getBalanceAfter()`       |
| `account` (Account, nullable)     | `getAccount()`            |
| (value object)             | `ok(...)` / `fail(...)` factories |

### Format (utility)

| Behaviors                          |
|------------------------------------|
| `roundToCentavos(value)`           |
| `formatPeso(value)` -> "PHP 1,000,000.00" |
| `pad(text, width)`                 |
| `padRight(text, width)`            |
| `repeat(char, times)`              |

### Main (console/UI)

| Attributes                  | Behaviors                                             |
|-----------------------------|-------------------------------------------------------|
| `bank` (Bank)               | `main(args)` (bootstraps the application)             |
| `authenticationService` (AuthenticationService) | `run()` (banner, sign-in, menu loop) |
| `console` (Scanner)         | `signIn()` `displayMenu()` `parseMenuChoice(choice)`  |
| menu option constants       | `readAmount(prompt)` `showAccountInquiry(account)`    |
| `MAX_LOGIN_ROUNDS = 5`      | `showTransactionHistory(account)`                     |

---

## Activity 3 – Create the Class Design

The class diagram below matches the implemented program (flat package `org.beltran`).

```
                        +------------------+
                        |      Main        |
                        | (console / UI)   |
                        +--------+---------+
                                 | 1
                                 | uses
                 +---------------+---------------+
                 |                               |
                 v 1                             v 1
        +------------------+            +----------------------+
        |      Bank        |            | AuthenticationService|
        |------------------|            |----------------------|
        | accounts         |            | bank                 |
        | ledger           |            | authenticate()       |
        |------------------|            +----------------------+
        | findAccount()    |
        | validateAccount()|
        | validateAmount() |
        | deposit()        |
        | withdraw()       |
        | transfer()       |
        | transactionsFor()|
        +--------+---------+
                 | 1
                 |
        +--------+------------------------------------+
        |                * (composition)              |
        v                                            v
+----------------+                        +-------------------+
|    Account     | 1  *                *  |    Transaction    |
|  (abstract)    |------ records -------> |-------------------|
|----------------|                        | accountNumber     |
| number         |                        | type              |
| owner  *---> Customer                   | amount            |
| pin            |                        | balanceAfter      |
| balance        |                        | timestamp         |
| failedLogins   |                        | counterparty      |
| locked         |                        +-------------------+
| active         |
|----------------|
| deposit()      |
| withdraw()     |
| maxWithdrawable()          (abstract)
| getAccountType()           (abstract)
| matchesPin()  recordFailedLogin()
| resetFailedLogins()  lock()
+--------+-------+
         |
         | generalizes  (inheritance)
         |
   +-----+-------+
   |             |
   v             v
+----------------+      +------------------+
| SavingsAccount |      | CheckingAccount  |
|----------------|      |------------------|
| maxWithdrawable|      | maxWithdrawable  |
|  = min(balance,|      |  = balance       |
|    50,000.00)  |      |                  |
+----------------+      +------------------+

TransactionType (enum): DEPOSIT, WITHDRAWAL, TRANSFER-IN, TRANSFER-OUT
OperationResult:  used as the return type of service-layer operations
Format:           static formatting helpers
```

### Multiplicity

| Relationship            | Cardinality | Category     |
|-------------------------|-------------|--------------|
| Bank -- owns -> Account | 1 to many   | Composition  |
| Bank -- owns -> Transaction (ledger) | 1 to many | Composition |
| Account -- records -> Transaction    | 1 to many   | Association  |
| Account -- owned by -> Customer      | many to 1   | Association  |
| AuthenticationService -- uses -> Bank | 1 to 1     | Dependency   |
| Main -- uses -> Bank / AuthenticationService | 1 to 1 | Dependency |

---

## Activity 4 – Apply Encapsulation

Object data is protected by making fields `private` and exposing carefully
controlled access through methods. No part of the application can freely modify
another object's data.

### Protected data and how it is changed

| Data                       | Access                       | Mutation method(s)               |
|----------------------------|------------------------------|----------------------------------|
| `Account.balance`          | `getBalance()` (read-only)   | `deposit()` / `withdraw()`       |
| `Account.pin`              | `matchesPin()` (comparison)  | none (immutable after creation)  |
| `Account.failedLoginAttempts` | `getFailedLoginAttempts()` | `recordFailedLogin()` / `resetFailedLogins()` |
| `Account.locked`           | `isLocked()`                 | `lock()` (only after 3rd failure) |
| `Account.active`           | `isActive()`                 | none after construction          |
| `Bank.accounts`            | not exposed directly          | only through `Bank` operations   |
| `Bank.ledger`              | `transactionsFor()` (filtered copy) | `postTransaction()` (private) |

### Key points

- **Balance is never set directly.** Outside code calls `Bank.deposit(...)`,
  which invokes `Account.deposit(amount)`; the balance is updated only inside
  the account through round-to-centavo arithmetic.
- **Withdrawals are guarded inside the account.** An account refuses a
  withdrawal when it is LOCKED, when it is INACTIVE, when the amount exceeds
  the balance, or when the amount exceeds the account type's limit.
- **PIN is opaque.** Verification is done by `matchesPin(candidate)`, so the
  stored PIN never leaks to callers.
- **The bank's collections are private.** Accounts and the ledger cannot be
  modified from the console layer; `Main` works only through the `Bank` facade
  and object getters.

---

## Activity 5 – Apply Abstraction

Implementation details that other parts of the program do not need to know are
hidden behind clear, operation-level interfaces.

### `Account` as an abstract operation layer
Callers see a small business interface — `deposit()`, `withdraw()`,
`getBalance()`, `getStatusLabel()`, `maxWithdrawable()` — and never the storage
details (how the balance is stored, how rounding works, how status is computed).

### `Bank` as a facade
`Main` interacts with `Bank` through high-level operations only:
`deposit()`, `withdraw()`, `transfer()`, `transactionsFor()`. The internal
lookup of accounts, the validation of account state and amount limits, the seed
data loading, and the transaction recording are all hidden.

### `OperationResult` as the result interface
All service-layer operations return an `OperationResult` (`success`, `message`,
and optional data). The console layer only needs to print `getMessage()` or
branch on `isSuccess()`; it does not need to know *why* an operation succeeded
or failed internally.

### Menu/IO abstraction
`Main` owns the keyboard prompts, the menu loop, and the table rendering, so the
domain classes stay free of console output concerns. Formatting (currency,
column padding, rounding) is centralized in `Format`.

---

## Activity 6 – Apply Inheritance and Polymorphism

Inheritance is used only where it provides a meaningful solution: account types
with genuinely different behavior. It is not forced onto the design.

### The hierarchy

```
Account (abstract)
  |- SavingsAccount
  |- CheckingAccount
```

### Polymorphic behavior

| Abstract member          | SavingsAccount                          | CheckingAccount            |
|--------------------------|-----------------------------------------|----------------------------|
| `maxWithdrawable()`      | `min(balance, 50,000.00)`               | `balance`                  |
| `getAccountType()`       | `"SAVINGS"`                             | `"CHECKING"`               |

- The `Bank` holds a `List<Account>` and treats every account polymorphically.
  It never checks the concrete type; the correct limit is enforced by the
  account itself through `maxWithdrawable()`.
- `deposit()` and `withdraw()` are inherited from `Account`. `withdraw()`
  consults the overridden `maxWithdrawable()`, so a Savings withdrawal above
  PHP 50,000.00 is rejected while a CheckingAccount can withdraw down to the
  full balance.
- `getAccountType()` is displayed in the **Account / Balance Inquiry** screen,
  demonstrating the attribute in the user interface.

### Why this is meaningful
- A savings account is realistically subject to a stricter per-transaction
  limit; a checking account has different, less restrictive rules.
- Adding a new account type in the future means adding one subclass and its
  `maxWithdrawable()`/`getAccountType()` overrides — no changes to `Bank`.

---

## Activity 7 – Apply Composition and Object Relationships

Objects interact through the relationships below, shown with their categories.

### Association

```
Customer 1 <---------- * Account        (an account is owned by one customer;
                                         a customer may own many accounts)
Account  1 ------------ * Transaction    (an account records many transactions)
AuthenticationService  ----->  Bank       (the service depends on the bank's
                                          account lookup)
```

### Aggregation / Composition

```
Bank 1 ---------- * Account
        composition: the Bank creates its accounts in its constructor
        (seedAccounts()); the accounts have no independent lifecycle apart
        from the Bank that owns them. When the Bank is destroyed, its
        accounts are destroyed with it.

Bank 1 ---------- * Transaction
        composition: the ledger is a private collection created and
        maintained entirely by the Bank (postTransaction()). Entries are
        immutable; they are only added through the Bank.
```

### How objects collaborate at runtime

1. `Main.main()` creates a `Bank` (which seeds its accounts) and an
   `AuthenticationService`, then starts the console loop.
2. `Main.signIn()` asks `AuthenticationService.authenticate(...)` to validate a
   customer; on success it receives the signed-in `Account`.
3. Each menu action calls `Bank.deposit / withdraw / transfer`, which validates,
   delegates the money movement to the account(s), and records `Transaction`
   objects in the ledger.
4. Inquiry actions read account getters; history renders `Bank.transactionsFor()`
   entries.

---

## Activity 9 – Test the OOP Application

Run the program:

```
set JAVA_HOME=C:\Program Files\Java\jdk-25.0.4.1
mvn -q compile
java -cp target/classes org.beltran.Main
```

Seed data (PIN = account number):

| Account | Customer | Type     | PIN  | Initial Balance | Active |
|--------:|:---------|:---------|:----:|----------------:|:-------|
| 1001    | Juan Dela Cruz | Checking | 1001 | PHP 25,000.00  | yes    |
| 1002    | Maria Santos   | Savings  | 1002 | PHP 50,000.00  | yes    |
| 1003    | Pedro Reyes    | Savings  | 1003 | PHP 10,000.00  | no     |

### 1. Authentication

- [ ] Correct account number and PIN (e.g., `1001` / `1001`) signs in with a
      welcome message.
- [ ] Non-existent account number is rejected: "Account ... does not exist."
- [ ] Blank account number / blank PIN are rejected with a required-field message.
- [ ] Wrong PIN is rejected and counts down the attempts (Attempt 1 of 3, 2 of 3).
- [ ] After 3 wrong PINs the account becomes LOCKED and further logins are
      rejected, even with the correct PIN.
- [ ] A successful login resets the failed-attempt counter back to 0.
- [ ] Inactive account `1003` can still sign in and view data.

### 2. Account Inquiry

- [ ] Menu option 1 shows Account Number, Holder Name, Account Type, Status,
      Failed Attempts, and Balance.
- [ ] ACTIVE account shows status "ACTIVE"; INACTIVE shows "INACTIVE"; a locked
      account shows "LOCKED".

### 3. Balance Inquiry

- [ ] Menu option 2 shows the same account/balance screen as Account Inquiry.
- [ ] The balance displayed matches the last recorded balance after any
      deposits, withdrawals, or transfers.

### 4. Deposit

- [ ] A valid deposit increases the balance and prints the new balance.
- [ ] A deposit of 0 or negative is rejected: "Amount must be greater than 0.00."
- [ ] A deposit over PHP 1,000,000.00 is rejected.
- [ ] Text such as "PHP 5,000", "5000", or "5 000" (commas/PHP stripped) is
      accepted; a non-numeric entry is cancelled cleanly.
- [ ] A deposit to INACTIVE account `1003` is blocked.
- [ ] A deposit to a LOCKED account is blocked.

### 5. Withdrawal

- [ ] A valid withdrawal decreases the balance and prints the new balance.
- [ ] Withdrawing more than the balance is rejected with the available balance
      shown.
- [ ] Savings account (`1002`) rejects a single withdrawal above PHP 50,000.00;
      Checking account (`1001`) does not.
- [ ] Same amount validation applies (non-positive, above PHP 1,000,000.00).

### 6. Fund Transfer

- [ ] Transfer between two different accounts moves the amount; both balances
      are reported and both accounts record the transfer.
- [ ] Transfer to the same account is rejected.
- [ ] Transfer with insufficient source balance is rejected.
- [ ] Transfer to a non-existent destination is rejected.
- [ ] Savings cap applies to the transfer-out side (source).
- [ ] After a successful transfer, both the source (TRANSFER-OUT) and the
      destination (TRANSFER-IN) show the counterparty detail in history.

### 7. Transaction Recording

- [ ] Every successful deposit, withdrawal, and transfer creates a record.
- [ ] Each record contains: account number, type, amount, balance after,
      timestamp, and counterparty (for transfers).
- [ ] Failed operations (bad amount, insufficient funds, locked account, same
      account transfer) do NOT create a record.

### 8. Transaction History

- [ ] Menu option 6 lists only the current account's transactions, newest last.
- [ ] Columns show DATE / TIME, TYPE, AMOUNT, BALANCE, and counterparty.
- [ ] An account with no transactions shows "No transactions recorded yet."
- [ ] Balances in the history are consistent with the current balance.

### 9. Menu / Navigation

- [ ] Option 7 (Logout) ends the session with a logout message.
- [ ] Invalid menu input (non-numeric or out of range) shows the invalid
      selection message and re-prompts.
- [ ] After logout, the program prints the farewell message and exits.

---

## Summary

The procedural single-class banking program was refactored into a small
object-oriented design:

- **Classes by responsibility:** entities (`Customer`, `Account`,
  `SavingsAccount`, `CheckingAccount`, `Transaction`, `TransactionType`),
  services (`Bank`, `AuthenticationService`), result value object
  (`OperationResult`), formatting utility (`Format`), and console controller
  (`Main`).
- **Encapsulation:** balances, PINs, and account state are private and changed
  only through controlled methods.
- **Abstraction:** `Bank` and `Account` expose operation-level interfaces;
  the UI depends on results, not internals.
- **Inheritance/polymorphism:** Savings vs Checking accounts differ through
  `maxWithdrawable()` / `getAccountType()`.
- **Composition/relationships:** Bank composes accounts and the ledger; the
  customer–account relationship is a one-to-many association.
- **Behavior preserved:** authentication with lockout, inquiry, deposit,
  withdrawal, transfer, transaction recording, and history are unchanged
  (with the addition of the account-type rule and its display).