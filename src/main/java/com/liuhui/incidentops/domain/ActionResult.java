package com.liuhui.incidentops.domain;

import java.time.Instant;

public record ActionResult(String action, String status, String message, Instant executedAt) {
    public ActionResult {
        executedAt = executedAt == null ? Instant.now() : executedAt;
    }
}
