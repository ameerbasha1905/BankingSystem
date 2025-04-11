package com.banking;

public final class SavingsAccount extends BankAccount {
    private final double interestRate;  // In percentage

    public SavingsAccount(String accountNumber, Customer customer, double interestRate) {
        super(accountNumber, customer);
        this.interestRate = interestRate;
    }

    public void applyInterest() {
        double interest = getBalance() * interestRate / 100;
        deposit(interest);
        System.out.println("Applied interest of $" + interest + " at " + interestRate +
                           "% to savings account " + getAccountNumber());
    }

    @Override
    public void displayAccountInfo() {
        System.out.println("Account No: " + getAccountNumber());
        System.out.println("Name: " + getCustomer().name());
        System.out.println("Type: Savings");
        System.out.println("Balance: $" + getBalance());
        System.out.println("Last Transaction: " + (getLastTransaction() != null ? getLastTransaction() : "N/A"));
    }
}
