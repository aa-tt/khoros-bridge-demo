package com.example.khoros.events;

import java.util.Map;

public record EventEnvelope(
        String eventId,
        String eventType,
        String occurredAt,
        String source,
        Map<String, Object> data) {
}
