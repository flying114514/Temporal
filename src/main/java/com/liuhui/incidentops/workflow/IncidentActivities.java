package com.liuhui.incidentops.workflow;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

@ActivityInterface
public interface IncidentActivities {
    @ActivityMethod
    void diagnose(String incidentId);

    @ActivityMethod
    void executeApprovedAction(String incidentId);
}
