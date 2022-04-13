package com.eventsourcing.bankAccount.domain;

import com.eventsourcing.bankAccount.events.AddressUpdatedEvent;
import com.eventsourcing.bankAccount.events.BalanceDepositedEvent;
import com.eventsourcing.bankAccount.events.BankAccountCreatedEvent;
import com.eventsourcing.bankAccount.events.EmailChangedEvent;
import com.eventsourcing.es.AggregateRoot;
import com.eventsourcing.es.Event;
import com.eventsourcing.es.SerializerUtils;
import com.eventsourcing.es.exceptions.InvalidEventTypeException;
import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class BankAccountAggregate extends AggregateRoot {


    public static final String AGGREGATE_TYPE = "BankAccountAggregate";

    public BankAccountAggregate(String id) {
        super(id, AGGREGATE_TYPE);
    }

    private String email;
    private String userName;
    private String address;
    private BigDecimal balance;


    @Override
    public void when(Event event) {
        switch (event.getEventType()) {
            case BankAccountCreatedEvent.BANK_ACCOUNT_CREATED_V1 ->
                    handle(SerializerUtils.deserializeFromJsonBytes(event.getData(), BankAccountCreatedEvent.class));
            case EmailChangedEvent.EMAIL_CHANGED_V1 ->
                    handle(SerializerUtils.deserializeFromJsonBytes(event.getData(), EmailChangedEvent.class));
            case AddressUpdatedEvent.ADDRESS_UPDATED_V1 ->
                    handle(SerializerUtils.deserializeFromJsonBytes(event.getData(), AddressUpdatedEvent.class));
            case BalanceDepositedEvent.BALANCE_DEPOSITED ->
                    handle(SerializerUtils.deserializeFromJsonBytes(event.getData(), BalanceDepositedEvent.class));
            default -> throw new InvalidEventTypeException(event.getEventType());
        }
    }

    private void handle(final BankAccountCreatedEvent event) {
        this.email = event.getEmail();
        this.userName = event.getUserName();
        this.address = event.getAddress();
        this.balance = BigDecimal.valueOf(0);
    }

    private void handle(final EmailChangedEvent event) {
        this.email = event.getNewEmail();
    }

    private void handle(final AddressUpdatedEvent event) {
        this.address = event.getNewAddress();
    }

    private void handle(final BalanceDepositedEvent event) {
        this.balance = this.balance.add(event.getAmount());
    }

    public void createBankAccount(String email, String address, String userName) {
        final var data = BankAccountCreatedEvent.builder()
                .aggregateId(id)
                .email(email)
                .address(address)
                .userName(userName)
                .build();

        final byte[] dataBytes = SerializerUtils.serializeToJsonBytes(data);
        final var event = this.createEvent(BankAccountCreatedEvent.BANK_ACCOUNT_CREATED_V1, dataBytes, null);
        this.apply(event);
    }

    public void changeEmail(String email) {
        final var data = EmailChangedEvent.builder().aggregateId(id).newEmail(email).build();
        final byte[] dataBytes = SerializerUtils.serializeToJsonBytes(data);
        final var event = this.createEvent(EmailChangedEvent.EMAIL_CHANGED_V1, dataBytes, null);
        apply(event);

    }

    public void changeAddress(String newAddress) {
        final var data = AddressUpdatedEvent.builder().aggregateId(id).newAddress(newAddress).build();
        final byte[] dataBytes = SerializerUtils.serializeToJsonBytes(data);
        final var event = this.createEvent(AddressUpdatedEvent.ADDRESS_UPDATED_V1, dataBytes, null);
        apply(event);
    }

    public void depositBalance(BigDecimal amount) {
        final var data = BalanceDepositedEvent.builder().aggregateId(id).amount(amount).build();
        final byte[] dataBytes = SerializerUtils.serializeToJsonBytes(data);
        final var event = this.createEvent(BalanceDepositedEvent.BALANCE_DEPOSITED, dataBytes, null);
        apply(event);
    }


    @Override
    public String toString() {
        return "BankAccountAggregate{" +
                "email='" + email + '\'' +
                ", userName='" + userName + '\'' +
                ", address='" + address + '\'' +
                ", balance=" + balance +
                ", id='" + id + '\'' +
                ", type='" + type + '\'' +
                ", version=" + version +
                ", changes=" + changes.size() +
                '}';
    }
}