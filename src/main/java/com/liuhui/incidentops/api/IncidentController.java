package com.liuhui.incidentops.api;

import com.liuhui.incidentops.domain.*;
import com.liuhui.incidentops.service.IncidentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/incidents")
public class IncidentController {
    private final IncidentService incidentService;

    public IncidentController(IncidentService incidentService) {
        this.incidentService = incidentService;
    }

    @PostMapping("/events")
    public ResponseEntity<Incident> ingest(@Valid @RequestBody AlertEvent event) {
        return ResponseEntity.accepted().body(incidentService.ingest(event));
    }

    @GetMapping
    public List<Incident> list() {
        return incidentService.list();
    }

    @GetMapping("/{id}")
    public Incident get(@PathVariable UUID id) {
        return incidentService.get(id);
    }

    @PostMapping("/{id}/diagnose")
    public Incident diagnose(@PathVariable UUID id) {
        return incidentService.diagnose(id);
    }

    @PostMapping("/{id}/actions")
    public ActionResult action(@PathVariable UUID id, @Valid @RequestBody ActionRequest request) {
        return incidentService.requestAction(id, request);
    }

    @PostMapping("/{id}/resolve")
    public Incident resolve(@PathVariable UUID id) {
        return incidentService.resolve(id);
    }
}
