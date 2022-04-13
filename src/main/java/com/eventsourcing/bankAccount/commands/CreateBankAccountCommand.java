package com.eventsourcing.bankAccount.commands;

public record CreateBankAccountCommand(String aggregateID, String email, String userName, String address) {
}
