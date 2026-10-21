package com.liuhui.incidentops.service;

import com.liuhui.incidentops.domain.*;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class IncidentService {
    private static final Duration CORRELATION_WINDOW = Duration.ofMinutes(10);
    private final ConcurrentMap<UUID, Incident> incidents = new ConcurrentHashMap<>();

    public Incident ingest(AlertEvent event) {
        String key = correlationKey(event);
        Incident existing = incidents.values().stream()
                .filter(i -> i.correlationKey().equals(key))
                .filter(i -> Duration.between(i.lastSeen(), event.occurredAt()).abs().compareTo(CORRELATION_WINDOW) <= 0)
                .min(Comparator.comparing(Incident::lastSeen).reversed())
                .orElse(null);
        if (existing == null) {
            Incident created = new Incident(UUID.randomUUID(), key, event.service(), event.title(),
                    event.severity(), IncidentStatus.OPEN, event.occurredAt(), event.occurredAt(),
                    List.of(event), List.of(), null);
            incidents.put(created.id(), created);
            return created;
        }
        Incident merged = new Incident(existing.id(), key, existing.service(), existing.title(),
                moreSevere(existing.severity(), event.severity()), existing.status(), existing.firstSeen(),
                event.occurredAt().isAfter(existing.lastSeen()) ? event.occurredAt() : existing.lastSeen(),
                append(existing.alerts(), event), existing.evidence(), existing.diagnosis());
        incidents.put(merged.id(), merged);
        return merged;
    }

    public List<Incident> list() {
        return incidents.values().stream().sorted(Comparator.comparing(Incident::lastSeen).reversed()).toList();
    }

    public Incident get(UUID id) {
        return incidents.get(id);
    }

    public Incident requireForAgent(UUID id) {
        return require(id);
    }

    public Incident attachDiagnosis(UUID id, Diagnosis diagnosis) {
        Incident incident = require(id);
        Incident updated = new Incident(incident.id(), incident.correlationKey(), incident.service(), incident.title(),
                incident.severity(), IncidentStatus.INVESTIGATING, incident.firstSeen(), incident.lastSeen(),
                incident.alerts(), diagnosis.evidence(), diagnosis);
        incidents.put(id, updated);
        return updated;
    }

    public Incident changeStatus(UUID id, IncidentStatus status) {
        Incident updated = copyWithStatus(require(id), status);
        incidents.put(id, updated);
        return updated;
    }

    public Incident diagnose(UUID id) {
        Incident incident = require(id);
        List<Evidence> evidence = new ArrayList<>();
        evidence.add(new Evidence("alert-correlation", "收到 " + incident.alerts().size() + " 条同服务告警，指纹已完成聚合", "correlator", Instant.now(), 0.9));
        evidence.add(new Evidence("severity-analysis", "当前最高严重级别为 " + incident.severity(), "rule-engine", Instant.now(), 0.82));
        Diagnosis diagnosis = new Diagnosis(
                "服务 " + incident.service() + " 出现与“" + incident.title() + "”相关的异常，建议先核查最近发布与依赖服务状态",
                0.78, evidence,
                List.of("查询最近 30 分钟错误率与延迟", "比对最近发布记录", "确认依赖服务是否同步异常"), Instant.now());
        Incident updated = new Incident(incident.id(), incident.correlationKey(), incident.service(), incident.title(),
                incident.severity(), IncidentStatus.INVESTIGATING, incident.firstSeen(), incident.lastSeen(),
                incident.alerts(), evidence, diagnosis);
        incidents.put(id, updated);
        return updated;
    }

    public ActionResult requestAction(UUID id, ActionRequest request) {
        Incident incident = require(id);
        if (incident.severity() == Severity.P1 || incident.severity() == Severity.P2) {
            Incident pending = copyWithStatus(incident, IncidentStatus.AWAITING_APPROVAL);
            incidents.put(id, pending);
            return new ActionResult(request.action(), "PENDING_APPROVAL", "高风险处置需要人工审批", Instant.now());
        }
        incidents.put(id, copyWithStatus(incident, IncidentStatus.MITIGATING));
        return new ActionResult(request.action(), "SIMULATED", "已通过安全策略；MVP 仅模拟执行，未调用外部系统", Instant.now());
    }

    public Incident resolve(UUID id) {
        Incident incident = require(id);
        Incident resolved = copyWithStatus(incident, IncidentStatus.RESOLVED);
        incidents.put(id, resolved);
        return resolved;
    }

    private Incident require(UUID id) {
        Incident incident = incidents.get(id);
        if (incident == null) throw new IllegalArgumentException("incident not found: " + id);
        return incident;
    }

    private static String correlationKey(AlertEvent event) {
        return event.service() + ":" + event.fingerprint();
    }

    private static Severity moreSevere(Severity a, Severity b) {
        return a.ordinal() <= b.ordinal() ? a : b;
    }

    private static List<AlertEvent> append(List<AlertEvent> source, AlertEvent event) {
        List<AlertEvent> result = new ArrayList<>(source);
        result.add(event);
        return result;
    }

    private static Incident copyWithStatus(Incident i, IncidentStatus status) {
        return new Incident(i.id(), i.correlationKey(), i.service(), i.title(), i.severity(), status,
                i.firstSeen(), i.lastSeen(), i.alerts(), i.evidence(), i.diagnosis());
    }
}
