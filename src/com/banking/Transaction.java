package com.banking;

import java.time.LocalDateTime;

public record Transaction(String transactionId, String accountNumber, double amount, 
                          TransactionType type, LocalDateTime timestamp) { }
