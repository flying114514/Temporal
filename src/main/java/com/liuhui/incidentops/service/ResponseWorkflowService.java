package com.liuhui.incidentops.service;

import com.liuhui.incidentops.domain.*;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class ResponseWorkflowService {
    private final IncidentService incidentService;

    public ResponseWorkflowService(IncidentService incidentService) {
        this.incidentService = incidentService;
    }

    public Incident approve(UUID id) {
        Incident incident = incidentService.requireForAgent(id);
        if (incident.status() != IncidentStatus.AWAITING_APPROVAL) {
            throw new IllegalStateException("incident is not awaiting approval");
        }
        return incidentService.changeStatus(id, IncidentStatus.MITIGATING);
    }

    public Incident reject(UUID id) {
        Incident incident = incidentService.requireForAgent(id);
        if (incident.status() != IncidentStatus.AWAITING_APPROVAL) {
            throw new IllegalStateException("incident is not awaiting approval");
        }
        return incidentService.changeStatus(id, IncidentStatus.INVESTIGATING);
    }

    public ActionResult execute(UUID id, ActionRequest request) {
        Incident incident = incidentService.requireForAgent(id);
        if (incident.status() != IncidentStatus.MITIGATING) {
            throw new IllegalStateException("incident must be in MITIGATING state");
        }
        // External side effects are intentionally not wired in the MVP.
        return new ActionResult(request.action(), "SIMULATED", "安全策略通过；外部执行器尚未接入", Instant.now());
    }
}
