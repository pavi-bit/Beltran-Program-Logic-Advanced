# DAY 4 – DATA STRUCTURES, ALGORITHMS, AND OPTIMIZATION
## Implementation Plan and Documentation

### ACTIVITY 1 – Identify Data Requirements
**Review of Current OOP Banking Application**

The following data entities need to be stored and processed:

| Entity | Description | Current Implementation | Required Properties |
|---|---|---|---|
| **Customers** | Bank customers who own accounts | `Customer` class | `customerId` (String), `name` (String) |
| **Accounts** | Bank accounts (Checking/Savings) | `Account` abstract class with `CheckingAccount` and `SavingsAccount` subclasses | `number` (String), `owner` (Customer), `pin` (String), `balance` (double), `failedLoginAttempts` (int), `locked` (boolean), `active` (boolean) |
| **Transactions** | Completed transaction records | `Transaction` class | `accountNumber` (String), `type` (TransactionType), `amount` (double), `balanceAfter` (double), `timestamp` (LocalDateTime), `counterparty` (String) |
| **Transaction History** | Collection of all completed transactions | Stored in `Bank.ledger` as `List<Transaction>` | Chronological record of all account activity |
| **Pending Transactions** | Transactions waiting to be processed | **NOT CURRENTLY IMPLEMENTED** | Need queue to hold transactions before processing/commitment |

---

### ACTIVITY 2 – Select Appropriate Data Structures
Evaluation of data structures for the banking application:

| Data Structure | Suitability | Data to be Stored | Reason for Selection | Required Operations | Performance Considerations |
|---|---|---|---|---|---|
| **Arrays** | Low | Fixed collections | Fixed size arrays are inflexible for growing accounts/transactions. Not ideal as bank grows dynamically. | Access by index, traversal | O(1) access, O(n) for search; fixed size limits scalability |
| **Lists (ArrayList)** | **Currently Used** – Good | Accounts and completed transactions (`accounts`, `ledger`) | Dynamic size, easy iteration, preserves insertion order. Good for displaying transaction history in order. | `add()`, `get()`, `remove()`, `contains()`, `size()`, iteration | `add()` O(1) amortized, `get()` O(1), search O(n), contains O(n) |
| **Stacks** | Potentially Useful | Undo operations, transaction reversal history | LIFO structure useful for "undo last transaction" or rollback operations. Currently not implemented but could be valuable. | `push()`, `pop()`, `peek()`, `isEmpty()` | All O(1) |
| **Queues** | **Required for Activity 4** | Pending transactions awaiting processing | FIFO ensures transactions are processed in the order they were received (fair processing). Critical for batch processing or deferred execution. | `enqueue()/offer()`, `dequeue()/poll()`, `peek()`, `isEmpty()`, `size()` | All O(1) for ArrayDeque/LinkedList |
| **Dictionaries/Maps (HashMap)** | **Highly Recommended** | Account lookup by account number | Provides O(1) average lookup by key. Much faster than linear search as account count grows. Eliminates full list traversal. | `put()`, `get()`, `containsKey()`, `remove()`, `keySet()`, `values()` | O(1) average for get/put; O(n) worst case if hash collisions; O(1) space efficient |
| **Sets (HashSet)** | Potentially Useful | Unique account numbers, customer IDs | Ensures uniqueness and enables O(1) membership testing. Prevents duplicate accounts/customers. | `add()`, `remove()`, `contains()`, `size()` | O(1) average operations |

**Selected Structures for Implementation:**

1. **ArrayList** (Keep) - For ledger and account collections where iteration/order matters
2. **HashMap<String, Account>** (Add) - For fast account lookup by account number (O(1))
3. **HashMap<String, List<Transaction>>** (Add) - For indexed transaction lookup per account (optimization)
4. **ArrayDeque<Transaction>** (Add) - For pending transaction queue (FIFO processing)
5. **LinkedList** or **ArrayList** with sorting (Enhance) - For sorted transaction history

---

### ACTIVITY 3 – Implement Account Searching

**Current Implementation:** Linear search in `Bank.findAccount()` - scans all accounts sequentially until match found.

**Proposed Approaches:**

| Approach | Implementation | Time Complexity | Space Complexity | Pros | Cons | Recommendation |
|---|---|---|---|---|---|---|
| **Linear Search** | Iterate through `accounts` list from start to end | O(n) - worst case checks all elements | O(1) extra space | Simple, works on unsorted list, no preprocessing | Slow for large datasets (n grows) | Baseline - acceptable for small n, but not optimal |
| **Binary Search** | Search sorted `accounts` list by account number using `Collections.binarySearch()` or custom binary search | O(log n) | O(1) extra space | Much faster than linear for large n | Requires list to be sorted by account number; maintaining sorted order on inserts is needed | Good if accounts list is kept sorted; efficient for read-heavy operations |
| **Dictionary/Map Lookup** | Use `HashMap<String, Account>` with account number as key | O(1) average, O(n) worst case | O(1) extra space for lookup (map stores references) | Fastest for direct lookup by key; simplest API | Uses additional memory for map; must keep map in sync with list | **Best for this use case** - account numbers are unique keys, lookups are frequent (auth, transactions) |

**Recommended Approach:** **HashMap lookup** as the primary method (`findAccount()`), with linear search kept as fallback or for comparison. Binary search is also implemented to demonstrate understanding of algorithmic trade-offs.

**Implementation Plan:**
- Add `private final Map<String, Account> accountIndex = new HashMap<>()`
- Populate index in `seedAccounts()` and keep in sync if accounts added later
- Refactor `findAccount()` to use map: `return accountIndex.get(cleaned)`
- Add `findAccountLinear()` for comparison/demo
- Add `findAccountBinarySearch()` that sorts a copy/view of accounts by number

---

### ACTIVITY 4 – Implement Transaction Processing (Queue for Pending Transactions)

**Requirement:** Implement queue mechanism for handling multiple transactions with operations: add, store, retrieve, process, complete.

**Selected Structure:** `ArrayDeque<Transaction>` as a FIFO queue.

**Transaction States:**
- **Pending**: Transaction created but not yet fully processed/committed
- **Completed**: Transaction posted to ledger (historical record)

**Proposed Operations:**

| Operation | Method | Description |
|---|---|---|
| **1. Adding a transaction** | `enqueuePending(Transaction tx)` or `addPending()` | Add transaction to end of queue (FIFO). Marks as pending. |
| **2. Storing the transaction** | Queue internally stores via `offer()`/`addLast()` | Transaction object is stored in memory in queue structure until dequeued. |
| **3. Retrieving the transaction** | `peekPending()` | View next transaction without removing from queue (inspection). |
| **4. Processing the transaction** | `processNextPending()` | Dequeue transaction and apply business logic (validate, execute). |
| **5. Completing the transaction** | After processing, post to ledger via `postTransaction()` or mark complete | Move from pending queue to completed ledger; record in transaction history. |

**Implementation Plan:**
```java
private final Queue<Transaction> pendingQueue = new ArrayDeque<>();
```

**Methods to add to Bank:**
- `public void enqueuePending(Transaction transaction)` - Add to queue
- `public Transaction dequeuePending()` - Remove and return next (FIFO)
- `public Transaction peekPending()` - View next without removal
- `public boolean hasPending()` - Check if queue not empty
- `public int pendingCount()` - Queue size
- `public void clearPending()` - Clear all pending
- `public List<Transaction> getPendingTransactions()` - View all pending (for display)
- `public OperationResult processPending()` - Process next pending transaction end-to-end

**Design Note:** For demonstration, we can simulate "pending" workflow - e.g., queue transfers for later batch processing, or just demonstrate queue operations explicitly.

---

### ACTIVITY 5 – Implement Transaction History

**Current State:** `transactionsFor(accountNumber)` returns all transactions for an account from ledger (no sorting).

**Required:** Store and retrieve transaction records with sorting by transaction date, amount, or transaction type.

**Implementation Plan:**

**Retrieval Operations:**
1. `transactionsFor(String accountNumber)` - Get all transactions (unsorted as in ledger order)
2. `transactionsFor(String accountNumber, SortBy sortBy, boolean ascending)` - Get sorted transactions
3. `getAllTransactions()` - Get full ledger
4. `getAllTransactions(SortBy sortBy, boolean ascending)` - Get full ledger sorted

**Sorting Options:**
Create enum `SortBy` for type safety:
```java
public enum SortBy {
    DATE,
    AMOUNT,
    TYPE
}
```

**Sorting Implementation:**
Use `Collections.sort()` with custom `Comparator<Transaction>`

- **Sort by Date:** Compare `timestamp` (ascending/descending)
- **Sort by Amount:** Compare `amount` (ascending/descending) 
- **Sort by Type:** Compare `type` name/label (ascending/descending)

**Transaction Indexing (Optimization):** Add `Map<String, List<Transaction>> transactionIndex` to avoid scanning entire ledger when filtering by account. Keep index in sync on `postTransaction()`.

---

### ACTIVITY 6 – Implement Searching and Sorting

**Required Tasks:**

1. **Search for an account** - Already covered (Activity 3) with map/linear/binary
2. **Search for a transaction** - Need to find specific transaction(s) by criteria
3. **Sort transactions by amount** - Covered (Activity 5)
4. **Sort transactions by date** - Covered (Activity 5)  
5. **Compare different search approaches** - Document performance differences

**Transaction Search Operations:**

| Search Criteria | Method | Complexity | Notes |
|---|---|---|---|
| **By account + date range** | Search in transaction list | O(n) | Scan filtered or full list |
| **By amount range** | `findTransactionsByAmountRange(double min, double max)` | O(n) | Linear scan through ledger |
| **By transaction type** | `findTransactionsByType(TransactionType type)` | O(n) | Filter all transactions |
| **By counterparty** | `findTransactionsByCounterparty(String pattern)` | O(n) | Pattern matching |
| **Exact match** | Find specific transaction by composite key (account, timestamp, type, amount) | O(n) | Unlikely to have exact duplicates given timestamp precision |

**Sorting Algorithms to Demonstrate:** Use Java's built-in `Collections.sort()` (Timsort - O(n log n) time, O(n) space) which is efficient and appropriate. Can also implement simple sorts (bubble/insertion) for educational comparison if desired, but built-in is fine.

**Account Sorting for Binary Search:** Keep `accounts` list sorted by account number - when adding accounts (if ever), maintain sorted order or sort on-demand before binary search: `Collections.sort(accounts, Comparator.comparing(Account::getNumber))`

---

### ACTIVITY 7 – Analyze Algorithm Efficiency

**Time Complexity Analysis:**

| Operation | Current (Before Optimization) | After Optimization | Explanation |
|---|---|---|---|
| **findAccount()** | O(n) - linear scan of all accounts | **O(1) average** with HashMap | Direct key lookup vs full traversal |
| **validateAccount()** | O(n) - calls findAccount() | **O(1) average** | Benefits from optimized lookup |
| **transactionsFor(account)** | O(n) - scans entire ledger | **O(k)** where k = transactions for account with index, or still O(n) if scanning; with `transactionIndex.get(account)`, O(1) to get list + O(k) to return copy | Indexing by account avoids scanning unrelated transactions |
| **postTransaction()** | O(1) amortized (ArrayList.add) | **O(1) amortized** + O(1) for map updates | Slightly more work but same asymptotic; massive gain on reads |
| **Sorting transactions (n items)** | N/A (not implemented) | **O(m log m)** where m = transactions being sorted | Timsort is efficient, stable, O(m log m) worst case |
| **Binary search for account** | N/A | **O(log n)** | After sorting accounts list by number |
| **Queue operations (pending)** | N/A | **O(1)** for enqueue/dequeue/peek | ArrayDeque is optimal for FIFO |

**Space Complexity Analysis:**

| Component | Space | Notes |
|---|---|---|
| **ArrayList accounts** | O(n) | Stores account references |
| **ArrayList ledger** | O(m) | Stores transaction references (m = total transactions) |
| **HashMap accountIndex** | O(n) | Duplicate references to same Account objects - minimal overhead |
| **HashMap<String, List<Transaction>> transactionIndex** | O(m) | Each transaction reference stored once in its account's list |
| **ArrayDeque pendingQueue** | O(p) | p = pending transactions |
| **Total** | O(n + m) | Linear with data size; indexing trades memory for speed |

**Big O Notation Summary:**

- Account lookup: O(n) → **O(1) average** (optimized)
- Transaction history retrieval by account: O(n) → **O(k)** where k is count per account (optimized via indexing)
- Transaction processing (pending queue): **O(1)** per operation
- Sorting: **O(t log t)** for t transactions
- Linear search (comparative): O(n)
- Binary search: O(log n)

**Identified Inefficiencies as Data Grows:**
1. `transactionsFor()` currently does full scan of `ledger` - with 10k transactions across 1000 accounts, still scans all 10k even if account has 10 transactions. **Fixed with transaction indexing.**
2. `findAccount()` does linear scan - with many accounts, auth and transactions slow down. **Fixed with HashMap.**
3. No caching/indexing for transaction queries - sorting entire history repeatedly is costly. With indices, we sort smaller lists.
4. No data structure for pending work - could lead to ad-hoc processing. **Queue solves this.**

---

### ACTIVITY 8 – Optimization Challenge

**Inefficient Operation Identified:** `transactionsFor(String accountNumber)` in `Bank` class

**Before Optimization:**

```java
public List<Transaction> transactionsFor(String accountNumber) {
    List<Transaction> result = new ArrayList<>();
    String cleaned = accountNumber == null ? "" : accountNumber.trim();
    for (Transaction transaction : ledger) {
        if (transaction.getAccountNumber().equals(cleaned)) {
            result.add(transaction);
        }
    }
    return result;
}
```

- **Existing Implementation:** Linear scan through entire ledger, checks each transaction's account number
- **Problem:** Time grows linearly with total transaction count regardless of how many transactions belong to the account. As ledger grows (millions of transactions), this becomes expensive.
- **Time Complexity:** O(m) where m = total number of transactions in ledger
- **Space Complexity:** O(k) where k = number of matching transactions (for result list)

**After Optimization:**

```java
// Add index
private final Map<String, List<Transaction>> transactionIndex = new HashMap<>();

// Maintain index on post
private void postTransaction(Account account, TransactionType type, double amount, String counterparty) {
    Transaction tx = new Transaction(account.getNumber(), type, amount,
            account.getBalance(), LocalDateTime.now(), counterparty);
    ledger.add(tx);
    transactionIndex.computeIfAbsent(account.getNumber(), k -> new ArrayList<>()).add(tx);
}

// Optimized retrieval
public List<Transaction> transactionsFor(String accountNumber) {
    String cleaned = accountNumber == null ? "" : accountNumber.trim();
    List<Transaction> txs = transactionIndex.get(cleaned);
    if (txs == null) return new ArrayList<>();
    // Return copy to prevent external modification
    return new ArrayList<>(txs);
}
```

- **Improved Implementation:** Direct lookup in map by account number returns pre-indexed list
- **Reason for Improvement:** Eliminates scanning unrelated transactions. Lookup is O(1), then just copy references - O(k) for k transactions of that account. Much faster when accounts have few transactions relative to total.
- **New Time Complexity:** O(1) for map lookup + O(k) to copy result = O(k) where k = transactions for that account
- **New Space Complexity:** O(m) for index (stores references to same Transaction objects) - minimal memory overhead
- **Observed Improvement:** For large m (total transactions) and small k (per account), performance improves dramatically (e.g., m=100000, k=100 → 1000x faster). Even better with many lookups.

**Additional Optimizations:**
1. **Account lookup**: O(n) → O(1) average via `accountIndex` map
2. **Support sorted retrieval** without rescanning - sort the returned copy using comparators
3. **Binary search option** for accounts when map not suitable or for demonstration

---

### ACTIVITY 9 – Test the Optimized Application

**Testing Strategy:**
1. **Preserve existing behavior** - All current operations (deposit, withdraw, transfer, auth, inquiry) must produce identical results
2. **Test new functionality**:
- Pending transaction queue operations (enqueue, dequeue, peek, process)
- Transaction history sorting (by date, amount, type)
- Account lookup via map (verify same results as linear)
- Binary search demonstration
- Transaction search/filtering
3. **Integration testing** via Main menu or test harness
4. **Edge cases**: Empty queues/lists, null inputs, sorting with equal values

**Verification Checklist:**
- [ ] Deposits still work correctly and update balances
- [ ] Withdrawals respect limits and validations  
- [ ] Transfers work end-to-end (debit source, credit destination)
- [ ] Authentication still enforces PIN/lockout logic
- [ ] Transaction history displays correctly
- [ ] Optimizations don't change business logic
- [ ] Queue operations maintain FIFO order
- [ ] Sorting produces correct ordering (ascending/descending)
- [ ] Map lookups return same accounts as linear search

**Test Plan Execution:** After implementation, run the application, test all menu options, and optionally write quick verification code.

---

## Next Steps

1. Create supporting classes/enums as needed:
- `SortBy` enum for transaction sorting
- Update `Bank` class with all new structures and methods
- Add helper methods for searching/sorting/comparison
- Document complexity in code comments
- Update `Main` to optionally demonstrate new features (or create demo method)

2. Implement changes while maintaining backward compatibility

3. Test to ensure no behavioral changes to existing functionality

4. Verify compilation and run the application
