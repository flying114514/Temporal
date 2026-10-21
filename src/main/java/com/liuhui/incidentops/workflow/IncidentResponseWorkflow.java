package com.liuhui.incidentops.workflow;

import io.temporal.workflow.SignalMethod;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

@WorkflowInterface
public interface IncidentResponseWorkflow {
    @WorkflowMethod
    void start(String incidentId);

    @SignalMethod
    void approve();

    @SignalMethod
    void reject();
}
