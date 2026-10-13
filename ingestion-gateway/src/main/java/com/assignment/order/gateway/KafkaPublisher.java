package com.assignment.order.gateway;

import com.assignment.order.core.SourceSystem;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
class KafkaPublisher {
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final String sourceATopic;
    private final String sourceBTopic;

    KafkaPublisher(
            KafkaTemplate<String, String> kafkaTemplate,
            @Value("${app.raw-topic.source-a}") String sourceATopic,
            @Value("${app.raw-topic.source-b}") String sourceBTopic
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.sourceATopic = sourceATopic;
        this.sourceBTopic = sourceBTopic;
    }

    String publish(SourceSystem sourceSystem, String orderReference, String rawOrder) {
        String eventId = UUID.randomUUID().toString();
        String topic = sourceSystem == SourceSystem.SOURCE_A ? sourceATopic : sourceBTopic;
        ProducerRecord<String, String> record = new ProducerRecord<>(topic, sourceSystem + ":" + orderReference, rawOrder);
        record.headers().add(new RecordHeader("eventId", eventId.getBytes(StandardCharsets.UTF_8)));
        record.headers().add(new RecordHeader("sourceSystem", sourceSystem.name().getBytes(StandardCharsets.UTF_8)));
        record.headers().add(new RecordHeader("receivedAt", Instant.now().toString().getBytes(StandardCharsets.UTF_8)));
        try {
            kafkaTemplate.send(record).get(10, TimeUnit.SECONDS);
            return eventId;
        } catch (Exception exception) {
            throw new KafkaPublishException("Kafka did not acknowledge the order publication", exception);
        }
    }
}
