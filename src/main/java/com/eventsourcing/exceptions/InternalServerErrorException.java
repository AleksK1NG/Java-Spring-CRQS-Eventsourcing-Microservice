package com.eventsourcing.exceptions;

public class InternalServerErrorException extends RuntimeException{
    public InternalServerErrorException() {
        super();
    }

    public InternalServerErrorException(String message) {
        super(message);
    }
}
