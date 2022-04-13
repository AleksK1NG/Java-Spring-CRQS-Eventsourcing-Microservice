package com.eventsourcing.bankAccount.commands;

public record ChangeAddressCommand(String aggregateID, String newAddress) {
}
