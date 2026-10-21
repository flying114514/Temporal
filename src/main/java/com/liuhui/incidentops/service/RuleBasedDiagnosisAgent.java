package com.liuhui.incidentops.service;

import com.liuhui.incidentops.domain.*;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
public class RuleBasedDiagnosisAgent implements DiagnosisAgent {
    @Override
    public Diagnosis diagnose(Incident incident) {
        String hypothesis = switch (incident.title().toLowerCase()) {
            case String title when title.contains("latency") || title.contains("timeout") ->
                    "请求延迟或下游依赖变慢，优先检查最近发布、依赖延迟和线程池饱和度";
            case String title when title.contains("error") || title.contains("5xx") ->
                    "服务错误率升高，优先检查最近发布、异常堆栈和依赖服务健康度";
            default -> "服务出现异常，需结合指标、日志和 Trace 进一步确认根因";
        };
        return new Diagnosis(hypothesis, 0.72,
                List.of(new Evidence("correlation", "告警已按服务和指纹聚合", "rule-agent", Instant.now(), 0.9)),
                List.of("查询服务指标", "检查最近发布", "查看关联 Trace"), Instant.now());
    }
}
