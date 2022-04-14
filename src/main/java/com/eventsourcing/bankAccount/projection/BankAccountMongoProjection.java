package com.eventsourcing.bankAccount.projection;


import com.eventsourcing.es.Event;
import com.eventsourcing.es.SerializerUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.listener.adapter.ConsumerRecordMetadata;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
@Slf4j
@RequiredArgsConstructor
public class BankAccountMongoProjection {

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
            ack.acknowledge();
            log.info("ack events: {}", Arrays.toString(events));
        } catch (Exception e) {
            ack.nack(100);
            log.error("(BankAccountMongoProjection) topic: {}, offset: {}, partition: {}, timestamp: {}", meta.topic(), meta.offset(), meta.partition(), meta.timestamp());
            log.error("bankAccountMongoProjectionListener: {}", e.getMessage());
        }
    }
}
