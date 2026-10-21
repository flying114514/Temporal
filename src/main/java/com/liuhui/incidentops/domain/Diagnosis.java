package com.liuhui.incidentops.domain;

import java.time.Instant;
import java.util.List;

public record Diagnosis(String hypothesis, double confidence, List<Evidence> evidence,
                        List<String> nextSteps, Instant generatedAt) {
    public Diagnosis {
        evidence = evidence == null ? List.of() : List.copyOf(evidence);
        nextSteps = nextSteps == null ? List.of() : List.copyOf(nextSteps);
        generatedAt = generatedAt == null ? Instant.now() : generatedAt;
    }
}
