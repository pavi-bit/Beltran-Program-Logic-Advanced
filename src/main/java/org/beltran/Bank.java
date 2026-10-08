package org.beltran;

import java.time.LocalDateTime;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;

public class Bank {

    public static final int MAX_FAILED_ATTEMPTS = 3;
    public static final double MAX_SINGLE_AMOUNT = 1000000.00;

    private final List<Account> accounts = new ArrayList<>();
    private final List<Transaction> ledger = new ArrayList<>();
    
    // Activity 2 & 3: Data structures for optimization
    // Map for O(1) average account lookup by account number
    private final Map<String, Account> accountIndex = new HashMap<>();
    // Map for O(1) lookup of transactions by account number
    private final Map<String, List<Transaction>> transactionIndex = new HashMap<>();
    // Queue for pending transactions (FIFO) - Activity 4
    private final Queue<Transaction> pendingQueue = new ArrayDeque<>();

    public Bank() {
        seedAccounts();
    }

    /**
     * Activity 3: Account searching using Dictionary/Map Lookup (O(1) average)
     * This is the optimized approach for account lookup.
     * Time Complexity: O(1) average, O(n) worst case due to hash collisions
     * Space Complexity: O(1) extra space for lookup
     */
    public Account findAccount(String accountNumber) {
        if (accountNumber == null || accountNumber.trim().isEmpty()) {
            return null;
        }
        String cleaned = accountNumber.trim();
        return accountIndex.get(cleaned);
    }

    /**
     * Activity 3: Linear Search implementation for comparison
     * Time Complexity: O(n) - worst case checks all elements
     * Space Complexity: O(1)
     */
    public Account findAccountLinearSearch(String accountNumber) {
        if (accountNumber == null || accountNumber.trim().isEmpty()) {
            return null;
        }
        String cleaned = accountNumber.trim();
        for (Account account : accounts) {
            if (account.getNumber().equals(cleaned)) {
                return account;
            }
        }
        return null;
    }

    /**
     * Activity 3: Binary Search implementation for comparison
     * Requires accounts list to be sorted by account number
     * Time Complexity: O(log n)
     * Space Complexity: O(1)
     */
    public Account findAccountBinarySearch(String accountNumber) {
        if (accountNumber == null || accountNumber.trim().isEmpty()) {
            return null;
        }
        String cleaned = accountNumber.trim();
        // Create a sorted copy of accounts by account number
        List<Account> sortedAccounts = new ArrayList<>(accounts);
        Collections.sort(sortedAccounts, Comparator.comparing(Account::getNumber));
        
        int left = 0;
        int right = sortedAccounts.size() - 1;
        while (left <= right) {
            int mid = (left + right) / 2;
            int cmp = sortedAccounts.get(mid).getNumber().compareTo(cleaned);
            if (cmp == 0) {
                return sortedAccounts.get(mid);
            } else if (cmp < 0) {
                left = mid + 1;
            } else {
                right = mid - 1;
            }
        }
        return null;
    }

    public OperationResult validateAccount(String accountNumber) {
        if (accountNumber == null || accountNumber.trim().isEmpty()) {
            return OperationResult.fail("Account number is required.");
        }
        Account account = findAccount(accountNumber);
        if (account == null) {
            return OperationResult.fail("Account " + accountNumber.trim() + " does not exist.");
        }
        if (account.isLocked()) {
            return OperationResult.fail("Account " + account.getNumber() + " is LOCKED after "
                    + MAX_FAILED_ATTEMPTS + " failed attempts. Please contact the branch.");
        }
        if (!account.isActive()) {
            return OperationResult.fail("Account " + account.getNumber()
                    + " is INACTIVE. Transactions are blocked. Please contact the branch.");
        }
        return OperationResult.ok(account, "Account validated.");
    }

    public OperationResult validateAmount(double amount) {
        if (Double.isNaN(amount) || Double.isInfinite(amount)) {
            return OperationResult.fail("Amount must be a valid number.");
        }
        if (amount <= 0) {
            return OperationResult.fail("Amount must be greater than 0.00.");
        }
        if (amount > MAX_SINGLE_AMOUNT) {
            return OperationResult.fail("Amount must not exceed "
                    + Format.formatPeso(MAX_SINGLE_AMOUNT) + ".");
        }
        return OperationResult.ok("Amount validated.");
    }

    public OperationResult deposit(String accountNumber, double amount) {
        OperationResult accountCheck = validateAccount(accountNumber);
        if (!accountCheck.isSuccess()) {
            return OperationResult.fail("Deposit failed. " + accountCheck.getMessage());
        }
        OperationResult amountCheck = validateAmount(amount);
        if (!amountCheck.isSuccess()) {
            return OperationResult.fail("Deposit failed. " + amountCheck.getMessage());
        }
        double value = Format.roundToCentavos(amount);
        Account account = accountCheck.getAccount();
        OperationResult result = account.deposit(value);
        if (!result.isSuccess()) {
            return OperationResult.fail("Deposit failed. " + result.getMessage());
        }
        postTransaction(account, TransactionType.DEPOSIT, value, "");
        return OperationResult.ok(account.getBalance(), "Deposit successful. "
                + Format.formatPeso(value) + " deposited to Account " + account.getNumber()
                + ". New balance: " + Format.formatPeso(account.getBalance()) + ".");
    }

    public OperationResult withdraw(String accountNumber, double amount) {
        OperationResult accountCheck = validateAccount(accountNumber);
        if (!accountCheck.isSuccess()) {
            return OperationResult.fail("Withdrawal failed. " + accountCheck.getMessage());
        }
        OperationResult amountCheck = validateAmount(amount);
        if (!amountCheck.isSuccess()) {
            return OperationResult.fail("Withdrawal failed. " + amountCheck.getMessage());
        }
        double value = Format.roundToCentavos(amount);
        Account account = accountCheck.getAccount();
        OperationResult result = account.withdraw(value);
        if (!result.isSuccess()) {
            return OperationResult.fail("Withdrawal failed. " + result.getMessage());
        }
        postTransaction(account, TransactionType.WITHDRAWAL, value, "");
        return OperationResult.ok(account.getBalance(), "Withdrawal successful. "
                + Format.formatPeso(value) + " withdrawn from Account " + account.getNumber()
                + ". New balance: " + Format.formatPeso(account.getBalance()) + ".");
    }

    public OperationResult transfer(String sourceNumber, String destinationNumber, double amount) {
        OperationResult sourceCheck = validateAccount(sourceNumber);
        if (!sourceCheck.isSuccess()) {
            return OperationResult.fail("Transfer failed. Source account: "
                    + sourceCheck.getMessage());
        }
        OperationResult destinationCheck = validateAccount(destinationNumber);
        if (!destinationCheck.isSuccess()) {
            return OperationResult.fail("Transfer failed. Destination account: "
                    + destinationCheck.getMessage());
        }
        Account source = sourceCheck.getAccount();
        Account destination = destinationCheck.getAccount();
        if (source.getNumber().equals(destination.getNumber())) {
            return OperationResult.fail(
                    "Transfer failed. Source and destination must be different accounts.");
        }
        OperationResult amountCheck = validateAmount(amount);
        if (!amountCheck.isSuccess()) {
            return OperationResult.fail("Transfer failed. " + amountCheck.getMessage());
        }
        double value = Format.roundToCentavos(amount);
        if (value > source.getBalance()) {
            return OperationResult.fail("Transfer failed. Insufficient balance in Account "
                    + source.getNumber() + " to send " + Format.formatPeso(value)
                    + ". Available: " + Format.formatPeso(source.getBalance()) + ".");
        }
        OperationResult debitResult = source.withdraw(value);
        if (!debitResult.isSuccess()) {
            return OperationResult.fail("Transfer failed. " + debitResult.getMessage());
        }
        destination.deposit(value);
        postTransaction(source, TransactionType.TRANSFER_OUT, value,
                "To " + destination.getNumber() + " (" + destination.getHolderName() + ")");
        postTransaction(destination, TransactionType.TRANSFER_IN, value,
                "From " + source.getNumber() + " (" + source.getHolderName() + ")");
        return OperationResult.ok(source.getBalance(), "Transfer successful. "
                + Format.formatPeso(value) + " sent from Account " + source.getNumber()
                + " to Account " + destination.getNumber() + ". Source balance: "
                + Format.formatPeso(source.getBalance()) + " | Destination balance: "
                + Format.formatPeso(destination.getBalance()) + ".");
    }

    /**
     * Activity 5 & 8: Optimized transaction retrieval with indexing
     * Time Complexity: O(1) for map lookup + O(k) to copy = O(k) where k = transactions for account
     * Space Complexity: O(k) for result copy
     */
    public List<Transaction> transactionsFor(String accountNumber) {
        String cleaned = accountNumber == null ? "" : accountNumber.trim();
        List<Transaction> txs = transactionIndex.get(cleaned);
        if (txs == null) {
            return new ArrayList<>();
        }
        // Return copy to prevent external modification of internal state
        return new ArrayList<>(txs);
    }

    /**
     * Activity 5: Get transactions sorted by specified criteria
     */
    public List<Transaction> transactionsFor(String accountNumber, SortBy sortBy, boolean ascending) {
        List<Transaction> transactions = transactionsFor(accountNumber);
        sortTransactions(transactions, sortBy, ascending);
        return transactions;
    }

    /**
     * Activity 5: Get all transactions
     */
    public List<Transaction> getAllTransactions() {
        return new ArrayList<>(ledger);
    }

    /**
     * Activity 5: Get all transactions sorted
     */
    public List<Transaction> getAllTransactions(SortBy sortBy, boolean ascending) {
        List<Transaction> transactions = new ArrayList<>(ledger);
        sortTransactions(transactions, sortBy, ascending);
        return transactions;
    }

    /**
     * Activity 5 & 6: Sort transactions by date, amount, or type
     */
    private void sortTransactions(List<Transaction> transactions, SortBy sortBy, boolean ascending) {
        if (transactions == null || transactions.isEmpty()) {
            return;
        }
        switch (sortBy) {
            case DATE:
                Collections.sort(transactions, Comparator.comparing(Transaction::getTimestamp));
                break;
            case AMOUNT:
                Collections.sort(transactions, Comparator.comparing(Transaction::getAmount));
                break;
            case TYPE:
                Collections.sort(transactions, Comparator.comparing(t -> t.getType().name()));
                break;
            default:
                Collections.sort(transactions, Comparator.comparing(Transaction::getTimestamp));
                break;
        }
        if (!ascending) {
            Collections.reverse(transactions);
        }
    }

    /**
     * Activity 6: Search for transactions by type
     */
    public List<Transaction> findTransactionsByType(TransactionType type) {
        List<Transaction> result = new ArrayList<>();
        for (Transaction transaction : ledger) {
            if (transaction.getType() == type) {
                result.add(transaction);
            }
        }
        return result;
    }

    /**
     * Activity 6: Search for transactions by amount range
     */
    public List<Transaction> findTransactionsByAmountRange(double minAmount, double maxAmount) {
        List<Transaction> result = new ArrayList<>();
        for (Transaction transaction : ledger) {
            if (transaction.getAmount() >= minAmount && transaction.getAmount() <= maxAmount) {
                result.add(transaction);
            }
        }
        return result;
    }

    /**
     * Activity 4: Queue operations for pending transactions
     */
    public void enqueuePending(Transaction transaction) {
        if (transaction != null) {
            pendingQueue.offer(transaction);
        }
    }

    public Transaction dequeuePending() {
        return pendingQueue.poll();
    }

    public Transaction peekPending() {
        return pendingQueue.peek();
    }

    public boolean hasPending() {
        return !pendingQueue.isEmpty();
    }

    public int pendingCount() {
        return pendingQueue.size();
    }

    public void clearPending() {
        pendingQueue.clear();
    }

    public List<Transaction> getPendingTransactions() {
        return new ArrayList<>(pendingQueue);
    }

    /**
     * Activity 4: Process next pending transaction
     * Demonstrates retrieving, processing, and completing a pending transaction
     */
    public OperationResult processNextPending() {
        if (!hasPending()) {
            return OperationResult.fail("No pending transactions to process.");
        }
        Transaction pending = dequeuePending();
        // In this demo, pending transactions are just completed by moving conceptually
        // The actual processing would depend on transaction type and validation
        return OperationResult.ok("Pending transaction processed and completed: "
                + pending.getType().getLabel() + " for " + Format.formatPeso(pending.getAmount())
                + " on account " + pending.getAccountNumber());
    }

    /**
     * Activity 4: Process all pending transactions
     */
    public OperationResult processAllPending() {
        int count = pendingCount();
        if (count == 0) {
            return OperationResult.fail("No pending transactions to process.");
        }
        int processed = 0;
        while (hasPending()) {
            processNextPending();
            processed++;
        }
        return OperationResult.ok("Processed " + processed + " pending transaction(s).");
    }

    private void seedAccounts() {
        CheckingAccount acc1 = new CheckingAccount("1001",
                new Customer("C001", "Juan Dela Cruz"), "1001", 25000.00, false, true);
        SavingsAccount acc2 = new SavingsAccount("1002",
                new Customer("C002", "Maria Santos"), "1002", 50000.00, false, true);
        SavingsAccount acc3 = new SavingsAccount("1003",
                new Customer("C003", "Pedro Reyes"), "1003", 10000.00, false, false);

        accounts.add(acc1);
        accounts.add(acc2);
        accounts.add(acc3);
        
        // Populate indices
        accountIndex.put(acc1.getNumber(), acc1);
        accountIndex.put(acc2.getNumber(), acc2);
        accountIndex.put(acc3.getNumber(), acc3);
        
        // Initialize transaction indices
        transactionIndex.put(acc1.getNumber(), new ArrayList<>());
        transactionIndex.put(acc2.getNumber(), new ArrayList<>());
        transactionIndex.put(acc3.getNumber(), new ArrayList<>());
    }

    private void postTransaction(Account account, TransactionType type, double amount,
                                  String counterparty) {
        Transaction tx = new Transaction(account.getNumber(), type, amount,
                account.getBalance(), LocalDateTime.now(), counterparty);
        ledger.add(tx);
        // Update transaction index (Activity 8 optimization)
        transactionIndex.computeIfAbsent(account.getNumber(), k -> new ArrayList<>()).add(tx);
    }
}
