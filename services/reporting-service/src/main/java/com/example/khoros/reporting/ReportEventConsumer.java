package com.example.khoros.reporting;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Component
public class ReportEventConsumer {
    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("yyyy/MM/dd").withZone(ZoneOffset.UTC);
    private final KafkaConsumer<String, String> consumer;
    private final DynamoDbClient dynamoDb;
    private final S3Client s3;
    private final String bucket;
    private final String table;
    private final ObjectMapper mapper = new ObjectMapper();

    public ReportEventConsumer(
            @Value("${AWS_REGION:us-east-1}") String region,
            @Value("${MSK_BOOTSTRAP_SERVERS}") String brokers,
            @Value("${MSK_ORDER_TOPIC:bridge-events}") String topic,
            @Value("${MSK_GROUP_ID:reporting-service}") String groupId,
            @Value("${REPORTS_BUCKET}") String bucket,
            @Value("${REPORTS_TABLE}") String table) {
        this.bucket = bucket;
        this.table = table;
        Region awsRegion = Region.of(region);
        this.dynamoDb = DynamoDbClient.builder().region(awsRegion).build();
        this.s3 = S3Client.builder().region(awsRegion).build();
        var properties = com.example.khoros.events.KafkaSettings.clientProperties(brokers, "reporting-service", groupId);
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
                String eventId = event.path("eventId").asText(record.key());
                String key = "reports/" + DAY.format(Instant.now()) + "/" + eventId + ".json";
                s3.putObject(PutObjectRequest.builder().bucket(bucket).key(key).contentType("application/json").build(),
                        RequestBody.fromString(record.value()));
                dynamoDb.putItem(PutItemRequest.builder().tableName(table).item(Map.of(
                        "PK", AttributeValue.fromS("REPORT#" + eventId),
                        "SK", AttributeValue.fromS("EVENT#" + event.path("occurredAt").asText()),
                        "eventType", AttributeValue.fromS(event.path("eventType").asText()),
                        "s3Key", AttributeValue.fromS(key))).build());
                consumer.commitSync();
            } catch (Exception exception) {
                System.err.println("Report generation failed; offset will be retried: " + exception.getMessage());
                throw new IllegalStateException(exception);
            }
        }
    }
}
