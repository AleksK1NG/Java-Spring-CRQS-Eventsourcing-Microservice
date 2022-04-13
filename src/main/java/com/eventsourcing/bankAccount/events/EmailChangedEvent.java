package com.eventsourcing.bankAccount.events;

import com.eventsourcing.bankAccount.domain.BankAccountAggregate;
import com.eventsourcing.es.BaseEvent;
import lombok.Builder;
import lombok.Data;

@Data
public class EmailChangedEvent extends BaseEvent {
    public static final String EMAIL_CHANGED_V1 = "EMAIL_CHANGED_V1";
    public static final String AGGREGATE_TYPE = BankAccountAggregate.AGGREGATE_TYPE;

    private String newEmail;

    @Builder
    public EmailChangedEvent(String aggregateId, String newEmail) {
        super(aggregateId);
        this.newEmail = newEmail;
    }
}