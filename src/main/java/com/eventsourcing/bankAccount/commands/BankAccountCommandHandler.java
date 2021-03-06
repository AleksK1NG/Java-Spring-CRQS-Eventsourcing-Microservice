package com.eventsourcing.bankAccount.commands;


import com.eventsourcing.bankAccount.domain.BankAccountAggregate;
import com.eventsourcing.es.EventStoreDB;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.sleuth.annotation.NewSpan;
import org.springframework.cloud.sleuth.annotation.SpanTag;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Slf4j
@Service
public class BankAccountCommandHandler implements BankAccountCommandService {

    private final EventStoreDB eventStoreDB;
    private static final String SERVICE_NAME = "microservice";

    @Override
    @NewSpan
    @Retry(name = SERVICE_NAME)
    @CircuitBreaker(name = SERVICE_NAME)
    public String handle(@SpanTag("command") CreateBankAccountCommand command) {
        final var aggregate = new BankAccountAggregate(command.aggregateID());
        aggregate.createBankAccount(command.email(), command.address(), command.userName());
        eventStoreDB.save(aggregate);

        log.info("(CreateBankAccountCommand) aggregate: {}", aggregate);
        return aggregate.getId();
    }

    @Override
    @NewSpan
    @Retry(name = SERVICE_NAME)
    @CircuitBreaker(name = SERVICE_NAME)
    public void handle(@SpanTag("command") ChangeEmailCommand command) {
        final var aggregate = eventStoreDB.load(command.aggregateID(), BankAccountAggregate.class);
        aggregate.changeEmail(command.newEmail());
        eventStoreDB.save(aggregate);
        log.info("(ChangeEmailCommand) aggregate: {}", aggregate);
    }

    @Override
    @NewSpan
    @Retry(name = SERVICE_NAME)
    @CircuitBreaker(name = SERVICE_NAME)
    public void handle(@SpanTag("command") ChangeAddressCommand command) {
        final var aggregate = eventStoreDB.load(command.aggregateID(), BankAccountAggregate.class);
        aggregate.changeAddress(command.newAddress());
        eventStoreDB.save(aggregate);
        log.info("(ChangeAddressCommand) aggregate: {}", aggregate);
    }

    @Override
    @NewSpan
    @Retry(name = SERVICE_NAME)
    @CircuitBreaker(name = SERVICE_NAME)
    public void handle(@SpanTag("command") DepositAmountCommand command) {
        final var aggregate = eventStoreDB.load(command.aggregateID(), BankAccountAggregate.class);
        aggregate.depositBalance(command.amount());
        eventStoreDB.save(aggregate);
        log.info("(DepositAmountCommand) aggregate: {}", aggregate);
    }
}
