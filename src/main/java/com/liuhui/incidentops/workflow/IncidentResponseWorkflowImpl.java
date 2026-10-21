package com.liuhui.incidentops.workflow;

import io.temporal.activity.ActivityOptions;
import io.temporal.workflow.Workflow;

import java.time.Duration;

public class IncidentResponseWorkflowImpl implements IncidentResponseWorkflow {
    private final IncidentActivities activities = Workflow.newActivityStub(
            IncidentActivities.class,
            ActivityOptions.newBuilder().setStartToCloseTimeout(Duration.ofMinutes(2)).build());
    private boolean approved;
    private boolean rejected;

    @Override
    public void start(String incidentId) {
        activities.diagnose(incidentId);
        Workflow.await(() -> approved || rejected);
        if (approved) {
            activities.executeApprovedAction(incidentId);
        }
    }

    @Override
    public void approve() {
        approved = true;
    }

    @Override
    public void reject() {
        rejected = true;
    }
}
