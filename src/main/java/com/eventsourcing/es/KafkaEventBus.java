package com.eventsourcing.es;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class KafkaEventBus implements EventBus {
    private final KafkaTemplate<String, byte[]> kafkaTemplate;

    @Value(value = "${order.kafka.topics.bank-account-event-store:bank-account-event-store}")
    private String bankAccountTopicName;

    @Override
    public void publish(List<Event> events) {
        final byte[] eventsBytes = SerializerUtils.serializeToJsonBytes(events.toArray(new Event[]{}));
        final ProducerRecord<String, byte[]> record = new ProducerRecord<>(bankAccountTopicName, eventsBytes);

        try {
            kafkaTemplate.send(record).get(3000, TimeUnit.MILLISECONDS);
            log.info("publishing kafka record value >>>>> {}", new String(record.value()));

        } catch (Exception ex) {
            log.error("(KafkaEventBus) publish get", ex);
            throw new RuntimeException(ex);
        }
    }
}
