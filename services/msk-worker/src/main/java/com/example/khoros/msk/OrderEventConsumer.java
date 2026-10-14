package com.example.khoros.msk;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;

@Component
public class OrderEventConsumer {
    private final KafkaConsumer<String, String> consumer;
    private final DynamoDbClient dynamoDb;
    private final String tableName;
    private final ObjectMapper mapper = new ObjectMapper();

    public OrderEventConsumer(
            @Value("${AWS_REGION:us-east-1}") String region,
            @Value("${MSK_BOOTSTRAP_SERVERS}") String brokers,
            @Value("${MSK_ORDER_TOPIC:bridge-events}") String topic,
            @Value("${MSK_GROUP_ID:order-projection}") String groupId,
            @Value("${ORDERS_TABLE}") String tableName) {
        this.tableName = tableName;
        this.dynamoDb = DynamoDbClient.builder().region(Region.of(region)).build();
        var properties = com.example.khoros.events.KafkaSettings.clientProperties(brokers, "msk-worker", groupId);
        properties.put("key.deserializer", StringDeserializer.class.getName());
        properties.put("value.deserializer", StringDeserializer.class.getName());
        this.consumer = new KafkaConsumer<>(properties);
        this.consumer.subscribe(List.of(topic));
    }

    @Scheduled(fixedDelayString = "${POLL_DELAY_MS:500}")
    public void consume() {
        for (ConsumerRecord<String, String> record : consumer.poll(Duration.ofMillis(100))) {
            try {
                JsonNode event = mapper.readTree(record.value());
                if (!"order.created".equals(event.path("eventType").asText())) {
                    consumer.commitSync();
                    continue;
                }
                String orderId = event.path("data").path("orderId").asText(record.key());
                dynamoDb.putItem(PutItemRequest.builder().tableName(tableName).item(Map.of(
                        "PK", AttributeValue.fromS("ORDER#" + orderId),
                        "SK", AttributeValue.fromS("PROJECTION#MSK"),
                        "eventType", AttributeValue.fromS(event.path("eventType").asText()),
                        "processedAt", AttributeValue.fromS(event.path("occurredAt").asText()),
                        "payload", AttributeValue.fromS(record.value()))).build());
                consumer.commitSync();
            } catch (Exception exception) {
                System.err.println("MSK event processing failed; offset will be retried: " + exception.getMessage());
                throw new IllegalStateException(exception);
            }
        }
    }
}
