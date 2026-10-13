package com.assignment.order.destination;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Service
class MockDestinationConsumer {
    private static final Logger LOGGER = LoggerFactory.getLogger(MockDestinationConsumer.class);

    @KafkaListener(topics = "${app.processed-topic}", groupId = "${app.consumer-group}")
    void receive(ConsumerRecord<String, String> record) {
        Header eventId = record.headers().lastHeader("eventId");
        String correlationId = eventId == null ? "missing" : new String(eventId.value(), StandardCharsets.UTF_8);
        LOGGER.info("destination_received eventId={} order={}", correlationId, record.value());
    }
}
