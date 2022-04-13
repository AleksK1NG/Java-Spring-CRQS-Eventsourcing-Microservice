package com.eventsourcing.bankAccount.dto;

import java.math.BigDecimal;

public record BankAccountResponseDTO(
        String aggregateId,
        String email,
        String address,
        String userName,
        BigDecimal balance) {
}