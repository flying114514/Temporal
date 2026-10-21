package com.liuhui.incidentops.domain;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record Incident(UUID id, String correlationKey, String service, String title,
                       Severity severity, IncidentStatus status, Instant firstSeen,
                       Instant lastSeen, List<AlertEvent> alerts, List<Evidence> evidence,
                       Diagnosis diagnosis) {
    public Incident {
        alerts = alerts == null ? List.of() : List.copyOf(alerts);
        evidence = evidence == null ? List.of() : List.copyOf(evidence);
    }
}
