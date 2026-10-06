package org.beltran;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

// Procedural banking app: data + menu loop live in main, actions are static methods.
public class Main {

    private static final int MAX_FAILED_ATTEMPTS = 3;
    private static final double MAX_SINGLE_AMOUNT = 1000000.00;
    private static final int MAX_LOGIN_ROUNDS = 5;
    private static final String TIMESTAMP_PATTERN = "yyyy-MM-dd HH:mm:ss";

    private static final int OPTION_ACCOUNT_INQUIRY = 1;
    private static final int OPTION_BALANCE_INQUIRY = 2;
    private static final int OPTION_DEPOSIT = 3;
    private static final int OPTION_WITHDRAWAL = 4;
    private static final int OPTION_FUND_TRANSFER = 5;
    private static final int OPTION_TRANSACTION_HISTORY = 6;
    private static final int OPTION_LOGOUT = 7;
    private static final int OPTION_COUNT = 7;

    private static final String TYPE_DEPOSIT = "DEPOSIT";
    private static final String TYPE_WITHDRAWAL = "WITHDRAWAL";
    private static final String TYPE_TRANSFER_IN = "TRANSFER-IN";
    private static final String TYPE_TRANSFER_OUT = "TRANSFER-OUT";

    // record = {number, holder, pin, balance, failedAttempts, locked, active}
    private static final ArrayList<String[]> ACCOUNTS = new ArrayList<String[]>();
    private static final int NUM = 0, HOLDER = 1, PIN = 2,
            BALANCE = 3, FAILED = 4, LOCKED = 5, ACTIVE = 6;

    // Each record: accountNumber|type|amount|balanceAfter|timestamp|counterparty
    private static final List<String> TRANSACTION_LOG = new ArrayList<String>();

    private static final Scanner CONSOLE = new Scanner(System.in);

    private static String lastMessage = "";

    private Main() {
    }

    // ------------------------------------------------------------------
    // Entry point and menu loop
    // ------------------------------------------------------------------

    public static void main(String[] args) {
        seedData();
        printBanner();

        int sessionIndex = signIn();
        if (sessionIndex < 0) {
            System.out.println("\n  Sign-in failed. Exiting the session.");
            return;
        }
        String sessionAccount = ACCOUNTS.get(sessionIndex)[NUM];

        boolean running = true;
        while (running) {
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

            String choice = readLine("");
            if (choice == null) {
                System.out.println("\n  Input stream closed. Exiting.");
                return;
            }

            int option = -1;
            String cleanedChoice = choice.trim();
            if (!cleanedChoice.isEmpty()) {
                try {
                    int code = Integer.parseInt(cleanedChoice);
                    if (code >= 1 && code <= OPTION_COUNT) {
                        option = code;
                    }
                } catch (NumberFormatException e) {
                    option = -1;
                }
            }
            if (option < 0) {
                System.out.println("\n  Invalid selection: \"" + cleanedChoice
                        + "\". Please enter a number between 1 and " + OPTION_COUNT + ".");
                continue;
            }

            switch (option) {
                case OPTION_ACCOUNT_INQUIRY:
                    displayAccountInquiry(sessionAccount);
                    break;
                case OPTION_BALANCE_INQUIRY:
                    displayBalance(sessionAccount);
                    break;
                case OPTION_DEPOSIT:
                    double depositAmount = readAmount("  Deposit amount (PHP): ");
                    if (!Double.isNaN(depositAmount)) {
                        depositFunds(sessionAccount, depositAmount);
                        System.out.println("\n  " + lastMessage);
                    }
                    break;
                case OPTION_WITHDRAWAL:
                    double withdrawalAmount = readAmount("  Withdrawal amount (PHP): ");
                    if (!Double.isNaN(withdrawalAmount)) {
                        withdrawFunds(sessionAccount, withdrawalAmount);
                        System.out.println("\n  " + lastMessage);
                    }
                    break;
                case OPTION_FUND_TRANSFER:
                    String destination = readLine("  Destination account number: ");
                    if (destination == null) {
                        System.out.println("\n  Input stream closed. Exiting.");
                        return;
                    }
                    double transferAmount = readAmount("  Transfer amount (PHP): ");
                    if (!Double.isNaN(transferAmount)) {
                        transferFunds(sessionAccount, destination, transferAmount);
                        System.out.println("\n  " + lastMessage);
                    }
                    break;
                case OPTION_TRANSACTION_HISTORY:
                    displayTransactionHistory(sessionAccount);
                    break;
                case OPTION_LOGOUT:
                    System.out.println("\n  Logout successful. Session ended for account "
                            + sessionAccount + ".");
                    running = false;
                    break;
                default:
                    System.out.println("\n  Option not available.");
                    break;
            }
        }
        System.out.println("\n  Thank you for banking with us. Goodbye!");
    }

    // ------------------------------------------------------------------
    // Setup and lookup
    // ------------------------------------------------------------------

    private static void seedData() {
        ACCOUNTS.clear();
        // PIN equals the account number; 1003 starts inactive.
        addAccount("1001", "Juan Dela Cruz", 25000.00, true);
        addAccount("1002", "Maria Santos", 50000.00, true);
        addAccount("1003", "Pedro Reyes", 10000.00, false);
        TRANSACTION_LOG.clear();
    }

    private static void addAccount(String number, String holder, double balance, boolean active) {
        ACCOUNTS.add(new String[]{number, holder, number,
                String.format("%.2f", balance), "0", "false", active ? "true" : "false"});
    }

    private static int findAccountIndex(String accountNumber) {
        if (accountNumber == null) {
            return -1;
        }
        String cleaned = accountNumber.trim();
        for (int i = 0; i < ACCOUNTS.size(); i++) {
            if (ACCOUNTS.get(i)[NUM].equals(cleaned)) {
                return i;
            }
        }
        return -1;
    }

    private static String holderOf(int index) {
        return ACCOUNTS.get(index)[HOLDER];
    }

    private static String pinOf(int index) {
        return ACCOUNTS.get(index)[PIN];
    }

    private static double balanceOf(int index) {
        return parseDouble(ACCOUNTS.get(index)[BALANCE]);
    }

    private static void setBalance(int index, double value) {
        ACCOUNTS.get(index)[BALANCE] = String.format("%.2f", value);
    }

    private static int failedOf(int index) {
        return (int) parseDouble(ACCOUNTS.get(index)[FAILED]);
    }

    private static void setFailed(int index, int value) {
        ACCOUNTS.get(index)[FAILED] = String.valueOf(value);
    }

    private static boolean isLocked(int index) {
        return parseBoolean(ACCOUNTS.get(index)[LOCKED]);
    }

    private static void setLocked(int index, boolean value) {
        ACCOUNTS.get(index)[LOCKED] = String.valueOf(value);
    }

    private static boolean isActive(int index) {
        return parseBoolean(ACCOUNTS.get(index)[ACTIVE]);
    }

    // ------------------------------------------------------------------
    // Authentication
    // ------------------------------------------------------------------

    private static int authenticateCustomer(String accountNumber, String pin) {
        if (accountNumber == null || accountNumber.trim().isEmpty()) {
            lastMessage = "Login failed. Account number is required.";
            return -1;
        }
        if (pin == null || pin.trim().isEmpty()) {
            lastMessage = "Login failed. PIN is required.";
            return -1;
        }

        int index = findAccountIndex(accountNumber);
        if (index < 0) {
            lastMessage = "Login failed. Account " + accountNumber.trim() + " does not exist.";
            return -1;
        }
        if (isLocked(index)) {
            lastMessage = "Login failed. Account " + ACCOUNTS.get(index)[NUM] + " is LOCKED after "
                    + MAX_FAILED_ATTEMPTS + " failed attempts. Please contact the branch.";
            return -1;
        }
        if (!pinOf(index).equals(pin.trim())) {
            int attempts = failedOf(index) + 1;
            setFailed(index, attempts);
            if (attempts >= MAX_FAILED_ATTEMPTS) {
                setLocked(index, true);
                lastMessage = "Login failed. Invalid PIN. Account " + ACCOUNTS.get(index)[NUM]
                        + " is now LOCKED after " + MAX_FAILED_ATTEMPTS + " failed attempts.";
            } else {
                lastMessage = "Login failed. Invalid PIN. Attempt " + attempts
                        + " of " + MAX_FAILED_ATTEMPTS + " used, "
                        + (MAX_FAILED_ATTEMPTS - attempts) + " remaining.";
            }
            return -1;
        }

        setFailed(index, 0);
        lastMessage = "Login successful. Welcome, " + holderOf(index)
                + " (Account " + ACCOUNTS.get(index)[NUM] + ").";
        return index;
    }

    private static int signIn() {
        System.out.println("\n+--------------------------------------------------+");
        System.out.println("|                  SECURE SIGN-IN                  |");
        System.out.println("+--------------------------------------------------+");

        for (int round = 1; round <= MAX_LOGIN_ROUNDS; round++) {
            String accountNumber = readLine("  Account number: ");
            if (accountNumber == null) {
                System.out.println("\n  Input stream closed. Exiting.");
                return -1;
            }
            String pin = readLine("  PIN           : ");
            if (pin == null) {
                System.out.println("\n  Input stream closed. Exiting.");
                return -1;
            }

            int index = authenticateCustomer(accountNumber, pin);
            System.out.println("\n  " + lastMessage);
            if (index >= 0) {
                return index;
            }
            if (lastMessage.contains("LOCKED")) {
                return -1;
            }
            System.out.println("  Attempt " + round + " of " + MAX_LOGIN_ROUNDS + ".");
        }
        System.out.println("\n  Login failed. Maximum of " + MAX_LOGIN_ROUNDS
                + " sign-in attempts reached.");
        return -1;
    }

    // ------------------------------------------------------------------
    // Validation
    // ------------------------------------------------------------------

    // Inactive accounts may sign in and view data, but cannot move money.
    private static int validateAccount(String accountNumber) {
        int index = findAccountIndex(accountNumber);
        if (index < 0 || isLocked(index) || !isActive(index)) {
            return -1;
        }
        return index;
    }

    private static boolean validateAmount(double amount) {
        if (Double.isNaN(amount) || Double.isInfinite(amount)) {
            return false;
        }
        return amount > 0 && amount <= MAX_SINGLE_AMOUNT;
    }

    private static String accountIssue(String accountNumber) {
        if (accountNumber == null || accountNumber.trim().isEmpty()) {
            return "Account number is required.";
        }
        int index = findAccountIndex(accountNumber);
        if (index < 0) {
            return "Account " + accountNumber.trim() + " does not exist.";
        }
        if (isLocked(index)) {
            return "Account " + ACCOUNTS.get(index)[NUM] + " is LOCKED after "
                    + MAX_FAILED_ATTEMPTS + " failed attempts. Please contact the branch.";
        }
        if (!isActive(index)) {
            return "Account " + ACCOUNTS.get(index)[NUM]
                    + " is INACTIVE. Transactions are blocked. Please contact the branch.";
        }
        return "";
    }

    private static String amountIssue(double amount) {
        if (Double.isNaN(amount) || Double.isInfinite(amount)) {
            return "Amount must be a valid number.";
        }
        if (amount <= 0) {
            return "Amount must be greater than 0.00.";
        }
        return "Amount must not exceed " + formatPeso(MAX_SINGLE_AMOUNT) + ".";
    }

    private static boolean recordTransaction(int accountIndex, String type, double amount,
                                             String counterparty) {
        if (accountIndex < 0 || accountIndex >= ACCOUNTS.size()) {
            lastMessage = "Record failed. No valid account was supplied.";
            return false;
        }
        String timestamp = new SimpleDateFormat(TIMESTAMP_PATTERN).format(new Date());
        String settled = String.format("%.2f", amount);
        String balanceAfter = String.format("%.2f", balanceOf(accountIndex));
        String party = counterparty == null ? "" : counterparty;
        TRANSACTION_LOG.add(ACCOUNTS.get(accountIndex)[NUM] + "|" + type + "|"
                + settled + "|" + balanceAfter + "|" + timestamp + "|" + party);
        return true;
    }

    // ------------------------------------------------------------------
    // Transactions
    // ------------------------------------------------------------------

    private static boolean depositFunds(String accountNumber, double amount) {
        int index = validateAccount(accountNumber);
        if (index < 0) {
            lastMessage = "Deposit failed. " + accountIssue(accountNumber);
            return false;
        }
        if (!validateAmount(amount)) {
            lastMessage = "Deposit failed. " + amountIssue(amount);
            return false;
        }

        double value = roundToCentavos(amount);
        setBalance(index, roundToCentavos(balanceOf(index) + value));
        recordTransaction(index, TYPE_DEPOSIT, value, "");
        lastMessage = "Deposit successful. " + formatPeso(value) + " deposited to Account "
                + ACCOUNTS.get(index)[NUM] + ". New balance: " + formatPeso(balanceOf(index)) + ".";
        return true;
    }

    private static boolean withdrawFunds(String accountNumber, double amount) {
        int index = validateAccount(accountNumber);
        if (index < 0) {
            lastMessage = "Withdrawal failed. " + accountIssue(accountNumber);
            return false;
        }
        if (!validateAmount(amount)) {
            lastMessage = "Withdrawal failed. " + amountIssue(amount);
            return false;
        }

        double value = roundToCentavos(amount);
        if (value > balanceOf(index)) {
            lastMessage = "Withdrawal failed. Insufficient balance to withdraw "
                    + formatPeso(value) + ". Available: " + formatPeso(balanceOf(index)) + ".";
            return false;
        }

        setBalance(index, roundToCentavos(balanceOf(index) - value));
        recordTransaction(index, TYPE_WITHDRAWAL, value, "");
        lastMessage = "Withdrawal successful. " + formatPeso(value) + " withdrawn from Account "
                + ACCOUNTS.get(index)[NUM] + ". New balance: " + formatPeso(balanceOf(index)) + ".";
        return true;
    }

    private static boolean transferFunds(String sourceNumber, String destinationNumber,
                                         double amount) {
        int sourceIndex = validateAccount(sourceNumber);
        if (sourceIndex < 0) {
            lastMessage = "Transfer failed. Source account: " + accountIssue(sourceNumber);
            return false;
        }
        int destinationIndex = validateAccount(destinationNumber);
        if (destinationIndex < 0) {
            lastMessage = "Transfer failed. Destination account: " + accountIssue(destinationNumber);
            return false;
        }
        if (sourceIndex == destinationIndex) {
            lastMessage = "Transfer failed. Source and destination must be different accounts.";
            return false;
        }
        if (!validateAmount(amount)) {
            lastMessage = "Transfer failed. " + amountIssue(amount);
            return false;
        }

        double value = roundToCentavos(amount);
        if (value > balanceOf(sourceIndex)) {
            lastMessage = "Transfer failed. Insufficient balance in Account "
                    + ACCOUNTS.get(sourceIndex)[NUM] + " to send " + formatPeso(value)
                    + ". Available: " + formatPeso(balanceOf(sourceIndex)) + ".";
            return false;
        }

        setBalance(sourceIndex, roundToCentavos(balanceOf(sourceIndex) - value));
        setBalance(destinationIndex, roundToCentavos(balanceOf(destinationIndex) + value));
        recordTransaction(sourceIndex, TYPE_TRANSFER_OUT, value,
                "To " + ACCOUNTS.get(destinationIndex)[NUM] + " (" + holderOf(destinationIndex) + ")");
        recordTransaction(destinationIndex, TYPE_TRANSFER_IN, value,
                "From " + ACCOUNTS.get(sourceIndex)[NUM] + " (" + holderOf(sourceIndex) + ")");
        lastMessage = "Transfer successful. " + formatPeso(value) + " sent from Account "
                + ACCOUNTS.get(sourceIndex)[NUM] + " to Account " + ACCOUNTS.get(destinationIndex)[NUM]
                + ". Source balance: " + formatPeso(balanceOf(sourceIndex))
                + " | Destination balance: " + formatPeso(balanceOf(destinationIndex)) + ".";
        return true;
    }

    // ------------------------------------------------------------------
    // Display actions
    // ------------------------------------------------------------------

    private static void displayAccountInquiry(String accountNumber) {
        int index = findAccountIndex(accountNumber);
        if (index < 0) {
            System.out.println("\n  Account inquiry failed. Account "
                    + describe(accountNumber) + " does not exist.");
            return;
        }
        String status = isLocked(index) ? "LOCKED" : (isActive(index) ? "ACTIVE" : "INACTIVE");
        System.out.println();
        System.out.println("  ACCOUNT INQUIRY");
        System.out.println("  ----------------------------------------------");
        System.out.println("  Account Number : " + ACCOUNTS.get(index)[NUM]);
        System.out.println("  Holder Name    : " + holderOf(index));
        System.out.println("  Status         : " + status);
        System.out.println("  Failed Attempts: " + failedOf(index) + " of " + MAX_FAILED_ATTEMPTS);
        System.out.println("  Balance        : " + formatPeso(balanceOf(index)));
        System.out.println("  ----------------------------------------------");
        System.out.println();
    }

    private static void displayBalance(String accountNumber) {
        int index = findAccountIndex(accountNumber);
        if (index < 0) {
            System.out.println("\n  Balance inquiry failed. Account "
                    + describe(accountNumber) + " does not exist.");
            return;
        }
        System.out.println("\n  BALANCE INQUIRY");
        System.out.println("  Account " + ACCOUNTS.get(index)[NUM] + " (" + holderOf(index) + ")");
        System.out.println("  Available balance: " + formatPeso(balanceOf(index)));
        System.out.println();
    }

    private static void displayTransactionHistory(String accountNumber) {
        int index = findAccountIndex(accountNumber);
        if (index < 0) {
            System.out.println("\n  Transaction history unavailable. Account "
                    + describe(accountNumber) + " does not exist.");
            return;
        }

        String prefix = ACCOUNTS.get(index)[NUM] + "|";
        int entries = 0;
        System.out.println();
        System.out.println("  TRANSACTION HISTORY - " + ACCOUNTS.get(index)[NUM]
                + " (" + holderOf(index) + ")");
        System.out.println("  Current balance: " + formatPeso(balanceOf(index)));
        System.out.println("  " + repeat('-', 72));

        for (String record : TRANSACTION_LOG) {
            if (!record.startsWith(prefix)) {
                continue;
            }
            String[] fields = record.split("\\|", -1);
            if (fields.length < 6) {
                continue;
            }
            if (entries == 0) {
                System.out.println("  " + pad("DATE / TIME", 20) + pad("TYPE", 15)
                        + padRight("AMOUNT", 15) + padRight("BALANCE", 17));
            }
            String row = "  " + pad(fields[4], 20) + pad(fields[1], 15)
                    + padRight(formatPeso(parseDouble(fields[2])), 15)
                    + padRight(formatPeso(parseDouble(fields[3])), 17);
            if (!fields[5].isEmpty()) {
                row += "  (" + fields[5] + ")";
            }
            System.out.println(row);
            entries++;
        }

        if (entries == 0) {
            System.out.println("  No transactions recorded yet.");
        }
        System.out.println("  " + repeat('-', 72));
        System.out.println();
    }

    // ------------------------------------------------------------------
    // Input helpers
    // ------------------------------------------------------------------

    private static double readAmount(String prompt) {
        String typed = readLine(prompt);
        if (typed == null) {
            return Double.NaN;
        }
        String cleaned = typed.trim()
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
            System.out.println("\n  \"" + typed.trim()
                    + "\" is not a valid amount. Operation cancelled.");
            return Double.NaN;
        }
    }

    private static String readLine(String prompt) {
        if (prompt != null && !prompt.isEmpty()) {
            System.out.print(prompt);
            System.out.flush();
        }
        try {
            if (!CONSOLE.hasNextLine()) {
                return null;
            }
            return CONSOLE.nextLine().trim();
        } catch (NoSuchElementException e) {
            return null;
        }
    }

    // ------------------------------------------------------------------
    // Formatting
    // ------------------------------------------------------------------

    private static double roundToCentavos(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private static String formatPeso(double value) {
        return "PHP " + String.format("%,.2f", value);
    }

    private static double parseDouble(String text) {
        try {
            return Double.parseDouble(text);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    private static boolean parseBoolean(String text) {
        return "true".equalsIgnoreCase(text);
    }

    private static String describe(String accountNumber) {
        return (accountNumber == null || accountNumber.trim().isEmpty())
                ? "(blank)" : accountNumber.trim();
    }

    private static String pad(String text, int width) {
        return text + repeat(' ', Math.max(0, width - text.length()));
    }

    private static String padRight(String text, int width) {
        return repeat(' ', Math.max(0, width - text.length())) + text;
    }

    private static String repeat(char c, int times) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < times; i++) {
            sb.append(c);
        }
        return sb.toString();
    }

    private static void printBanner() {
        System.out.println();
        System.out.println("==================================================");
        System.out.println("  BANKING TRANSACTION PROCESSING SYSTEM");
        System.out.println("  Session data is held in memory and resets on exit.");
        System.out.println("==================================================");
    }
}
