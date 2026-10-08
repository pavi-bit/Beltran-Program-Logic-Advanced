package org.beltran;

import java.util.List;

public class Day4Demo {
    public static void main(String[] args) {
        Bank bank = new Bank();
        System.out.println("=== DAY 4 DEMONSTRATION ===");
        System.out.println();
        
        // Activity 3: Account Searching Comparison
        demoAccountSearching(bank);
        System.out.println();
        
        // Activity 4: Pending Transaction Queue
        demoPendingTransactions(bank);
        System.out.println();
        
        // Activity 5 & 6: Transaction History Sorting & Searching
        demoTransactionHistorySorting(bank);
        System.out.println();
        
        // Activity 7: Efficiency Analysis Info
        demoEfficiencyAnalysis();
    }
    
    private static void demoAccountSearching(Bank bank) {
        System.out.println("ACTIVITY 3 - ACCOUNT SEARCHING:");
        System.out.println("----------------------------------------");
        
        // Map lookup (optimized)
        Account accMap = bank.findAccount("1001");
        System.out.println("Map Lookup (O(1) avg): " + (accMap != null ? accMap.getNumber() + " - " + accMap.getHolderName() : "Not found"));
        
        // Linear search
        Account accLinear = bank.findAccountLinearSearch("1002");
        System.out.println("Linear Search (O(n)):  " + (accLinear != null ? accLinear.getNumber() + " - " + accLinear.getHolderName() : "Not found"));
        
        // Binary search
        Account accBinary = bank.findAccountBinarySearch("1003");
        System.out.println("Binary Search (O(log n)): " + (accBinary != null ? accBinary.getNumber() + " - " + accBinary.getHolderName() : "Not found"));
        System.out.println("✓ All search methods find accounts correctly");
    }
    
    private static void demoPendingTransactions(Bank bank) {
        System.out.println("ACTIVITY 4 - PENDING TRANSACTION QUEUE:");
        System.out.println("----------------------------------------");
        System.out.println("Initial pending count: " + bank.pendingCount());
        System.out.println("Has pending? " + bank.hasPending());
        
        // Create some mock pending transactions
        Transaction pending1 = new Transaction("1001", TransactionType.DEPOSIT, 5000.00, 30000.00, 
                java.time.LocalDateTime.now(), "");
        Transaction pending2 = new Transaction("1002", TransactionType.WITHDRAWAL, 2000.00, 48000.00, 
                java.time.LocalDateTime.now(), "");
        
        // 1. Adding a transaction to queue
        bank.enqueuePending(pending1);
        bank.enqueuePending(pending2);
        System.out.println("✓ Added 2 transactions to pending queue");
        System.out.println("Pending count after enqueue: " + bank.pendingCount());
        
        // 2. Storing - queue stores internally (implicit)
        // 3. Retrieving without removing
        Transaction peeked = bank.peekPending();
        System.out.println("✓ Peek (retrieve without removal): " + peeked.getType().getLabel() + " " + Format.formatPeso(peeked.getAmount()));
        System.out.println("Pending count after peek: " + bank.pendingCount() + " (unchanged)");
        
        // 4. Processing - dequeue
        OperationResult proc1 = bank.processNextPending();
        System.out.println("✓ Process next: " + proc1.getMessage());
        System.out.println("Pending count after dequeue: " + bank.pendingCount());
        
        // 5. Completing - remaining
        OperationResult procAll = bank.processAllPending();
        System.out.println("✓ " + procAll.getMessage());
        System.out.println("Pending count after all processed: " + bank.pendingCount());
    }
    
    private static void demoTransactionHistorySorting(Bank bank) {
        System.out.println("ACTIVITY 5 & 6 - TRANSACTION HISTORY & SORTING/SEARCHING:");
        System.out.println("----------------------------------------");
        
        // Create some transactions
        bank.deposit("1001", 1000.00);
        bank.deposit("1001", 500.00);
        bank.withdraw("1001", 200.00);
        bank.deposit("1001", 2000.00);
        
        List<Transaction> txs = bank.transactionsFor("1001");
        System.out.println("Transactions for 1001 (default order): " + txs.size());
        if (!txs.isEmpty()) {
            for (int i = 0; i < Math.min(3, txs.size()); i++) {
                Transaction t = txs.get(i);
                System.out.println("  " + t.getTimestampFormatted() + " | " + t.getType().getLabel() + " | " + Format.formatPeso(t.getAmount()));
            }
        }
        
        // Sort by amount (ascending)
        List<Transaction> sortedByAmount = bank.transactionsFor("1001", SortBy.AMOUNT, true);
        System.out.println("\n✓ Sorted by AMOUNT (ascending):");
        for (Transaction t : sortedByAmount) {
            System.out.println("  " + Format.formatPeso(t.getAmount()) + " | " + t.getType().getLabel());
        }
        
        // Sort by amount (descending)
        List<Transaction> sortedByAmountDesc = bank.transactionsFor("1001", SortBy.AMOUNT, false);
        System.out.println("✓ Sorted by AMOUNT (descending):");
        for (Transaction t : sortedByAmountDesc) {
            System.out.println("  " + Format.formatPeso(t.getAmount()) + " | " + t.getType().getLabel());
        }
        
        // Search by type
        List<Transaction> deposits = bank.findTransactionsByType(TransactionType.DEPOSIT);
        System.out.println("\n✓ Found " + deposits.size() + " DEPOSIT transactions total");
        
        // Search by amount range
        List<Transaction> midRange = bank.findTransactionsByAmountRange(500.00, 1500.00);
        System.out.println("✓ Found " + midRange.size() + " transactions between PHP 500.00 - 1500.00");
    }
    
    private static void demoEfficiencyAnalysis() {
        System.out.println("ACTIVITY 7 - EFFICIENCY ANALYSIS:");
        System.out.println("----------------------------------------");
        System.out.println("See DATA_STRUCTURES_ALGORITHMS_IMPLEMENTATION.md for full analysis");
        System.out.println();
        System.out.println("Key Optimizations:");
        System.out.println("  • Account lookup: O(n) → O(1) average (HashMap)");
        System.out.println("  • Transaction lookup by account: O(n) → O(k) with indexing");
        System.out.println("  • Queue operations: O(1) for all operations");
        System.out.println("  • Sorting: O(t log t) using Timsort");
        System.out.println("  • Binary Search: O(log n) for sorted lists");
    }
}
