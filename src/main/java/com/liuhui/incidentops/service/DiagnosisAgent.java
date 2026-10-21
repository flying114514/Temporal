package com.liuhui.incidentops.service;

import com.liuhui.incidentops.domain.Diagnosis;
import com.liuhui.incidentops.domain.Incident;

public interface DiagnosisAgent {
    Diagnosis diagnose(Incident incident);
}
