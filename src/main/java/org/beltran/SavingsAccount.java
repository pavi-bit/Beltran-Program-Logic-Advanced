package org.beltran;

public class SavingsAccount extends Account {

    public static final double MAX_PER_TRANSACTION = 50000.00;

    public SavingsAccount(String number, Customer owner, String pin, double balance,
                          boolean locked, boolean active) {
        super(number, owner, pin, balance, locked, active);
    }

    @Override
    public String getAccountType() {
        return "SAVINGS";
    }

    @Override
    public double maxWithdrawable() {
        return Math.min(getBalance(), MAX_PER_TRANSACTION);
    }
}