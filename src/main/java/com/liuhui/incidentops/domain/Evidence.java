package com.liuhui.incidentops.domain;

import java.time.Instant;

public record Evidence(String type, String summary, String source, Instant observedAt, double confidence) {
    public Evidence {
        observedAt = observedAt == null ? Instant.now() : observedAt;
        confidence = Math.max(0, Math.min(1, confidence));
    }
}
