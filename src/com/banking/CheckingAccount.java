package com.banking;

import java.time.LocalDateTime;

public final class CheckingAccount extends BankAccount {
    private final double overdraftLimit;

    public CheckingAccount(String accountNumber, Customer customer, double overdraftLimit) {
        super(accountNumber, customer);
        this.overdraftLimit = overdraftLimit;
    }

    @Override
    public void withdraw(double amount) {
        if (amount > 0 && (getBalance() + overdraftLimit) >= amount) {
            balance -= amount;
            lastTransaction = LocalDateTime.now();
            System.out.println("Withdrew $" + amount + " from checking account " + getAccountNumber());
        } else {
            System.out.println("Invalid withdrawal or overdraft limit exceeded for account " + getAccountNumber());
        }
    }

    @Override
    public void displayAccountInfo() {
        System.out.println("Account No: " + getAccountNumber());
        System.out.println("Name: " + getCustomer().name());
        System.out.println("Type: Current");
        System.out.println("Balance: $" + getBalance());
        System.out.println("Last Transaction: " + (getLastTransaction() != null ? getLastTransaction() : "N/A"));
        System.out.println("Overdraft Limit: $" + overdraftLimit);
    }
}
