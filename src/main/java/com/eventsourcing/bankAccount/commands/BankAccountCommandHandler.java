package com.eventsourcing.bankAccount.commands;


import com.eventsourcing.bankAccount.domain.BankAccountAggregate;
import com.eventsourcing.es.EventStoreDB;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Slf4j
@Service
public class BankAccountCommandHandler implements BankAccountCommandService{

    private final EventStoreDB eventStoreDB;

    @Override
    public String handle(CreateBankAccountCommand command) {
        final var aggregate = new BankAccountAggregate(command.aggregateID());
        aggregate.createBankAccount(command.email(), command.address(), command.userName());
        eventStoreDB.save(aggregate);

        log.info("(CreateBankAccountCommand) aggregate: {}", aggregate);
        return aggregate.getId();
    }

    @Override
    public void handle(ChangeEmailCommand command) {
        final var aggregate = eventStoreDB.load(command.aggregateID(), BankAccountAggregate.class);
        aggregate.changeEmail(command.newEmail());
        eventStoreDB.save(aggregate);
        log.info("(ChangeEmailCommand) aggregate: {}", aggregate);
    }

    @Override
    public void handle(ChangeAddressCommand command) {
        final var aggregate = eventStoreDB.load(command.aggregateID(), BankAccountAggregate.class);
        aggregate.changeAddress(command.newAddress());
        eventStoreDB.save(aggregate);
        log.info("(ChangeAddressCommand) aggregate: {}", aggregate);
    }

    @Override
    public void handle(DepositAmountCommand command) {
        final var aggregate = eventStoreDB.load(command.aggregateID(), BankAccountAggregate.class);
        aggregate.depositBalance(command.amount());
        eventStoreDB.save(aggregate);
        log.info("(DepositAmountCommand) aggregate: {}", aggregate);
    }
}
