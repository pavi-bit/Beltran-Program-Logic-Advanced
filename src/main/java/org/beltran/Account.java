package org.beltran;

public abstract class Account {

    private final String number;
    private final Customer owner;
    private final String pin;
    private double balance;
    private int failedLoginAttempts;
    private boolean locked;
    private final boolean active;

    protected Account(String number, Customer owner, String pin, double balance,
                      boolean locked, boolean active) {
        this.number = number;
        this.owner = owner;
        this.pin = pin;
        this.balance = balance;
        this.locked = locked;
        this.active = active;
        this.failedLoginAttempts = 0;
    }

    public String getNumber() {
        return number;
    }

    public Customer getOwner() {
        return owner;
    }

    public String getHolderName() {
        return owner.getName();
    }

    public double getBalance() {
        return balance;
    }

    public int getFailedLoginAttempts() {
        return failedLoginAttempts;
    }

    public boolean isLocked() {
        return locked;
    }

    public boolean isActive() {
        return active;
    }

    public boolean matchesPin(String candidate) {
        return pin.equals(candidate);
    }

    public void recordFailedLogin() {
        failedLoginAttempts++;
    }

    public void resetFailedLogins() {
        failedLoginAttempts = 0;
    }

    public void lock() {
        locked = true;
    }

    public abstract String getAccountType();

    public abstract double maxWithdrawable();

    public String getStatusLabel() {
        if (locked) {
            return "LOCKED";
        }
        return active ? "ACTIVE" : "INACTIVE";
    }

    public OperationResult deposit(double amount) {
        OperationResult guard = transactionGuard();
        if (!guard.isSuccess()) {
            return guard;
        }
        balance = Format.roundToCentavos(balance + amount);
        return OperationResult.ok(balance, "Deposit accepted.");
    }

    public OperationResult withdraw(double amount) {
        OperationResult guard = transactionGuard();
        if (!guard.isSuccess()) {
            return guard;
        }
        if (amount > maxWithdrawable()) {
            if (amount > balance) {
                return OperationResult.fail("Insufficient balance to withdraw "
                        + Format.formatPeso(amount) + ". Available: "
                        + Format.formatPeso(balance) + ".");
            }
            return OperationResult.fail(getAccountType() + " accounts allow at most "
                    + Format.formatPeso(maxWithdrawable()) + " per transaction. Requested: "
                    + Format.formatPeso(amount) + ".");
        }
        balance = Format.roundToCentavos(balance - amount);
        return OperationResult.ok(balance, "Withdrawal accepted.");
    }

    private OperationResult transactionGuard() {
        if (locked) {
            return OperationResult.fail("Account " + number + " is LOCKED after "
                    + Bank.MAX_FAILED_ATTEMPTS + " failed attempts. Please contact the branch.");
        }
        if (!active) {
            return OperationResult.fail("Account " + number
                    + " is INACTIVE. Transactions are blocked. Please contact the branch.");
        }
        return OperationResult.ok("Account is ready.");
    }
}