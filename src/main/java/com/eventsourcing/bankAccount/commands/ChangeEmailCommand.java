package com.eventsourcing.bankAccount.commands;

public record ChangeEmailCommand(String aggregateID, String newEmail) {
}
