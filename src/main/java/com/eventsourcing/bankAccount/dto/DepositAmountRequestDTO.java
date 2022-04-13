package com.eventsourcing.bankAccount.dto;

import java.math.BigDecimal;

public record DepositAmountRequestDTO(BigDecimal amount) {
}