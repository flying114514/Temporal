package com.liuhui.incidentops.service;

import com.liuhui.incidentops.domain.*;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class IncidentServiceTest {
    private final IncidentService service = new IncidentService();

    @Test
    void correlatesAlertsByServiceAndFingerprint() {
        AlertEvent first = event("api.latency", Severity.P2);
        AlertEvent second = event("api.latency", Severity.P1);

        Incident incident = service.ingest(first);
        Incident merged = service.ingest(second);

        assertThat(merged.id()).isEqualTo(incident.id());
        assertThat(merged.alerts()).hasSize(2);
        assertThat(merged.severity()).isEqualTo(Severity.P1);
    }

    @Test
    void highRiskActionRequiresApproval() {
        Incident incident = service.ingest(event("db.error", Severity.P1));

        ActionResult result = service.requestAction(incident.id(), new ActionRequest("rollback", "operator", Map.of()));

        assertThat(result.status()).isEqualTo("PENDING_APPROVAL");
        assertThat(service.get(incident.id()).status()).isEqualTo(IncidentStatus.AWAITING_APPROVAL);
    }

    private AlertEvent event(String fingerprint, Severity severity) {
        return new AlertEvent("prometheus", fingerprint, "checkout", "Checkout latency high", severity, null, Map.of(), Map.of());
    }
}
