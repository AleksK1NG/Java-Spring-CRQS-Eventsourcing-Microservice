package com.eventsourcing.bankAccount.commands;

import java.math.BigDecimal;

public record DepositAmountCommand(String aggregateID, BigDecimal amount) {
}
