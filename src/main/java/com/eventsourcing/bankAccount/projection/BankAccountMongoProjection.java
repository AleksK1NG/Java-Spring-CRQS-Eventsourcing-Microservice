package com.eventsourcing.bankAccount.projection;


import com.eventsourcing.bankAccount.domain.BankAccountDocument;
import com.eventsourcing.bankAccount.events.AddressUpdatedEvent;
import com.eventsourcing.bankAccount.events.BalanceDepositedEvent;
import com.eventsourcing.bankAccount.events.BankAccountCreatedEvent;
import com.eventsourcing.bankAccount.events.EmailChangedEvent;
import com.eventsourcing.bankAccount.repository.BankAccountMongoRepository;
import com.eventsourcing.es.Event;
import com.eventsourcing.es.EventStoreDB;
import com.eventsourcing.es.Projection;
import com.eventsourcing.es.SerializerUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.listener.adapter.ConsumerRecordMetadata;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class BankAccountMongoProjection implements Projection {

    private final BankAccountMongoRepository mongoRepository;
    private final EventStoreDB eventStoreDB;

    @Value(value = "${microservice.kafka.topics.bank-account-event-store:bank-account-event-store}")
    private String bankAccountTopicName;


    @KafkaListener(topics = {"${microservice.kafka.topics.bank-account-event-store}"},
            groupId = "${microservice.kafka.groupId}",
            concurrency = "${microservice.kafka.default-concurrency}")
    public void bankAccountMongoProjectionListener(@Payload byte[] data, ConsumerRecordMetadata meta, Acknowledgment ack) {
        log.info("(BankAccountMongoProjection) topic: {}, offset: {}, partition: {}, timestamp: {}", meta.topic(), meta.offset(), meta.partition(), meta.timestamp());
        log.info("(BankAccountMongoProjection) data: {}", new String(data));

        try {
            final Event[] events = SerializerUtils.deserializeEventsFromJsonBytes(data);
            this.processEvents( Arrays.stream(events).toList());
            ack.acknowledge();
            log.info("ack events: {}", Arrays.toString(events));
        } catch (Exception e) {
            ack.nack(100);
            log.error("(BankAccountMongoProjection) topic: {}, offset: {}, partition: {}, timestamp: {}", meta.topic(), meta.offset(), meta.partition(), meta.timestamp());
            log.error("bankAccountMongoProjectionListener: {}", e.getMessage());
        }
    }

    private void processEvents(List<Event> events) {
        try {
            events.forEach(event -> this.when(event));
        } catch (Exception ex) {
            // TODO: delete from mongo, get from eventStore, upsert to mongo
        }
    }

    @Override
    public void when(Event event) {
        final var aggregateId = event.getAggregateId();
        log.info("(when) >>>>> aggregateId: {}", aggregateId);

        switch (event.getEventType()) {
            case BankAccountCreatedEvent.BANK_ACCOUNT_CREATED_V1 ->
                    handle(SerializerUtils.deserializeFromJsonBytes(event.getData(), BankAccountCreatedEvent.class));
            case EmailChangedEvent.EMAIL_CHANGED_V1 ->
                    handle(SerializerUtils.deserializeFromJsonBytes(event.getData(), EmailChangedEvent.class));
            case AddressUpdatedEvent.ADDRESS_UPDATED_V1 ->
                    handle(SerializerUtils.deserializeFromJsonBytes(event.getData(), AddressUpdatedEvent.class));
            case BalanceDepositedEvent.BALANCE_DEPOSITED ->
                    handle(SerializerUtils.deserializeFromJsonBytes(event.getData(), BalanceDepositedEvent.class));
            default -> log.error("unknown event type: {}", event.getEventType());
        }
    }


    private void handle(BankAccountCreatedEvent event) {
        log.info("(when) BankAccountCreatedEvent: {}, aggregateID: {}", event, event.getAggregateId());

        final var document = BankAccountDocument.builder()
                .aggregateId(event.getAggregateId())
                .email(event.getEmail())
                .address(event.getAddress())
                .userName(event.getUserName())
                .balance(BigDecimal.valueOf(0))
                .build();

        final var insert = mongoRepository.insert(document);
        log.info("(BankAccountCreatedEvent) insert: {}", insert);
    }

    private void handle(EmailChangedEvent event) {
        log.info("(when) EmailChangedEvent: {}, aggregateID: {}", event, event.getAggregateId());
        Optional<BankAccountDocument> documentOptional = mongoRepository.findByAggregateId(event.getAggregateId());
        if (documentOptional.isEmpty()) throw new RuntimeException("Bank Account Document not found id: {}" + event.getAggregateId());

        final var document = documentOptional.get();
        document.setEmail(event.getNewEmail());
        mongoRepository.save(document);
    }

    private void handle(AddressUpdatedEvent event) {
        log.info("(when) AddressUpdatedEvent: {}, aggregateID: {}", event, event.getAggregateId());
        Optional<BankAccountDocument> documentOptional = mongoRepository.findByAggregateId(event.getAggregateId());
        if (documentOptional.isEmpty()) throw new RuntimeException("Bank Account Document not found id: {}" + event.getAggregateId());

        final var document = documentOptional.get();
        document.setAddress(event.getNewAddress());
        mongoRepository.save(document);
    }

    private void handle(BalanceDepositedEvent event) {
        log.info("(when) BalanceDepositedEvent: {}, aggregateID: {}", event, event.getAggregateId());
        Optional<BankAccountDocument> documentOptional = mongoRepository.findByAggregateId(event.getAggregateId());
        if (documentOptional.isEmpty()) throw new RuntimeException("Bank Account Document not found id: {}" + event.getAggregateId());

        final var document = documentOptional.get();
        final var balance = document.getBalance();
        final var newBalance = balance.add(event.getAmount());
        document.setBalance(newBalance);
        mongoRepository.save(document);
    }
}
