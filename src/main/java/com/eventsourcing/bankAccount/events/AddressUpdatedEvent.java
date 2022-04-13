package com.eventsourcing.bankAccount.events;

import com.eventsourcing.bankAccount.domain.BankAccountAggregate;
import com.eventsourcing.es.BaseEvent;
import lombok.Builder;
import lombok.Data;

@Data
public class AddressUpdatedEvent extends BaseEvent {
    public static final String ADDRESS_UPDATED_V1 = "ADDRESS_UPDATED_V1";
    public static final String AGGREGATE_TYPE = BankAccountAggregate.AGGREGATE_TYPE;

    @Builder
    public AddressUpdatedEvent(String aggregateId, String newAddress) {
        super(aggregateId);
        this.newAddress = newAddress;
    }

    private String newAddress;
}