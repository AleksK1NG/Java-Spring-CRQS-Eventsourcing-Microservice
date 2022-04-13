package com.eventsourcing.bankAccount.dto;

public record CreateBankAccountRequestDTO(
        String email,
        String address,
        String userName) {
}
