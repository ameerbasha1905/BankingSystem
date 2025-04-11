package com.banking;

import java.time.LocalDateTime;

public sealed abstract class BankAccount implements BankOperations permits SavingsAccount, CheckingAccount {
    private final String accountNumber;
    private final Customer customer;
    protected double balance;
    protected LocalDateTime lastTransaction;  // Tracks the last transaction time

    public BankAccount(String accountNumber, Customer customer) {
        this.accountNumber = accountNumber;
        this.customer = customer;
        this.balance = 0;
        this.lastTransaction = null;
    }

    public String getAccountNumber() {
        return accountNumber;
    }
    
    public Customer getCustomer() {
        return customer;
    }
    
    public LocalDateTime getLastTransaction() {
        return lastTransaction;
    }
    
    @Override
    public void deposit(double amount) {
        if (amount > 0) {
            balance += amount;
            lastTransaction = LocalDateTime.now();
            System.out.println("Deposited $" + amount + " into account " + accountNumber);
        } else {
            System.out.println("Invalid deposit amount for account " + accountNumber);
        }
    }
    
    @Override
    public void withdraw(double amount) {
        if (amount > 0 && amount <= balance) {
            balance -= amount;
            lastTransaction = LocalDateTime.now();
            System.out.println("Withdrew $" + amount + " from account " + accountNumber);
        } else {
            System.out.println("Invalid withdrawal or insufficient funds in account " + accountNumber);
        }
    }
    
    @Override
    public double getBalance() {
        return balance;
    }
    
    // Abstract method to display account details.
    public abstract void displayAccountInfo();
}
