package com.liuhui.incidentops.domain;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.Map;

public record AlertEvent(
        @NotBlank String source,
        @NotBlank String fingerprint,
        @NotBlank String service,
        @NotBlank String title,
        @NotNull Severity severity,
        Instant occurredAt,
        Map<String, String> labels,
        Map<String, Object> annotations) {
    public AlertEvent {
        occurredAt = occurredAt == null ? Instant.now() : occurredAt;
        labels = labels == null ? Map.of() : Map.copyOf(labels);
        annotations = annotations == null ? Map.of() : Map.copyOf(annotations);
    }
}
