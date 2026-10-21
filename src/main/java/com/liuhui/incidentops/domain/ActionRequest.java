package com.liuhui.incidentops.domain;

import jakarta.validation.constraints.NotBlank;

import java.util.Map;

public record ActionRequest(@NotBlank String action, @NotBlank String requestedBy,
                            Map<String, String> parameters) {
    public ActionRequest {
        parameters = parameters == null ? Map.of() : Map.copyOf(parameters);
    }
}
