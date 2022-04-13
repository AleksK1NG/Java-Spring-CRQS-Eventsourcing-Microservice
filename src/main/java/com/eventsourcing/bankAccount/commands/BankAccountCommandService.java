package com.eventsourcing.bankAccount.commands;

public interface BankAccountCommandService {
    String handle(CreateBankAccountCommand command);

    void handle(ChangeEmailCommand command);

    void handle(ChangeAddressCommand command);

    void handle(DepositAmountCommand command);
}
