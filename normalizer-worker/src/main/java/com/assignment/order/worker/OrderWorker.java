package com.assignment.order.worker;

import com.assignment.order.core.OrderNormalizer;
import com.assignment.order.core.ProcessedOrder;
import com.assignment.order.core.SourceSystem;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

@Service
class OrderWorker {
    private final OrderNormalizer normalizer;
    private final ObjectMapper objectMapper;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final SourceSystem sourceSystem;
    private final String processedTopic;
    private final String dlqTopic;

    OrderWorker(
            OrderNormalizer normalizer,
            ObjectMapper objectMapper,
            KafkaTemplate<String, String> kafkaTemplate,
            @Value("${app.source-system}") SourceSystem sourceSystem,
            @Value("${app.processed-topic}") String processedTopic,
            @Value("${app.dlq-topic}") String dlqTopic
    ) {
        this.normalizer = normalizer;
        this.objectMapper = objectMapper;
        this.kafkaTemplate = kafkaTemplate;
        this.sourceSystem = sourceSystem;
        this.processedTopic = processedTopic;
        this.dlqTopic = dlqTopic;
    }

    @KafkaListener(topics = "${app.input-topic}", groupId = "${app.consumer-group}")
    void process(ConsumerRecord<String, String> record) {
        try {
            ProcessedOrder processedOrder = normalizer.normalize(sourceSystem, record.value());
            ProducerRecord<String, String> output = new ProducerRecord<>(
                    processedTopic,
                    sourceSystem + ":" + processedOrder.orderReference(),
                    objectMapper.writeValueAsString(processedOrder)
            );
            copyEventId(record, output);
            kafkaTemplate.send(output).get(10, TimeUnit.SECONDS);
        } catch (Exception exception) {
            publishFailure(record, exception);
        }
    }

    private void publishFailure(ConsumerRecord<String, String> record, Exception exception) {
        try {
            ProducerRecord<String, String> failure = new ProducerRecord<>(
                    dlqTopic,
                    record.key(),
                    objectMapper.writeValueAsString(new FailureMessage(record.value(), exception.getMessage()))
            );
            copyEventId(record, failure);
            kafkaTemplate.send(failure).get(10, TimeUnit.SECONDS);
        } catch (Exception dlqException) {
            throw new IllegalStateException("Could not publish normalization failure to the DLQ", dlqException);
        }
    }

    private void copyEventId(ConsumerRecord<String, String> input, ProducerRecord<String, String> output) {
        Header eventId = input.headers().lastHeader("eventId");
        if (eventId != null) {
            output.headers().add(new RecordHeader("eventId", eventId.value()));
        }
    }
}
