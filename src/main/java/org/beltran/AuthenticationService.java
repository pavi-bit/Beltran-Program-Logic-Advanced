package org.beltran;

public class AuthenticationService {

    private final Bank bank;

    public AuthenticationService(Bank bank) {
        this.bank = bank;
    }

    public OperationResult authenticate(String accountNumber, String pin) {
        if (accountNumber == null || accountNumber.trim().isEmpty()) {
            return OperationResult.fail("Login failed. Account number is required.");
        }
        if (pin == null || pin.trim().isEmpty()) {
            return OperationResult.fail("Login failed. PIN is required.");
        }
        Account account = bank.findAccount(accountNumber);
        if (account == null) {
            return OperationResult.fail("Login failed. Account " + accountNumber.trim()
                    + " does not exist.");
        }
        if (account.isLocked()) {
            return OperationResult.fail("Login failed. Account " + account.getNumber()
                    + " is LOCKED after " + Bank.MAX_FAILED_ATTEMPTS
                    + " failed attempts. Please contact the branch.");
        }
        if (!account.matchesPin(pin.trim())) {
            account.recordFailedLogin();
            int attempts = account.getFailedLoginAttempts();
            if (attempts >= Bank.MAX_FAILED_ATTEMPTS) {
                account.lock();
                return OperationResult.fail("Login failed. Invalid PIN. Account "
                        + account.getNumber() + " is now LOCKED after "
                        + Bank.MAX_FAILED_ATTEMPTS + " failed attempts.");
            }
            return OperationResult.fail("Login failed. Invalid PIN. Attempt " + attempts
                    + " of " + Bank.MAX_FAILED_ATTEMPTS + " used, "
                    + (Bank.MAX_FAILED_ATTEMPTS - attempts) + " remaining.");
        }
        account.resetFailedLogins();
        return OperationResult.ok(account, "Login successful. Welcome, "
                + account.getHolderName() + " (Account " + account.getNumber() + ").");
    }
}