# DAY 5 – BANKING APPLICATION CAPSTONE
## Progress Check Report

**Project:** BankTransactionSystem
**Package:** `org.beltran`
**Language/Platform:** Java 8 (compiled), Maven
**Base version:** Version 3 (Day 4 iteration) – branch `opt`, commit `01cce6f`
**Report type:** Progress check / self-assessment
**Date prepared:** 2026-10-09

---

## How to read this report

This document answers each Day 5 activity one by one and explains **how the
activity applies to this specific system**. Where a change (refactor/debug/fix)
was **not** made in this pass, the report explains **why**. This pass was
deliberately a *review and verification* pass, not a rewrite: the goal was to
measure the current state of Version 3 against the capstone requirements before
touching code.

**Status legend**

| Status | Meaning |
|--------|---------|
| **Implemented** | Works end-to-end and is verified |
| **Partial** | Code exists but is not reachable from the user flow, or is incomplete |
| **Not applied** | Identified as needed, but intentionally not changed in this pass |
| **Verified** | Confirmed by running the application, not just inspection |

**Verification method used in this report**

```
mvn -q -o compile               -> EXIT=0 (compiles clean on Java 8)
java -cp target/classes org.beltran.Main   (driven with piped input)
```

---

## Activity 1 – Review the Complete Application

### What was compared against

The original **problem statement, requirements, business rules, flowcharts, and
pseudocode were produced during Days 1–2 and are not stored in this repository**.
Only the following versioned artifacts exist to compare against:

- the original procedural program (`Main.java`, commit `a02a2d8`, 626 lines),
- `BANKING_SYSTEM_DESIGN.md` (OOP design),
- `DATA_STRUCTURES_ALGORITHMS_IMPLEMENTATION.md` (Day 4 plan).

The review below has two parts:

1. **Original (Days 1–2 procedural) design → current implementation** — the
   overall refactor.
2. **`BANKING_SYSTEM_DESIGN.md` → current implementation** — where the written
   OOP design no longer matches the code.

### 1.1 Original procedural design vs current implementation

| # | Original design artifact | Current implementation | Difference |
|---|--------------------------|------------------------|------------|
| 1 | Single procedural `Main` class with static methods and string-map accounts | 11 OOP classes across 13 source files | Fully refactored to OOP; `SortBy` and `Day4Demo` were added later (see 1.2 – C1/C2/C3) |
| 2 | Static state / global transaction log | Instance state; `Main` constructs `Bank` and `AuthenticationService` | `Main` became a thin UI/controller layer (`Main.java:28-32`) |
| 3 | Single generic account | Abstract `Account` + `SavingsAccount` (PHP 50,000 cap) + `CheckingAccount` | New account-type rule not in the original design |
| 4 | Linear search only | Added `accountIndex` (`HashMap`), `transactionIndex`, `pendingQueue` | Day 4 algorithmic additions (see 1.2 – C4/C5) |

### 1.2 `BANKING_SYSTEM_DESIGN.md` vs current implementation

`BANKING_SYSTEM_DESIGN.md` was committed at `7352ba4`, **before** the Day 4
optimization commit `01cce6f` added `SortBy`, `Day4Demo`, and ~250 lines to
`Bank`. It was therefore accurate when written, but the code has moved on and
the document is now stale. Every difference is routed to the activity where it
is followed up.

| # | Design doc claim (line) | Current implementation | Routed to |
|---|-------------------------|------------------------|-----------|
| C1 | "one package `org.beltran` containing 11 Java source files" (`:43`) | 13 files — `SortBy.java` and `Day4Demo.java` were added in `01cce6f` | Act 5, Act 7 |
| C2 | `SortBy` enum is not mentioned anywhere | Exists (`SortBy.java`); used only by `Day4Demo` | Act 2, Act 5, Act 6 |
| C3 | `Day4Demo` driver class is not mentioned | A second `main` entry point; not part of the product flow | Act 2, Act 5 |
| C4 | Bank fields are only `accounts` and `ledger` (`:112-116`, `:252-253`) | Also `accountIndex`, `transactionIndex`, `pendingQueue` (`Bank.java:18-27`) | Act 5, Act 6 |
| C5 | Bank behaviors: only ~7 methods listed (`:112-116`) | Adds ~18 methods: linear/binary search, `getAllTransactions`×2, `findTransactionsByType/AmountRange`, the full queue API, `sortTransactions` | Act 2, Act 5, Act 6, Act 7 |
| C6 | Account "records" many Transactions (`:250`, `:293`, `:457-462`) | `Account` holds **no** transaction collection; transactions live in `Bank.ledger` / `transactionIndex` | Act 5, Act 6 |
| C7 | `"5 000"` is accepted (`:553`) | Rejected by `readAmount` (`Main.java:187-191`) | Act 3 (D1) |
| C8 | Main lists `MAX_LOGIN_ROUNDS = 5` (`:152`) while lockout is `MAX_FAILED_ATTEMPTS = 3` (`Bank.java:15`) | Both the `of 5` and `of 3` attempt counters are printed to the user | Act 3 (D2) |
| C9 | Doc jumps from Activity 7 straight to Activity 9; there is no Activity 8 (`:499-505`) | – (documentation gap only) | Act 1, Act 7 |
| C10 | `OperationResult.getBalanceAfter` is documented (`:131`) | Public but never called | Act 5, Act 7 |

**Action taken:** none. Activity 1 is analysis only, so there was nothing valid
to refactor here. Every C-item is carried into the activity that owns it (right
column) and consolidated in the Activity 7 backlog.

---

## Activity 2 – Complete the Application

### Status of the 12 required features

| # | Required feature | Where it lives | Status |
|---|------------------|----------------|--------|
| 1 | Customer Authentication | `AuthenticationService.authenticate` (`AuthenticationService.java:11`) | Implemented / Verified |
| 2 | Account Inquiry | `Main.showAccountInquiry` (`Main.java:205`), menu option 1 | Implemented / Verified |
| 3 | Balance Inquiry | Same screen, menu option 2 (`Main.java:64-67`) | Implemented / Verified |
| 4 | Deposit | `Bank.deposit` (`Bank.java:129`), menu option 3 | Implemented / Verified |
| 5 | Withdrawal | `Bank.withdraw` (`Bank.java:150`), menu option 4 | Implemented / Verified |
| 6 | Fund Transfer | `Bank.transfer` (`Bank.java:171`), menu option 5 | Implemented / Verified |
| 7 | Transaction Recording | `Bank.postTransaction` (`Bank.java:394`) | Implemented / Verified |
| 8 | Transaction History | `Main.showTransactionHistory` (`Main.java:220`), menu option 6 | Implemented / Verified |
| 9 | Input Validation | `Main.readAmount`, `Main.parseMenuChoice`, `Bank.validateAmount` | Implemented / Verified |
| 10 | Error Handling | `OperationResult` threaded through all services (`OperationResult.java`) | Implemented / Verified |
| 11 | Transaction Processing (pending queue) | `Bank.enqueuePending/processNextPending/processAllPending` (`Bank.java:309-369`) | **Partial** – only used by `Day4Demo`, not reachable from `Main` |
| 12 | Logout | Menu option 7 (`Main.java:105-109`) | Implemented / Verified |

### Unfinished / partial functionality

- **Pending-transaction queue is not exposed** on the menu. It works (proven by
  `Day4Demo`), but a normal user cannot enqueue or process pending transactions.
- **Sorting and transaction search** (`transactionsFor(..., SortBy, ...)`,
  `findTransactionsByType`, `findTransactionsByAmountRange`) are likewise only
  used by `Day4Demo`, not by `Main`.
- **Modules present in code but absent from the design doc** (C2/C3/C5): `SortBy`,
  `Day4Demo`, and the whole queue/search/sort surface of `Bank` are neither on the
  menu nor described in `BANKING_SYSTEM_DESIGN.md`.

**Why no change was made here:** the 11 features that are part of the normal
user flow are already complete and verified. Wiring the queue/sorting into the
menu is a feature decision (it changes the UI), so it is listed in the
Activity 7 backlog rather than applied unilaterally during a progress check.

---

## Activity 3 – Debugging

### Tests actually executed

| Run | Scenario | Result |
|-----|----------|--------|
| A | Sign in `1003`, deposit → inactive account | Blocked: `"Account 1003 is INACTIVE. Transactions are blocked."` |
| B | Savings `1002`, withdraw 60,000 (balance 50,000) | Blocked: `"Insufficient balance ... Available: PHP 50,000.00."` |
| B2 | Savings `1002`, deposit 100,000 then withdraw 60,000 | Blocked with the correct savings-cap message: `"SAVINGS accounts allow at most PHP 50,000.00..."` |
| C | Transfer from `1001` to `1001` | Blocked: `"Source and destination must be different accounts."` |
| D | Deposit `"5 000"`, then `"PHP 5,000"` | `"5 000"` rejected; `"PHP 5,000"` accepted |
| E | Three wrong PINs, then correct PIN | Account LOCKED after 3; correct PIN then rejected |
| F | Non-existent account 5 times | 5 rounds, then session ends |
| Compile | `mvn -q -o compile` | Clean (EXIT=0) |

No **runtime crash** was reproduced: all parsing paths are guarded, and no
unhandled exception surfaced in any scenario.

### Issue log

| # | Observed behavior | Expected behavior | Cause | Corrective action (proposed) | Test result / status |
|---|-------------------|-------------------|-------|------------------------------|----------------------|
| D1 | `"5 000"` is rejected as invalid | Design doc says space/comma/PHP are tolerated | `readAmount` only removes `PHP`, `₱`, `,` (`Main.java:187-191`) | Strip all non-digit separators (e.g. allow spaces) **or** correct the design doc | Verified as a real mismatch; **Not applied** |
| D2 | Once an account is locked, UI still prints `Attempt X of 5` alongside the auth `Attempt X of 3` line | A single, consistent attempt message | UI `MAX_LOGIN_ROUNDS = 5` vs domain `MAX_FAILED_ATTEMPTS = 3` (`Main.java:8`, `Bank.java:15,141`) | Derive the UI limit from `Bank.MAX_FAILED_ATTEMPTS` or suppress the UI counter when the domain already reports attempts | Verified (Run E/F); **Not applied** |
| D3 | `findAccountBinarySearch` is O(n log n) per call because it sorts a copy every lookup | O(log n) after sorting once | `Collections.sort` inside the method (`Bank.java:77-78`) | Sort once at seed time or keep a sorted view | Found by inspection; **Not applied** (method is demo-only) |
| D4 | On stream close, `readAmount` prints `"Input stream closed. Exiting."` but only the next menu-loop check actually exits, printing the message twice | Exit promptly and only once | Helper prints an exit message it cannot act on (`Main.java:182-185`) | Return a sentinel the caller acts on, or remove the print from the helper | Found by inspection; **Not applied** |
| D5 | `Account.deposit` accepts 0/negative amounts if called directly | Defense-in-depth: reject non-positive amounts even without `Bank` | `Account.deposit` has no local amount guard (`Account.java:79-86`) | Add an amount guard inside `Account` | Potential issue (currently unreachable via UI, since `Bank.validateAmount` runs first); **Not applied** |
| D6 | Dead/unreachable code: `getAllTransactions`, `getOwner`, `getCustomerId`, and all queue/search methods are unused outside `Day4Demo` | Only code used by the product should remain, or it should be wired in | Day 4 features were added for the algorithms exercise but never integrated | Wire them into the menu or remove them | Verified by search; **Not applied** |
| D7 | Transfer performs `source.withdraw` then `destination.deposit` without rollback | If the credit step could fail, the debit should be undone | No compensating action (`Bank.java:198-202`) | Add a two-phase commit/rollback or make the destination credit infallible by contract | Latent; currently destination always accepts, so no live failure; **Not applied** |

D1 and D2 are also design-document drift items (C7 and C8 in Activity 1); each
can be resolved either by fixing the code or by correcting the document.

**Why no fixes were applied:** this pass was defined as a progress check. Each
issue above is documented with a concrete corrective action so it can be applied
deliberately in the final refactor (Activity 7) instead of being mixed into a
review pass. D1 and D2 are the only ones reproducible by a user today.

---

## Activity 4 – Defensive Programming

### Defenses already present

| Defense | Location |
|---------|----------|
| Menu-choice parsing with range check | `Main.parseMenuChoice` (`Main.java:164-177`) |
| Amount parsing/stripping + clean cancellation | `Main.readAmount` (`Main.java:179-203`) |
| Amount validation (NaN/Infinite/≤0/> max) | `Bank.validateAmount` (`Bank.java:115-127`) |
| Account validation (exists/locked/inactive) | `Bank.validateAccount` (`Bank.java:96-113`) |
| Balance + account-type cap validation | `Account.withdraw` (`Account.java:88-105`) |
| Same-account transfer rejection | `Bank.transfer` (`Bank.java:184-187`) |
| Lockout after failed logins | `AuthenticationService` (`AuthenticationService.java:28-40`) |
| Immutable ledger posting (private) | `Bank.postTransaction` (`Bank.java:394`) |
| Defensive copy of transaction lists | `Bank.transactionsFor` (`Bank.java:226`) |

### Gaps identified

- **Assertions:** none. Class invariants (e.g. `balance >= 0`, `amount > 0`)
  are only enforced through control flow, not `assert`.
- **PIN rules:** no length/format validation; PINs are compared in **plaintext**
  (`Account.matchesPin`, `Account.java:52-54`).
- **Amount scale:** rounding happens at operation time (`Format.roundToCentavos`)
  but a machine-epsilon tolerance is not used for comparisons.
- **No exceptions:** the design uses `OperationResult` instead of exceptions.
  This is a valid choice (KISS) and was preserved.

**Why no change was made:** adding assertions and PIN rules is low-risk but still
modifies the domain model; it belongs to the final hardening pass. The existing
`OperationResult` pattern already satisfies the *error-handling* portion of this
activity, so the code is not left unguarded.

---

## Activity 5 – Code Self-Review

| Area | Finding | Proposed improvement |
|------|---------|----------------------|
| Naming | Clear and consistent (`validateAccount`, `transactionsFor`, `getStatusLabel`) | Keep |
| Readability | Good; short methods, clear comments | Keep |
| Code duplication | `Bank.deposit`/`withdraw` repeat the same validate-account → validate-amount → round → guard sequence (`Bank.java:129-169`) | Extract a shared `executeMoneyMovement(...)` helper |
| Method responsibilities | `Main.run` handles menu + routing + formatting; large `switch` | Extract per-option command methods (it already does for inquiry/history) |
| Class responsibilities | `Bank` does accounts + ledger + queue + search + sort + validation | Split (SRP) – see Activity 6 |
| Coupling | `Main` → concrete `Bank`/`AuthenticationService`; `Bank` exposes many public methods | Introduce interfaces (DIP) |
| Maintainability | Strong, but dead code (`getAllTransactions`, queue/search) confuses readers | Remove or integrate |
| Documentation | Design doc is stale: says 11 files vs 13; omits `SortBy`/`Day4Demo`; omits queue/search fields and methods; claims an `Account–Transaction` collection that `Account` does not have (C1–C6, C10) | Update `BANKING_SYSTEM_DESIGN.md` to match the final code (Activity 7) |
| Dead code | `getAllTransactions`, `getOwner`, `getCustomerId`, `OperationResult.getBalanceAfter`, and all queue/search methods are unused outside `Day4Demo` (C2/C3/C5) | Remove or wire into the menu |
| Error handling | Consistent via `OperationResult` | Keep |
| Data structures | `ArrayList` ledger + `HashMap` indices + `ArrayDeque` queue are appropriate | Keep; document the index-sync invariant |
| Algorithms | `HashMap` lookup O(1); sort O(n log n); queue O(1) | `findAccountBinarySearch` is mis-implemented (D3) |
| Performance | Good for the menu flow; index keeps per-account history O(k) | Add a note that `ledger` and `transactionIndex` must be updated together |

**Why no change was made:** these are quality improvements to code that is
already functional. Folding them into the same pass as the debugging fixes would
make the change set harder to review, so they are grouped into Activity 7.

---

## Activity 6 – Apply Software Design Principles

### SOLID assessment

| Principle | Assessment | Evidence |
|-----------|------------|----------|
| **S – Single Responsibility** | **Partially met.** `Account`, `Transaction`, `OperationResult`, `Format` are focused. `Bank` violates SRP: it owns storage, validation, search, sorting, queueing, and orchestration in one class (`Bank.java`) | Candidate for splitting into e.g. `AccountRepository` + `BankService` |
| **O – Open/Closed** | **Mostly met for account types** (new subclass needs no `Bank` change). **Not met for transaction types**: adding a new kind requires editing `Bank.deposit/withdraw/transfer` | Introduce a transaction-posting abstraction |
| **L – Liskov Substitution** | **Met.** `SavingsAccount`/`CheckingAccount` are usable anywhere `Account` is and only refine the `maxWithdrawable()` limit | No violation found |
| **I – Interface Segregation** | **Weak.** No interfaces exist; `OperationResult` is a general-purpose result object carrying account *or* balance *or* neither | Define role-specific interfaces where useful |
| **D – Dependency Inversion** | **Not met.** `Main` and `AuthenticationService` depend on the concrete `Bank` class | Depend on an abstraction of the bank operations |

### Additional principles

| Principle | Assessment | Evidence |
|-----------|------------|----------|
| **DRY** | Reasonably applied via `Format` and `OperationResult`, but `deposit`/`withdraw` duplicate validation sequencing | Extract shared helper |
| **KISS** | Mostly good; the console flow is simple and readable | Keep |
| **YAGNI** | **Violated.** Pending queue, binary/linear search variants, and transaction search/sort were built for Day 4 but are not used by the product, and they are not described in the design doc (C2/C3/C5) | Remove or intentionally integrate |

### At least two meaningful design improvements (documented)

1. **Reduce `Bank`'s responsibilities (SRP + DIP).** Extract account/transaction
   storage into a repository abstraction (e.g. `AccountRepository`,
   `TransactionLedger`) and let `Bank` orchestrate only. `Main` and
   `AuthenticationService` would depend on interfaces rather than on the
   concrete `Bank`. *Benefit:* testability (a fake repository), smaller change
   surface, and a clear storage-vs-business boundary.

2. **Make transaction posting extensible (OCP + DRY).** Replace the repeated
   validate → round → mutate → `postTransaction` sequence in
   `deposit`/`withdraw`/`transfer` with a single `executeMoneyMovement(...)`
   helper (or a `TransactionAction` strategy). *Benefit:* new transaction types
   require no edits to existing methods, and the validation sequence exists in
   one place.

3. **Resolve YAGNI.** Either wire the pending queue and search/sort into the
   menu (making them real features) or delete them. *Benefit:* removes
   misleading, untested-by-the-product code.

4. **Treat the design document as a design artifact (C1–C10).** The doc no longer
   describes the code: it omits `SortBy`/`Day4Demo` and the queue/search
   infrastructure, and it claims an `Account–Transaction` collection that does not
   exist (C6). Aligning the document with the code (or the code with the
   document) is a required design task, scheduled in Activity 7. *Benefit:* the
   design stays a reliable source of truth and the "review against the original
   design" activity (Activity 1) remains meaningful.

---

## Activity 7 – Final Refactoring

The final refactor has **not been applied in this pass**. This pass was scoped as
a progress check; the findings are consolidated below into a prioritized backlog
so the final version can be produced deliberately.

### Prioritized backlog

| Priority | Item | Source | Type |
|----------|------|--------|------|
| **P0** | Fix `"5 000"` input handling (D1) and align/remove the duplicated attempt message (D2) | Activity 3 | Bug / UX |
| **P0** | Wire the pending-transaction queue into the menu **or** remove it (D6, Activity 2 #11) | Activity 2 / 6 | Completeness / YAGNI |
| **P1** | Extract shared `executeMoneyMovement` helper for deposit/withdraw (DRY) | Activity 5 | Refactor |
| **P1** | Fix `findAccountBinarySearch` to sort once (D3) | Activity 3 | Performance |
| **P1** | Add `Account.deposit` amount guard and assertions for invariants (D5, Activity 4) | Activity 3 / 4 | Defensive |
| **P1** | Align `BANKING_SYSTEM_DESIGN.md` with the final code: fix file count (11→13), add `SortBy`/`Day4Demo`, document `Bank`'s queue/search fields and methods, remove the `Account–Transaction` collection arrow, add the missing Activity 8, and correct the `"5 000"` checklist item (C1–C10) | Activity 1 / 5 / 6 | Documentation |
| **P2** | Split `Bank` into repository + service and introduce interfaces (SRP/DIP) | Activity 6 | Design |
| **P2** | Harden transfer with rollback / infallible credit contract (D7) | Activity 3 | Correctness |
| **P2** | PIN format validation and hashing | Activity 4 | Security |
| **P3** | Extract per-menu-option command methods from `Main.run` (SRP) | Activity 5 | Readability |
| **P3** | Remove remaining dead getters (`getOwner`, `getCustomerId`, `getAllTransactions`, `OperationResult.getBalanceAfter`) | Activity 5 | Cleanup |

### Why the refactor was deferred

- The current Version 3 **compiles and passes all exercised scenarios**; there is
  no live defect that blocks the capstone.
- The instruction for this pass explicitly allowed explaining when no
  refactor/debug is made. Applying P0–P3 would be a substantial, reviewable
  change set that is better done as an explicit "final version" step.
- Every deferred item is documented with a concrete corrective action and a
  priority, so the work is ready to execute on request.
- The design-document drift (C1–C10) was recorded rather than silently patched,
  so the gap between the written design and the code stays auditable.

---

## Appendix – Verification Commands

```bash
# Compile
mvn -q -o compile

# Run interactively
java -cp target/classes org.beltran.Main

# Example non-interactive scenario (deposit + transfer + history)
printf '1001\n1001\n3\n5000\n5\n1002\n1000\n1\n6\n7\n' \
  | java -cp target/classes org.beltran.Main

# Run the Day 4 data-structure/algorithms demo
java -cp target/classes org.beltran.Day4Demo
```

**Overall Day 5 status:** the 11 user-facing features are complete and verified;
the 12th (pending transaction processing) is implemented but not wired to the
menu. Ten differences between `BANKING_SYSTEM_DESIGN.md` and the code (C1–C10)
were identified and routed to the relevant activities. No code was modified in
this pass; all findings are documented and prioritized for the final refactor.
