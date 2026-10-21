# IncidentOps

面向生产事故的证据驱动响应平台 MVP：告警聚合、规则诊断、风险分级、审批门禁与安全处置流程。

## 当前实现

- `POST /api/incidents/events`：接收告警并按 `service + fingerprint` 在 10 分钟窗口内聚合
- `GET /api/incidents`、`GET /api/incidents/{id}`：查询事故
- `POST /api/incidents/{id}/diagnose`：生成规则驱动诊断和证据
- `POST /api/incidents/{id}/agent-diagnosis`：调用可替换的 `DiagnosisAgent` 抽象
- `POST /api/incidents/{id}/actions`：P1/P2 自动进入人工审批，P3/P4 仅模拟执行
- `POST /api/incidents/{id}/approval/approve`、`reject`：审批门禁
- `POST /api/incidents/{id}/execute`：审批后的模拟执行
- `POST /api/incidents/{id}/resolve`：标记恢复
- Actuator：`/actuator/health`、`/actuator/metrics`

## 启动

环境要求：Java 21、Maven 3.9+。

```bash
mvn spring-boot:run
```

发送一条告警：

```bash
curl -X POST http://localhost:8080/api/incidents/events \
  -H 'Content-Type: application/json' \
  -d '{
    "source":"prometheus",
    "fingerprint":"checkout-latency",
    "service":"checkout",
    "title":"Checkout latency high",
    "severity":"P1",
    "labels":{"cluster":"local"},
    "annotations":{"summary":"p99 above threshold"}
  }'
```

## 设计说明

当前版本故意不接入真实外部执行器，所有处置动作均为模拟结果，并且 P1/P2 必须经过审批。这保证本地开发阶段 fail-closed，不会误修改真实基础设施。

`DiagnosisAgent` 是后续接入 LLM、Prometheus、日志和 Trace 查询工具的扩展点。LLM provider 暂未绑定，避免在 API key、模型和部署方式未确定前把项目锁死到单一供应商。

## 后续接入

1. PostgreSQL：持久化 Incident、Alert、Evidence、审计日志。
2. Redis：幂等键、时间窗口聚合和短期状态。
3. Temporal：将诊断、审批、处置和恢复验收改造成可恢复工作流。
4. Prometheus / OpenTelemetry：接入只读查询工具。
5. 真实 LLM provider：实现证据约束的 Agent 推理，但保留规则诊断 fallback。
6. RBAC、审计日志和真实工具执行器。
