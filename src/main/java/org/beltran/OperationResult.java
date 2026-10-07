package org.beltran;

public final class OperationResult {

    private final boolean success;
    private final String message;
    private final Double balanceAfter;
    private final Account account;

    private OperationResult(boolean success, String message, Double balanceAfter, Account account) {
        this.success = success;
        this.message = message;
        this.balanceAfter = balanceAfter;
        this.account = account;
    }

    public static OperationResult ok(String message) {
        return new OperationResult(true, message, null, null);
    }

    public static OperationResult ok(Account account, String message) {
        return new OperationResult(true, message, null, account);
    }

    public static OperationResult ok(double balanceAfter, String message) {
        return new OperationResult(true, message, balanceAfter, null);
    }

    public static OperationResult fail(String message) {
        return new OperationResult(false, message, null, null);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public Double getBalanceAfter() {
        return balanceAfter;
    }

    public Account getAccount() {
        return account;
    }
}