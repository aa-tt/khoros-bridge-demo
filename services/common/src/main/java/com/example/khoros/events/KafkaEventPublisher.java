package com.example.khoros.events;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

public final class KafkaEventPublisher implements AutoCloseable {
    private final KafkaProducer<String, String> producer;
    private final ObjectMapper objectMapper;
    private final String topic;

    public KafkaEventPublisher(String bootstrapServers, String topic, String clientId, ObjectMapper objectMapper) {
        this.producer = new KafkaProducer<>(KafkaSettings.clientProperties(bootstrapServers, clientId, null));
        this.objectMapper = objectMapper;
        this.topic = topic;
    }

    public void publish(String key, EventEnvelope event) {
        try {
            String payload = objectMapper.writeValueAsString(event);
            producer.send(new ProducerRecord<>(topic, key, payload)).get();
        } catch (Exception exception) {
            throw new IllegalStateException("Could not publish event to MSK", exception);
        }
    }

    @Override
    public void close() {
        producer.close(Duration.ofSeconds(5));
    }
}
