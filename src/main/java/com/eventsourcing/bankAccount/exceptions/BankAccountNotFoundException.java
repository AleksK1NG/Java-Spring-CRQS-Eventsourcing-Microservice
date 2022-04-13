package com.eventsourcing.bankAccount.exceptions;

public class BankAccountNotFoundException extends RuntimeException {

    public BankAccountNotFoundException() {
    }

    public BankAccountNotFoundException(String id) {
        super("bank account not found id: " + id);
    }
}