package com.example.khoros.rest;

import com.example.khoros.events.EventEnvelope;
import com.example.khoros.events.KafkaEventPublisher;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final String tableName;
    private final String queueUrl;
    private final KafkaEventPublisher eventPublisher;
    private final DynamoDbClient dynamoDb;
    private final SqsClient sqs;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public OrderController(
            @Value("${AWS_REGION:us-east-1}") String region,
            @Value("${ORDERS_TABLE}") String tableName,
            @Value("${WORK_QUEUE_URL}") String queueUrl,
            @Value("${MSK_BOOTSTRAP_SERVERS}") String bootstrapServers,
            @Value("${MSK_ORDER_TOPIC:bridge-events}") String topic) {
        this.tableName = tableName;
        this.queueUrl = queueUrl;
        Region awsRegion = Region.of(region);
        this.dynamoDb = DynamoDbClient.builder().region(awsRegion).build();
        this.sqs = SqsClient.builder().region(awsRegion).build();
        this.eventPublisher = new KafkaEventPublisher(bootstrapServers, topic, "rest-service", objectMapper);
    }

    @PostMapping
    public OrderResponse create(@RequestBody Map<String, Object> request) {
        String orderId = UUID.randomUUID().toString();
        Instant now = Instant.now();
        EventEnvelope event = new EventEnvelope(orderId, "order.created", now.toString(), "rest-service", Map.of(
                "orderId", orderId,
                "status", "NEW",
                "payload", request));
        try {
            String payload = objectMapper.writeValueAsString(request);
            dynamoDb.putItem(PutItemRequest.builder().tableName(tableName).item(Map.of(
                    "PK", AttributeValue.fromS("ORDER#" + orderId),
                    "SK", AttributeValue.fromS("ORDER#" + orderId),
                    "status", AttributeValue.fromS("NEW"),
                    "createdAt", AttributeValue.fromS(now.toString()),
                    "payload", AttributeValue.fromS(payload))).build());
            sqs.sendMessage(SendMessageRequest.builder().queueUrl(queueUrl).messageBody(objectMapper.writeValueAsString(event)).build());
            eventPublisher.publish(orderId, event);
            return new OrderResponse(orderId, "NEW", now.toString());
        } catch (Exception exception) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Order could not be persisted or dispatched", exception);
        }
    }

    public record OrderResponse(String orderId, String status, String createdAt) {
    }
}
