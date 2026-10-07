package org.beltran;

import java.util.List;
import java.util.Scanner;

public class Main {

    private static final int MAX_LOGIN_ROUNDS = 5;

    private static final int OPTION_ACCOUNT_INQUIRY = 1;
    private static final int OPTION_BALANCE_INQUIRY = 2;
    private static final int OPTION_DEPOSIT = 3;
    private static final int OPTION_WITHDRAWAL = 4;
    private static final int OPTION_FUND_TRANSFER = 5;
    private static final int OPTION_TRANSACTION_HISTORY = 6;
    private static final int OPTION_LOGOUT = 7;
    private static final int OPTION_COUNT = 7;

    private final Bank bank;
    private final AuthenticationService authenticationService;
    private final Scanner console = new Scanner(System.in);

    public Main(Bank bank, AuthenticationService authenticationService) {
        this.bank = bank;
        this.authenticationService = authenticationService;
    }

    public static void main(String[] args) {
        Bank bank = new Bank();
        Main app = new Main(bank, new AuthenticationService(bank));
        app.run();
    }

    public void run() {
        System.out.println();
        System.out.println("==================================================");
        System.out.println("  BANKING TRANSACTION PROCESSING SYSTEM");
        System.out.println("  Session data is held in memory and resets on exit.");
        System.out.println("==================================================");

        Account sessionAccount = signIn();
        if (sessionAccount == null) {
            System.out.println("\n  Sign-in failed. Exiting the session.");
            return;
        }

        boolean running = true;
        while (running) {
            displayMenu();
            String choice = console.hasNextLine() ? console.nextLine().trim() : null;
            if (choice == null) {
                System.out.println("\n  Input stream closed. Exiting.");
                return;
            }

            int option = parseMenuChoice(choice);
            if (option < 0) {
                System.out.println("\n  Invalid selection: \"" + choice
                        + "\". Please enter a number between 1 and " + OPTION_COUNT + ".");
                continue;
            }

            switch (option) {
                case OPTION_ACCOUNT_INQUIRY:
                case OPTION_BALANCE_INQUIRY:
                    showAccountInquiry(sessionAccount);
                    break;
                case OPTION_DEPOSIT: {
                    double depositAmount = readAmount("  Deposit amount (PHP): ");
                    if (!Double.isNaN(depositAmount)) {
                        OperationResult result =
                                bank.deposit(sessionAccount.getNumber(), depositAmount);
                        System.out.println("\n  " + result.getMessage());
                    }
                    break;
                }
                case OPTION_WITHDRAWAL: {
                    double withdrawalAmount = readAmount("  Withdrawal amount (PHP): ");
                    if (!Double.isNaN(withdrawalAmount)) {
                        OperationResult result =
                                bank.withdraw(sessionAccount.getNumber(), withdrawalAmount);
                        System.out.println("\n  " + result.getMessage());
                    }
                    break;
                }
                case OPTION_FUND_TRANSFER: {
                    System.out.print("  Destination account number: ");
                    System.out.flush();
                    String destination = console.hasNextLine() ? console.nextLine().trim() : null;
                    if (destination == null) {
                        System.out.println("\n  Input stream closed. Exiting.");
                        return;
                    }
                    double transferAmount = readAmount("  Transfer amount (PHP): ");
                    if (!Double.isNaN(transferAmount)) {
                        OperationResult result = bank.transfer(sessionAccount.getNumber(),
                                destination, transferAmount);
                        System.out.println("\n  " + result.getMessage());
                    }
                    break;
                }
                case OPTION_TRANSACTION_HISTORY:
                    showTransactionHistory(sessionAccount);
                    break;
                case OPTION_LOGOUT:
                    System.out.println("\n  Logout successful. Session ended for account "
                            + sessionAccount.getNumber() + ".");
                    running = false;
                    break;
                default:
                    System.out.println("\n  Option not available.");
                    break;
            }
        }
        System.out.println("\n  Thank you for banking with us. Goodbye!");
    }

    private Account signIn() {
        System.out.println("\n+--------------------------------------------------+");
        System.out.println("|                  SECURE SIGN-IN                  |");
        System.out.println("+--------------------------------------------------+");

        for (int round = 1; round <= MAX_LOGIN_ROUNDS; round++) {
            System.out.print("  Account number: ");
            System.out.flush();
            String accountNumber = console.hasNextLine() ? console.nextLine().trim() : null;
            System.out.print("  PIN           : ");
            System.out.flush();
            String pin = console.hasNextLine() ? console.nextLine().trim() : null;
            if (accountNumber == null || pin == null) {
                System.out.println("\n  Input stream closed. Exiting.");
                return null;
            }

            OperationResult result = authenticationService.authenticate(accountNumber, pin);
            System.out.println("\n  " + result.getMessage());
            if (result.isSuccess()) {
                return result.getAccount();
            }
            if (!result.getMessage().contains("LOCKED")) {
                System.out.println("  Attempt " + round + " of " + MAX_LOGIN_ROUNDS + ".");
            }
        }
        return null;
    }

    private void displayMenu() {
        System.out.println();
        System.out.println("+--------------------------------------------------+");
        System.out.println("|      BANKING TRANSACTION PROCESSING SYSTEM       |");
        System.out.println("+--------------------------------------------------+");
        System.out.println("  " + OPTION_ACCOUNT_INQUIRY + ". Account Inquiry");
        System.out.println("  " + OPTION_BALANCE_INQUIRY + ". Balance Inquiry");
        System.out.println("  " + OPTION_DEPOSIT + ". Deposit");
        System.out.println("  " + OPTION_WITHDRAWAL + ". Withdrawal");
        System.out.println("  " + OPTION_FUND_TRANSFER + ". Fund Transfer");
        System.out.println("  " + OPTION_TRANSACTION_HISTORY + ". Transaction History");
        System.out.println("  " + OPTION_LOGOUT + ". Logout");
        System.out.println("--------------------------------------------------");
        System.out.print("  Enter your choice (1-" + OPTION_COUNT + "): ");
        System.out.flush();
    }

    private int parseMenuChoice(String choice) {
        if (choice == null || choice.isEmpty()) {
            return -1;
        }
        try {
            int code = Integer.parseInt(choice);
            if (code >= 1 && code <= OPTION_COUNT) {
                return code;
            }
        } catch (NumberFormatException e) {
            // fall through
        }
        return -1;
    }

    private double readAmount(String prompt) {
        System.out.print(prompt);
        System.out.flush();
        if (!console.hasNextLine()) {
            System.out.println("\n  Input stream closed. Exiting.");
            return Double.NaN;
        }
        String text = console.nextLine();
        String cleaned = text.trim()
                .replace("PHP", "")
                .replace("\u20B1", "")
                .replace(",", "")
                .trim();
        if (cleaned.isEmpty()) {
            System.out.println("\n  No amount entered. Operation cancelled.");
            return Double.NaN;
        }
        try {
            return Double.parseDouble(cleaned);
        } catch (NumberFormatException e) {
            System.out.println("\n  \"" + text.trim()
                    + "\" is not a valid amount. Operation cancelled.");
            return Double.NaN;
        }
    }

    private void showAccountInquiry(Account account) {
        System.out.println();
        System.out.println("  ACCOUNT / BALANCE INQUIRY");
        System.out.println("  ----------------------------------------------");
        System.out.println("  Account Number : " + account.getNumber());
        System.out.println("  Holder Name    : " + account.getHolderName());
        System.out.println("  Account Type   : " + account.getAccountType());
        System.out.println("  Status         : " + account.getStatusLabel());
        System.out.println("  Failed Attempts: " + account.getFailedLoginAttempts()
                + " of " + Bank.MAX_FAILED_ATTEMPTS);
        System.out.println("  Balance        : " + Format.formatPeso(account.getBalance()));
        System.out.println("  ----------------------------------------------");
        System.out.println();
    }

    private void showTransactionHistory(Account account) {
        List<Transaction> transactions = bank.transactionsFor(account.getNumber());
        System.out.println();
        System.out.println("  TRANSACTION HISTORY - " + account.getNumber()
                + " (" + account.getHolderName() + ")");
        System.out.println("  Current balance: " + Format.formatPeso(account.getBalance()));
        System.out.println("  " + Format.repeat('-', 72));

        int entries = 0;
        for (Transaction transaction : transactions) {
            if (entries == 0) {
                System.out.println("  " + Format.pad("DATE / TIME", 20) + Format.pad("TYPE", 15)
                        + Format.padRight("AMOUNT", 15) + Format.padRight("BALANCE", 17));
            }
            String row = "  " + Format.pad(transaction.getTimestampFormatted(), 20)
                    + Format.pad(transaction.getType().getLabel(), 15)
                    + Format.padRight(Format.formatPeso(transaction.getAmount()), 15)
                    + Format.padRight(Format.formatPeso(transaction.getBalanceAfter()), 17);
            if (transaction.getCounterparty() != null
                    && !transaction.getCounterparty().isEmpty()) {
                row += "  (" + transaction.getCounterparty() + ")";
            }
            System.out.println(row);
            entries++;
        }

        if (entries == 0) {
            System.out.println("  No transactions recorded yet.");
        }
        System.out.println("  " + Format.repeat('-', 72));
        System.out.println();
    }
}