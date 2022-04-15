package com.eventsourcing.bankAccount.exceptions;

public class InvalidEmailException extends RuntimeException {
    public InvalidEmailException() {
        super();
    }

    public InvalidEmailException(String email) {
        super("invalid email address: " + email);
    }
}
