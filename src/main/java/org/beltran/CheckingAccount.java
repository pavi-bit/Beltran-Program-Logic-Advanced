package org.beltran;

public class CheckingAccount extends Account {

    public CheckingAccount(String number, Customer owner, String pin, double balance,
                           boolean locked, boolean active) {
        super(number, owner, pin, balance, locked, active);
    }

    @Override
    public String getAccountType() {
        return "CHECKING";
    }

    @Override
    public double maxWithdrawable() {
        return getBalance();
    }
}