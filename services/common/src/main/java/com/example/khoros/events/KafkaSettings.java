package com.example.khoros.events;

import java.util.Properties;

public final class KafkaSettings {
    private KafkaSettings() {
    }

    public static Properties clientProperties(String bootstrapServers, String clientId, String groupId) {
        Properties properties = new Properties();
        properties.put("bootstrap.servers", bootstrapServers);
        properties.put("client.id", clientId);
        properties.put("security.protocol", "SASL_SSL");
        properties.put("sasl.mechanism", "AWS_MSK_IAM");
        properties.put("sasl.jaas.config", "software.amazon.msk.auth.iam.client.callback.handler.class required;");
        properties.put("sasl.client.callback.handler.class", "software.amazon.msk.auth.iam.client.callback.handler.class");
        properties.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        properties.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        if (groupId != null) {
            properties.put("group.id", groupId);
            properties.put("auto.offset.reset", "earliest");
            properties.put("enable.auto.commit", "false");
        }
        return properties;
    }
}
