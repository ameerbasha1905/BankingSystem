package com.banking;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Scanner;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class BankManager {

    // Collection to store accounts, where key is the account number
    private static final Map<Integer, BankAccount> accounts = new HashMap<>();
    // List to record all transactions made in the system
    private static final List<Transaction> transactions = new ArrayList<>();
    // Counter for generating the next unique account number
    private static int nextAccountNumber = 1001;

    public static void main(String[] args) {
        // Localization: Load the welcome message from "Messages.properties".
        // Ensure that the "Messages.properties" file is placed in the src folder.
        Locale currentLocale = Locale.getDefault();
        ResourceBundle messages = ResourceBundle.getBundle("Messages", currentLocale);
        System.out.println(messages.getString("greeting"));

        Scanner scanner = new Scanner(System.in);
        boolean running = true;
        
        // Main menu loop
        while (running) {
            System.out.println("\n==== BankManager - Java Console Banking ====");
            System.out.println("1. Create Account");
            System.out.println("2. Deposit Money");
            System.out.println("3. Withdraw Money");
            System.out.println("4. View Account Details");
            System.out.println("5. Advanced Report (Lambdas & Streams)");
            System.out.println("6. Delete Account");
            System.out.println("7. Concurrency Demo (ExecutorService/Callables)");
            System.out.println("8. Show Sorted Accounts (by Customer Name)");
            System.out.println("9. Exit");
            System.out.print("\n> ");

            int choice;
            try {
                choice = Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number between 1 and 9.");
                continue;
            }
            
            // Switch-case for menu selections
            switch (choice) {
                case 1 -> createAccount(scanner);
                case 2 -> depositMoney(scanner);
                case 3 -> withdrawMoney(scanner);
                case 4 -> viewAccountDetails(scanner);
                case 5 -> showAdvancedReport();
                case 6 -> deleteAccount(scanner);
                case 7 -> {
                    try {
                        showConcurrencyDemo();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
                case 8 -> showSortedAccounts();
                case 9 -> {
                    System.out.println("Exiting application.");
                    running = false;
                }
                default -> System.out.println("Invalid choice, try again.");
            }
        }
        scanner.close();
    }
    
    /**
     * Method to create a new bank account.
     * It collects user input for the customer's name, role, and account type, then instantiates
     * either a SavingsAccount or CheckingAccount.
     */
    private static void createAccount(Scanner scanner) {
        System.out.print("Enter Name: ");
        String name = scanner.nextLine();
        System.out.print("Enter Role (e.g., ADMIN, TECHNICIAN): ");
        String role = scanner.nextLine().trim().toUpperCase();
        System.out.print("Choose Account Type [1] Savings [2] Current: ");
        int type;
        try {
            type = Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            System.out.println("Invalid account type selection.");
            return;
        }
        // Create a new Customer record
        Customer customer = new Customer("C" + nextAccountNumber, name, role);
        BankAccount account;
        // Create an account based on the chosen type
        if (type == 1) {
            account = new SavingsAccount(String.valueOf(nextAccountNumber), customer, 3.0);
        } else if (type == 2) {
            account = new CheckingAccount(String.valueOf(nextAccountNumber), customer, 500.0);
        } else {
            System.out.println("Invalid account type selection.");
            return;
        }
        // Store the new account and increment the account counter
        accounts.put(nextAccountNumber, account);
        System.out.println("Account created. Account No: " + nextAccountNumber);
        nextAccountNumber++;
    }
    
    /**
     * Method to deposit money into an existing account.
     * It retrieves the account based on user input, performs a deposit, and records the transaction.
     */
    private static void depositMoney(Scanner scanner) {
        System.out.print("Enter Account No: ");
        int accountNumber;
        try {
            accountNumber = Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            System.out.println("Invalid account number.");
            return;
        }
        BankAccount account = accounts.get(accountNumber);
        if (account == null) {
            System.out.println("Account not found.");
            return;
        }
        System.out.print("Enter Amount: ");
        double amount;
        try {
            amount = Double.parseDouble(scanner.nextLine());
        } catch (NumberFormatException e) {
            System.out.println("Invalid amount.");
            return;
        }
        // Perform the deposit operation
        account.deposit(amount);
        // Record the deposit transaction with a timestamp
        transactions.add(new Transaction("T" + (transactions.size() + 1),
                account.getAccountNumber(), amount, TransactionType.DEPOSIT, LocalDateTime.now()));
        System.out.println("Deposit successful. New Balance: $" + account.getBalance());
    }
    
    /**
     * Method to withdraw money from an existing account.
     * It verifies that funds are available, performs the withdrawal, and records the transaction.
     */
    private static void withdrawMoney(Scanner scanner) {
        System.out.print("Enter Account No: ");
        int accountNumber;
        try {
            accountNumber = Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            System.out.println("Invalid account number.");
            return;
        }
        BankAccount account = accounts.get(accountNumber);
        if (account == null) {
            System.out.println("Account not found.");
            return;
        }
        System.out.print("Enter Amount: ");
        double amount;
        try {
            amount = Double.parseDouble(scanner.nextLine());
        } catch (NumberFormatException e) {
            System.out.println("Invalid amount.");
            return;
        }
        double initialBalance = account.getBalance();
        // Perform the withdrawal
        account.withdraw(amount);
        if (account.getBalance() == initialBalance) {
            System.out.println("Insufficient funds.");
        } else {
            // Record the withdrawal transaction
            transactions.add(new Transaction("T" + (transactions.size() + 1),
                    account.getAccountNumber(), amount, TransactionType.WITHDRAWAL, LocalDateTime.now()));
            System.out.println("Withdrawal successful. New Balance: $" + account.getBalance());
        }
    }
    
    /**
     * Method to display details of a specific account.
     */
    private static void viewAccountDetails(Scanner scanner) {
        System.out.print("Enter Account No: ");
        int accountNumber;
        try {
            accountNumber = Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            System.out.println("Invalid account number.");
            return;
        }
        BankAccount account = accounts.get(accountNumber);
        if (account == null) {
            System.out.println("Account not found.");
        } else {
            account.displayAccountInfo();
        }
    }
    
    /**
     * Method to delete an account.
     * Role-based restrictions are enforced here: Technicians are not permitted to remove accounts.
     */
    private static void deleteAccount(Scanner scanner) {
        System.out.print("Enter Your Role (e.g., ADMIN, TECHNICIAN): ");
        String userRole = scanner.nextLine().trim().toUpperCase();
        if (userRole.equals("TECHNICIAN")) {
            System.out.println("Technicians are not permitted to remove existing accounts.");
            return;
        }
        System.out.print("Enter Account No to delete: ");
        int accountNumber;
        try {
            accountNumber = Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            System.out.println("Invalid account number.");
            return;
        }
        BankAccount account = accounts.get(accountNumber);
        if (account == null) {
            System.out.println("Account not found.");
        } else {
            accounts.remove(accountNumber);
            System.out.println("Account " + accountNumber + " successfully removed.");
        }
    }
    
    /**
     * Method to show sorted accounts based on customer name.
     * This demonstrates the use of generic collections and Comparator.comparing() for sorting.
     */
    private static void showSortedAccounts() {
        System.out.println("Accounts Sorted by Customer Name:");
        List<BankAccount> sortedAccounts = accounts.values().stream()
            .sorted(Comparator.comparing(acc -> acc.getCustomer().name()))
            .collect(Collectors.toList());
        sortedAccounts.forEach(acc -> System.out.println("Account " + acc.getAccountNumber()
            + " - " + acc.getCustomer().name()));
    }
    
    /**
     * Advanced Report section that demonstrates the use of lambdas, streams, collectors,
     * switch expressions, and the Date/Time API.
     */
    private static void showAdvancedReport() {
        System.out.println("\n==== Advanced Report ====");
        
        // ----- Lambdas -----
        // Consumer: Prints transaction details.
        Consumer<Transaction> printTx = tx ->
            System.out.println("Transaction " + tx.transactionId() + ": $" + tx.amount() + " [" + tx.type() + "]");
        // Predicate: Checks if a deposit is less than $100.
        Predicate<Transaction> isSmallDeposit = tx -> tx.type() == TransactionType.DEPOSIT && tx.amount() < 100;
        // Supplier: Provides a default Customer object.
        Supplier<Customer> defaultCustomer = () -> new Customer("C000", "Default User", "DEFAULT_ROLE");
        // Function: Formats a numerical balance into a currency string.
        Function<Double, String> formatCurrency = bal -> String.format("$%,.2f", bal);
        
        System.out.println("Default Customer: " + defaultCustomer.get());
        System.out.println("Formatted Balance for $1030: " + formatCurrency.apply(1030.0));
        System.out.println("Is a $50 deposit small? " +
            isSmallDeposit.test(new Transaction("T999", "Dummy", 50, TransactionType.DEPOSIT, LocalDateTime.now())));
        
        // ----- Streams – Terminal Operations -----
        // Count the total number of deposit transactions.
        long depositCount = transactions.stream()
            .filter(tx -> tx.type() == TransactionType.DEPOSIT)
            .count();
        System.out.println("Total Deposit Transactions: " + depositCount);
        
        // Find and print the first deposit transaction.
        transactions.stream()
            .filter(tx -> tx.type() == TransactionType.DEPOSIT)
            .findFirst()
            .ifPresent(tx -> System.out.println("First Deposit Transaction: " + tx));
        
        // Find and print the smallest deposit transaction.
        transactions.stream()
            .filter(tx -> tx.type() == TransactionType.DEPOSIT)
            .min(Comparator.comparing(Transaction::amount))
            .ifPresent(tx -> System.out.println("Smallest Deposit: $" + tx.amount()));
        
        // Find any deposit transaction.
        transactions.stream()
            .filter(tx -> tx.type() == TransactionType.DEPOSIT)
            .findAny()
            .ifPresent(tx -> System.out.println("Found any deposit: " + tx));
        
        // Check if all deposits exceed $10.
        boolean allDepositsAbove10 = transactions.stream()
            .filter(tx -> tx.type() == TransactionType.DEPOSIT)
            .allMatch(tx -> tx.amount() > 10);
        System.out.println("All deposits above $10: " + allDepositsAbove10);
        
        // Check if any deposit exceeds $1000 and if no transaction is negative.
        boolean anyDepositLarge = transactions.stream()
            .filter(tx -> tx.type() == TransactionType.DEPOSIT)
            .anyMatch(tx -> tx.amount() > 1000);
        boolean noneNegative = transactions.stream()
            .noneMatch(tx -> tx.amount() < 0);
        System.out.println("Any deposit exceeding $1000: " + anyDepositLarge);
        System.out.println("No negative transactions: " + noneNegative);
        
        // ----- Collectors -----
        // Group transactions by their type.
        var groupedByType = transactions.stream()
            .collect(Collectors.groupingBy(Transaction::type));
        System.out.println("Transactions Grouped by Type: " + groupedByType);
        
        // Partition deposit transactions based on whether they exceed $500.
        var partitionedDeposits = transactions.stream()
            .filter(tx -> tx.type() == TransactionType.DEPOSIT)
            .collect(Collectors.partitioningBy(tx -> tx.amount() > 500));
        System.out.println("Deposits Partitioned (amount > 500): " + partitionedDeposits);
        
        // Convert transactions into a Map with transactionId as the key.
        var transactionsMap = transactions.stream()
            .collect(Collectors.toMap(Transaction::transactionId, tx -> tx));
        System.out.println("Transactions as Map: " + transactionsMap);
        
        // ----- Intermediate Operations -----
        // Extract limited, unique deposit transaction IDs.
        List<String> limitedTxIds = transactions.stream()
            .filter(tx -> tx.type() == TransactionType.DEPOSIT)
            .map(Transaction::transactionId)
            .distinct()
            .sorted()
            .limit(2)
            .collect(Collectors.toList());
        System.out.println("Limited (2) Unique Deposit Transaction IDs: " + limitedTxIds);
        
        // Sort transactions by timestamp and print each using the Consumer lambda.
        List<Transaction> sortedTxs = transactions.stream()
            .sorted(Comparator.comparing(Transaction::timestamp))
            .collect(Collectors.toList());
        System.out.println("Transactions Sorted by Timestamp:");
        sortedTxs.forEach(printTx);
        
        // ----- Switch Expression with Pattern Matching -----
        System.out.println("Detailed Transaction Reports (Formatted):");
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        for (Transaction tx : transactions) {
            String report = switch (tx.type()) {
                case DEPOSIT ->
                    "Deposit of " + formatCurrency.apply(tx.amount()) +
                    " on " + tx.timestamp().format(dtf);
                case WITHDRAWAL ->
                    "Withdrawal of " + formatCurrency.apply(tx.amount()) +
                    " on " + tx.timestamp().format(dtf);
            };
            System.out.println(report);
        }
        
        // ----- Collections/Generics: Sorted Accounts by Customer Name -----
        System.out.println("Accounts Sorted by Customer Name:");
        List<BankAccount> sortedAccounts = accounts.values().stream()
            .sorted(Comparator.comparing(acc -> acc.getCustomer().name()))
            .collect(Collectors.toList());
        sortedAccounts.forEach(acc -> System.out.println("Account " + acc.getAccountNumber() 
            + " - " + acc.getCustomer().name()));
        
        // ----- NIO.2 File I/O -----
        String fileSummary = "\n--- Account Summary ---\n";
        for (BankAccount acc : accounts.values()) {
            fileSummary += "Account No: " + acc.getAccountNumber() + "\n" +
                           "Name: " + acc.getCustomer().name() + "\n" +
                           "Balance: " + formatCurrency.apply(acc.getBalance()) + "\n" +
                           "Last Transaction: " + (acc.getLastTransaction() != null 
                              ? acc.getLastTransaction().format(dtf)
                              : "N/A") + "\n\n";
        }
        try {
            Files.writeString(Path.of("account_summary.txt"), fileSummary, StandardCharsets.UTF_8);
            System.out.println("Account summary written to account_summary.txt");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Optional Advanced Concurrency Demo method.
     * Demonstrates the use of ExecutorService and Callable tasks to process transactions concurrently.
     */
    private static void showConcurrencyDemo() throws Exception {
        System.out.println("\n==== Concurrency Demo ====");
        
        // Create an ExecutorService with a fixed thread pool
        ExecutorService executor = Executors.newFixedThreadPool(2);
        List<Callable<String>> tasks = new ArrayList<>();

        // Task 1: Deposit $200 into account 1001 (if it exists)
        tasks.add(() -> {
            BankAccount account = accounts.get(1001);
            if (account != null) {
                account.deposit(200);
                transactions.add(new Transaction("T" + (transactions.size() + 1),
                        account.getAccountNumber(), 200, TransactionType.DEPOSIT, LocalDateTime.now()));
                return "Deposited $200 into account " + account.getAccountNumber();
            } else {
                return "Account 1001 not found for deposit";
            }
        });

        // Task 2: Withdraw $100 from account 1001 (if it exists)
        tasks.add(() -> {
            BankAccount account = accounts.get(1001);
            if (account != null) {
                double initBalance = account.getBalance();
                account.withdraw(100);
                if (account.getBalance() != initBalance) {
                    transactions.add(new Transaction("T" + (transactions.size() + 1),
                            account.getAccountNumber(), 100, TransactionType.WITHDRAWAL, LocalDateTime.now()));
                    return "Withdrew $100 from account " + account.getAccountNumber();
                } else {
                    return "Insufficient funds in account " + account.getAccountNumber();
                }
            } else {
                return "Account 1001 not found for withdrawal";
            }
        });

        // Execute tasks concurrently and print the results
        List<Future<String>> results = executor.invokeAll(tasks);
        for (Future<String> future : results) {
            System.out.println("Concurrency result: " + future.get());
        }
        executor.shutdown();
    }
}
