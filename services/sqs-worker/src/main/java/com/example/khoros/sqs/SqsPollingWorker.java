package com.example.khoros.sqs;

import com.example.khoros.events.EventEnvelope;
import com.example.khoros.events.KafkaEventPublisher;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;

@Component
public class SqsPollingWorker {
    private final SqsClient sqs;
    private final String queueUrl;
    private final KafkaEventPublisher eventPublisher;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public SqsPollingWorker(
            @Value("${AWS_REGION:us-east-1}") String region,
            @Value("${WORK_QUEUE_URL}") String queueUrl,
            @Value("${MSK_BOOTSTRAP_SERVERS}") String brokers,
            @Value("${MSK_ORDER_TOPIC:bridge-events}") String topic) {
        this.queueUrl = queueUrl;
        this.sqs = SqsClient.builder().region(Region.of(region)).build();
        this.eventPublisher = new KafkaEventPublisher(brokers, topic, "sqs-worker", objectMapper);
    }

    @Scheduled(fixedDelayString = "${POLL_DELAY_MS:1000}")
    public void poll() {
        List<Message> messages = sqs.receiveMessage(ReceiveMessageRequest.builder()
                .queueUrl(queueUrl).waitTimeSeconds(10).maxNumberOfMessages(5).visibilityTimeout(45).build()).messages();
        for (Message message : messages) {
            try {
                EventEnvelope original = objectMapper.readValue(message.body(), EventEnvelope.class);
                EventEnvelope completed = new EventEnvelope(UUID.randomUUID().toString(), "work.completed", Instant.now().toString(),
                        "sqs-worker", Map.of("orderId", original.data().getOrDefault("orderId", "unknown"), "result", "OK"));
                eventPublisher.publish(original.eventId(), completed);
                sqs.deleteMessage(DeleteMessageRequest.builder().queueUrl(queueUrl).receiptHandle(message.receiptHandle()).build());
            } catch (Exception exception) {
                System.err.println("SQS message processing failed; leaving it for retry: " + exception.getMessage());
            }
        }
    }
}
