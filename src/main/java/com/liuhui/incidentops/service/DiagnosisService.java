package com.liuhui.incidentops.service;

import com.liuhui.incidentops.domain.Diagnosis;
import com.liuhui.incidentops.domain.Incident;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class DiagnosisService {
    private final IncidentService incidentService;
    private final DiagnosisAgent diagnosisAgent;

    public DiagnosisService(IncidentService incidentService, DiagnosisAgent diagnosisAgent) {
        this.incidentService = incidentService;
        this.diagnosisAgent = diagnosisAgent;
    }

    public Incident diagnose(UUID id) {
        Incident incident = incidentService.requireForAgent(id);
        Diagnosis diagnosis = diagnosisAgent.diagnose(incident);
        return incidentService.attachDiagnosis(id, diagnosis);
    }
}
