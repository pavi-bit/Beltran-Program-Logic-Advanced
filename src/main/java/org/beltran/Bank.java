package org.beltran;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Bank {

    public static final int MAX_FAILED_ATTEMPTS = 3;
    public static final double MAX_SINGLE_AMOUNT = 1000000.00;

    private final List<Account> accounts = new ArrayList<>();
    private final List<Transaction> ledger = new ArrayList<>();

    public Bank() {
        seedAccounts();
    }

    public Account findAccount(String accountNumber) {
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

    private void seedAccounts() {
        accounts.add(new CheckingAccount("1001",
                new Customer("C001", "Juan Dela Cruz"), "1001", 25000.00, false, true));
        accounts.add(new SavingsAccount("1002",
                new Customer("C002", "Maria Santos"), "1002", 50000.00, false, true));
        accounts.add(new SavingsAccount("1003",
                new Customer("C003", "Pedro Reyes"), "1003", 10000.00, false, false));
    }

    private void postTransaction(Account account, TransactionType type, double amount,
                                 String counterparty) {
        ledger.add(new Transaction(account.getNumber(), type, amount,
                account.getBalance(), LocalDateTime.now(), counterparty));
    }
}