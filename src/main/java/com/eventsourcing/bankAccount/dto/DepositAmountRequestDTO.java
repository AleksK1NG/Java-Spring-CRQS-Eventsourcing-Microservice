package com.eventsourcing.bankAccount.dto;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

public record DepositAmountRequestDTO(@Min(value = 300, message = "minimal amount is 300") @NotNull BigDecimal amount) {
}