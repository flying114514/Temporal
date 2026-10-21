package com.liuhui.incidentops.api;

import com.liuhui.incidentops.domain.*;
import com.liuhui.incidentops.service.DiagnosisService;
import com.liuhui.incidentops.service.ResponseWorkflowService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/incidents/{id}")
public class IncidentWorkflowController {
    private final DiagnosisService diagnosisService;
    private final ResponseWorkflowService workflowService;

    public IncidentWorkflowController(DiagnosisService diagnosisService, ResponseWorkflowService workflowService) {
        this.diagnosisService = diagnosisService;
        this.workflowService = workflowService;
    }

    @PostMapping("/agent-diagnosis")
    public Incident diagnose(@PathVariable UUID id) {
        return diagnosisService.diagnose(id);
    }

    @PostMapping("/approval/approve")
    public Incident approve(@PathVariable UUID id) {
        return workflowService.approve(id);
    }

    @PostMapping("/approval/reject")
    public Incident reject(@PathVariable UUID id) {
        return workflowService.reject(id);
    }

    @PostMapping("/execute")
    public ActionResult execute(@PathVariable UUID id, @Valid @RequestBody ActionRequest request) {
        return workflowService.execute(id, request);
    }
}
