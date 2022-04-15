package com.eventsourcing.bankAccount.exceptions;

public class InvalidAddressException extends RuntimeException {
    public InvalidAddressException() {
        super();
    }

    public InvalidAddressException(String address) {
        super("invalid address: " + address);
    }
}
