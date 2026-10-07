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

The class design of the banking system follows a clear object-oriented
structure, with relationships defined between entities, services, and utilities.
At the top level, the `Main` class acts as the console/UI layer. It is
responsible for bootstrapping the application, handling user input, and
displaying outputs. `Main` depends on the `Bank` and `AuthenticationService`
classes, delegating all business logic to them while keeping the user interface
separate.

The `Bank` class serves as the central facade for all banking operations. It
composes both `Account` objects and `Transaction` records, meaning it owns and
manages their lifecycle. The `Bank` validates accounts and amounts, orchestrates
deposits, withdrawals, and transfers, and records transactions in its ledger.
This ensures that all financial activity flows through a single, controlled
entry point.

The `AuthenticationService` class depends directly on the `Bank`. It handles
login validation, PIN checks, and enforces the lockout policy after failed
attempts. By separating authentication from transaction logic, the design
maintains a clean division of responsibilities.

The `Account` class is abstract and represents the core domain entity. Each
account is associated with a `Customer`, establishing a one-to-many relationship
where a customer may own multiple accounts. Accounts also record multiple
`Transaction` entries, creating a history of activity. The abstract methods
`maxWithdrawable()` and `getAccountType()` are implemented differently by
subclasses, allowing polymorphic behavior.

Two concrete subclasses extend `Account`: `SavingsAccount` and
`CheckingAccount`. A `SavingsAccount` enforces a per-transaction cap of
PHP 50,000.00, while a `CheckingAccount` allows withdrawals up to the full
balance. This hierarchy demonstrates meaningful inheritance, where different
account types enforce distinct rules without requiring changes to the Bank's
logic.

The `Customer` class represents the account holder and provides identity
attributes such as customer ID and name. It supports the ownership relationship
with accounts, ensuring that each account is tied to a specific customer.

The `Transaction` class represents a single ledger entry. Each transaction
records the account number, type, amount, balance after the operation,
timestamp, and counterparty (for transfers). Transactions are owned by the
`Bank` and associated with accounts, providing a complete audit trail of
financial activity.

The `TransactionType` enum defines the categories of transactions: Deposit,
Withdrawal, Transfer In, and Transfer Out. This ensures consistency and prevents
invalid transaction types from being introduced.

The `OperationResult` class encapsulates the outcome of operations. It is
returned by the `Bank` and `AuthenticationService` to indicate success or
failure, along with a message, balance after the operation, and optional account
data. This simplifies error handling and keeps the UI layer focused on
displaying results.

The `Format` utility class provides shared formatting helpers for currency,
padding, rounding, and text alignment. It is used by `Main`, `Bank`, and
`Account` to ensure consistent output formatting across the application.

### Class diagram

The diagram below matches the implemented program (flat package `org.beltran`).

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

Encapsulation was applied throughout the system to protect sensitive data and
enforce business rules. Each class hides its internal state by declaring fields
as private and exposing only controlled methods for access or modification.

### Account class

The `balance` field is private and can only be changed through `deposit()` and
`withdraw()`. These methods enforce rules such as rounding to centavos and
checking the account status before updating; withdrawals additionally verify
that the amount is within the balance and within the account type's
per-transaction limit. Amount validity itself (non-numeric, zero/negative, above
PHP 1,000,000.00) is enforced beforehand by `Bank.validateAmount()`.

The `pin` field is immutable and never exposed directly. Verification is done
only through `matchesPin(candidate)`, ensuring the actual PIN remains hidden.

Login security is encapsulated with `failedLoginAttempts`, tracked internally.
Methods like `recordFailedLogin()` and `resetFailedLogins()` manage this
counter, while `lock()` enforces account lockout after repeated failures.

### Bank class

The collections of accounts and ledger are private. External code cannot
directly manipulate them; instead, controlled methods like `findAccount(number)`
and `transactionsFor(number)` provide safe access.

Transactions are recorded only through the private `postTransaction()` method,
ensuring the ledger remains consistent and tamper-proof.

### OperationResult class

Encapsulates the outcome of operations into a single object. The UI layer only
needs to check `isSuccess()` or display `getMessage()`, without knowing the
internal processing details.

### AuthenticationService class

Encapsulates login validation logic, interacting with the `Bank` to verify
credentials. Failed attempts and lockout policies are enforced internally,
preventing external bypass.

---

## Activity 5 – Apply Abstraction

Abstraction in the banking system was applied to simplify complex operations and
hide unnecessary implementation details from the user and other parts of the
program. By exposing only essential methods and concealing internal logic, the
design ensures that each class provides a clear, high-level interface.

### Account class (abstract base)

Defines shared operations such as `deposit()`, `withdraw()`, and `getBalance()`.

Hides internal details like how balances are updated, how login attempts are
tracked, and how lockouts are enforced.

Abstract methods (`maxWithdrawable()`, `getAccountType()`) force subclasses to
provide specific rules without exposing the underlying mechanics.

### Bank class (facade)

Provides high-level operations (`deposit()`, `withdraw()`, `transfer()`) that
clients can call directly.

Internally, it handles account lookup, validation, and transaction recording,
but these details are hidden from the caller.

This abstraction allows the UI layer to interact with the `Bank` in simple
terms, without worrying about the complexity of account management.

### OperationResult class

Abstracts the outcome of operations into a single object.

Instead of exposing raw error codes or internal states, it provides simple
methods like `isSuccess()` and `getMessage()`.

This makes error handling and result reporting straightforward for the UI.

### Main class (UI layer)

Abstracts user interaction by providing menu options and prompts.

The user does not see how input is parsed or how transactions are processed
internally; they only interact with clear commands like "Deposit" or "Withdraw".

### Format utility class

Abstracts formatting logic for currency and text alignment.

The rest of the system simply calls `formatPeso()` or `pad()`, without needing
to know how rounding or alignment is implemented.

---

## Activity 6 – Apply Inheritance and Polymorphism

Inheritance and polymorphism were applied to make the system flexible, reusable,
and easier to extend. By defining shared behaviors in a base class and allowing
subclasses to override them, the design supports different account types without
duplicating code.

### Inheritance Structure

The `Account` class is abstract and serves as the base class for all account
types.

`SavingsAccount` and `CheckingAccount` both inherit from `Account`, gaining
access to shared attributes (account number, owner, PIN, balance, status) and
behaviors (deposit, withdraw, authentication).

This inheritance ensures that common logic is centralized, while specialized
rules are implemented only in subclasses.

### Polymorphic Behavior

The `withdraw()` method behaves differently depending on the account type:

In a `SavingsAccount`, withdrawals are capped at PHP 50,000.00 per transaction.

In a `CheckingAccount`, withdrawals can go up to the full balance.

The `getAccountType()` method returns different labels ("SAVINGS" or "CHECKING"),
depending on the subclass.

The `maxWithdrawable()` method is overridden in each subclass to enforce
account-specific rules.

---

## Activity 7 – Apply Composition and Object Relationships

Composition and object relationships were applied to model how classes
collaborate at runtime. Instead of relying solely on inheritance, the system
uses composition to build complex behaviors from simpler components.

### Bank as a central hub

Composes a collection of `Account` objects and a ledger of `Transaction`
records.

Owns these objects, meaning their lifecycle is tied to the `Bank`.

Provides controlled access to accounts and transactions through methods, rather
than exposing raw collections.

### Customer–Account relationship

A `Customer` may own multiple `Account` objects (one-to-many association).

Each `Account` references its `Customer`, establishing ownership and identity.
The reference is one-way: the `Customer` class does not keep a back-collection
of its accounts.

### Account–Transaction relationship

Each `Account` records multiple `Transaction` entries (one-to-many association).

Transactions capture the history of deposits, withdrawals, and transfers for
auditing.

### AuthenticationService–Bank relationship

The `AuthenticationService` depends on the `Bank` to validate credentials.

It interacts with accounts indirectly through the `Bank`, enforcing lockout
policies.

### Main–Bank/AuthenticationService relationship

The `Main` class composes both the `Bank` and `AuthenticationService`.

It delegates all business logic to these classes, focusing only on user
interaction.

### OperationResult as a communication object

Returned by `Bank` and `AuthenticationService` to encapsulate outcomes.

Used by `Main` to display results without needing internal details.

### Format utility relationship

Used by `Main`, `Bank`, and `Account` to ensure consistent formatting of
currency and text.

Not owned by any class, but shared as a helper.

### How objects collaborate at runtime

1. `Main.main()` creates a `Bank` (which seeds its accounts) and an
   `AuthenticationService`, then starts the console loop.
2. `Main.signIn()` asks `AuthenticationService.authenticate(...)` to validate a
   customer; on success it receives the signed-in `Account`.
3. Each menu action calls `Bank.deposit / withdraw / transfer`, which validates,
   delegates the money movement to the account(s), and records `Transaction`
   objects in the ledger.
4. Inquiry actions read account getters; history renders
   `Bank.transactionsFor()` entries.

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