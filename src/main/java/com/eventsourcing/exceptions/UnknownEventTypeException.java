package com.eventsourcing.exceptions;

public class UnknownEventTypeException extends RuntimeException {
    public UnknownEventTypeException() {
    }

    public UnknownEventTypeException(String eventType) {
        super("unknown event type: " + eventType);
    }
}
