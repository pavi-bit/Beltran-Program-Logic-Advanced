package org.beltran;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
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

    // Each account is one database-style row: number, holder, pin, balance,
    // failed, locked, active.
    private static final ArrayList<LinkedHashMap<String, String>> ACCOUNTS = new ArrayList<>();

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
        String sessionAccount = numberOf(sessionIndex);

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


    // number | holder_name    | pin  | balance   | failed | locked | active
    private static void seedData() {
        ACCOUNTS.clear();
        ACCOUNTS.add(row("1001", "Juan Dela Cruz", "25000.00", true ));
        ACCOUNTS.add(row("1002", "Maria Santos",   "50000.00", true ));
        ACCOUNTS.add(row("1003", "Pedro Reyes",    "10000.00", false));
        TRANSACTION_LOG.clear();
    }

    // Builds one database row; the PIN always equals the account number.
    private static LinkedHashMap<String, String> row(String number, String holder,
            String balance, boolean active) {
        LinkedHashMap<String, String> r = new LinkedHashMap<>();
        r.put("number", number);
        r.put("holder", holder);
        r.put("pin", number);
        r.put("balance", balance);
        r.put("failed", "0");
        r.put("locked", "false");
        r.put("active", active ? "true" : "false");
        return r;
    }

    private static String get(int index, String field) {
        return ACCOUNTS.get(index).get(field);
    }

    private static void put(int index, String field, String value) {
        ACCOUNTS.get(index).put(field, value);
    }

    private static String numberOf(int index) {
        return get(index, "number");
    }

    private static String holderOf(int index) {
        return get(index, "holder");
    }

    private static String pinOf(int index) {
        return get(index, "pin");
    }

    private static double balanceOf(int index) {
        return parseDouble(get(index, "balance"));
    }

    private static void setBalance(int index, double value) {
        put(index, "balance", String.format("%.2f", value));
    }

    private static int failedOf(int index) {
        return (int) parseDouble(get(index, "failed"));
    }

    private static void setFailed(int index, int value) {
        put(index, "failed", String.valueOf(value));
    }

    private static boolean isLocked(int index) {
        return parseBoolean(get(index, "locked"));
    }

    private static void setLocked(int index, boolean value) {
        put(index, "locked", String.valueOf(value));
    }

    private static boolean isActive(int index) {
        return parseBoolean(get(index, "active"));
    }

    private static int findAccountIndex(String accountNumber) {
        if (accountNumber == null) {
            return -1;
        }
        String cleaned = accountNumber.trim();
        for (int i = 0; i < ACCOUNTS.size(); i++) {
            if (numberOf(i).equals(cleaned)) {
                return i;
            }
        }
        return -1;
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
            lastMessage = "Login failed. Account " + numberOf(index) + " is LOCKED after "
                    + MAX_FAILED_ATTEMPTS + " failed attempts. Please contact the branch.";
            return -1;
        }
        if (!pinOf(index).equals(pin.trim())) {
            int attempts = failedOf(index) + 1;
            setFailed(index, attempts);
            if (attempts >= MAX_FAILED_ATTEMPTS) {
                setLocked(index, true);
                lastMessage = "Login failed. Invalid PIN. Account " + numberOf(index)
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
                + " (Account " + numberOf(index) + ").";
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

    // Inactive accounts may sign in and view data, but cannot move money.
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
        TRANSACTION_LOG.add(numberOf(accountIndex) + "|" + type + "|"
                + settled + "|" + balanceAfter + "|" + timestamp + "|" + party);
        return true;
    }

    // ------------------------------------------------------------------
    // Transactions
    // ------------------------------------------------------------------

    private static boolean depositFunds(String accountNumber, double amount) {
        int index = findAccountIndex(accountNumber);
        boolean validAmount = !Double.isNaN(amount) && !Double.isInfinite(amount)
                && amount > 0 && amount <= MAX_SINGLE_AMOUNT;
        if (index >= 0 && !isLocked(index) && isActive(index)) {
            if (validAmount) {
                double value = roundToCentavos(amount);
                setBalance(index, roundToCentavos(balanceOf(index) + value));
                recordTransaction(index, TYPE_DEPOSIT, value, "");
                lastMessage = "Deposit successful. " + formatPeso(value) + " deposited to Account "
                        + numberOf(index) + ". New balance: " + formatPeso(balanceOf(index)) + ".";
                return true;
            }
            lastMessage = "Deposit failed. " + (Double.isNaN(amount) || Double.isInfinite(amount)
                    ? "Amount must be a valid number."
                    : amount <= 0 ? "Amount must be greater than 0.00."
                    : "Amount must not exceed " + formatPeso(MAX_SINGLE_AMOUNT) + ".");
        } else {
            lastMessage = "Deposit failed. " + (index < 0
                    ? "Account " + describe(accountNumber) + " does not exist."
                    : isLocked(index) ? "Account " + numberOf(index) + " is LOCKED after "
                            + MAX_FAILED_ATTEMPTS + " failed attempts. Please contact the branch."
                    : "Account " + numberOf(index)
                            + " is INACTIVE. Transactions are blocked. Please contact the branch.");
        }
        return false;
    }

    private static boolean withdrawFunds(String accountNumber, double amount) {
        int index = findAccountIndex(accountNumber);
        boolean validAmount = !Double.isNaN(amount) && !Double.isInfinite(amount)
                && amount > 0 && amount <= MAX_SINGLE_AMOUNT;
        if (index >= 0 && !isLocked(index) && isActive(index)) {
            if (validAmount) {
                double value = roundToCentavos(amount);
                if (value <= balanceOf(index)) {
                    setBalance(index, roundToCentavos(balanceOf(index) - value));
                    recordTransaction(index, TYPE_WITHDRAWAL, value, "");
                    lastMessage = "Withdrawal successful. " + formatPeso(value) + " withdrawn from Account "
                            + numberOf(index) + ". New balance: " + formatPeso(balanceOf(index)) + ".";
                    return true;
                }
                lastMessage = "Withdrawal failed. Insufficient balance to withdraw "
                        + formatPeso(value) + ". Available: " + formatPeso(balanceOf(index)) + ".";
            } else {
                lastMessage = "Withdrawal failed. " + (Double.isNaN(amount) || Double.isInfinite(amount)
                        ? "Amount must be a valid number."
                        : amount <= 0 ? "Amount must be greater than 0.00."
                        : "Amount must not exceed " + formatPeso(MAX_SINGLE_AMOUNT) + ".");
            }
        } else {
            lastMessage = "Withdrawal failed. " + (index < 0
                    ? "Account " + describe(accountNumber) + " does not exist."
                    : isLocked(index) ? "Account " + numberOf(index) + " is LOCKED after "
                            + MAX_FAILED_ATTEMPTS + " failed attempts. Please contact the branch."
                    : "Account " + numberOf(index)
                            + " is INACTIVE. Transactions are blocked. Please contact the branch.");
        }
        return false;
    }

    private static boolean transferFunds(String sourceNumber, String destinationNumber,
                                         double amount) {
        int src = findAccountIndex(sourceNumber);
        int dst = findAccountIndex(destinationNumber);
        boolean validAmount = !Double.isNaN(amount) && !Double.isInfinite(amount)
                && amount > 0 && amount <= MAX_SINGLE_AMOUNT;
        if (src >= 0 && !isLocked(src) && isActive(src)
                && dst >= 0 && !isLocked(dst) && isActive(dst)) {
            if (src != dst) {
                if (validAmount) {
                    double value = roundToCentavos(amount);
                    if (value <= balanceOf(src)) {
                        setBalance(src, roundToCentavos(balanceOf(src) - value));
                        setBalance(dst, roundToCentavos(balanceOf(dst) + value));
                        recordTransaction(src, TYPE_TRANSFER_OUT, value,
                                "To " + numberOf(dst) + " (" + holderOf(dst) + ")");
                        recordTransaction(dst, TYPE_TRANSFER_IN, value,
                                "From " + numberOf(src) + " (" + holderOf(src) + ")");
                        lastMessage = "Transfer successful. " + formatPeso(value) + " sent from Account "
                                + numberOf(src) + " to Account " + numberOf(dst)
                                + ". Source balance: " + formatPeso(balanceOf(src))
                                + " | Destination balance: " + formatPeso(balanceOf(dst)) + ".";
                        return true;
                    }
                    lastMessage = "Transfer failed. Insufficient balance in Account "
                            + numberOf(src) + " to send " + formatPeso(value) + ". Available: "
                            + formatPeso(balanceOf(src)) + ".";
                } else {
                    lastMessage = "Transfer failed. " + (Double.isNaN(amount) || Double.isInfinite(amount)
                            ? "Amount must be a valid number."
                            : amount <= 0 ? "Amount must be greater than 0.00."
                            : "Amount must not exceed " + formatPeso(MAX_SINGLE_AMOUNT) + ".");
                }
            } else {
                lastMessage = "Transfer failed. Source and destination must be different accounts.";
            }
        } else {
            lastMessage = "Transfer failed. " + (src < 0
                    ? "Source account " + describe(sourceNumber) + " does not exist."
                    : isLocked(src) ? "Source account " + numberOf(src) + " is LOCKED after "
                            + MAX_FAILED_ATTEMPTS + " failed attempts. Please contact the branch."
                    : !isActive(src) ? "Source account " + numberOf(src)
                            + " is INACTIVE. Transactions are blocked. Please contact the branch."
                    : dst < 0 ? "Destination account " + describe(destinationNumber)
                            + " does not exist."
                    : isLocked(dst) ? "Destination account " + numberOf(dst) + " is LOCKED after "
                            + MAX_FAILED_ATTEMPTS + " failed attempts. Please contact the branch."
                    : "Destination account " + numberOf(dst)
                            + " is INACTIVE. Transactions are blocked. Please contact the branch.");
        }
        return false;
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
        System.out.println("  Account Number : " + numberOf(index));
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
        System.out.println("  Account " + numberOf(index) + " (" + holderOf(index) + ")");
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

        String prefix = numberOf(index) + "|";
        int entries = 0;
        System.out.println();
        System.out.println("  TRANSACTION HISTORY - " + numberOf(index)
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