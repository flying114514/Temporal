# IncidentOps

> Evidence-driven incident response platform for production systems.

IncidentOps is an intelligent incident response platform built around **alert correlation, evidence-based diagnosis, durable workflow orchestration, and governed remediation**. It connects operational signals with service context, guides an Agent through a traceable investigation, and enforces approval and risk policies before any remediation action is executed.

The platform is designed for high-pressure production operations: every conclusion is backed by evidence, every action is policy-checked, and every workflow can survive retries, restarts, and human intervention.

## Architecture

```text
Prometheus / Logs / Traces / Deployments
                 │
                 ▼
        Alert Ingestion & Correlation
                 │
                 ▼
       Incident Context and Topology
                 │
                 ▼
        Evidence-driven Agent Diagnosis
                 │
                 ▼
       Temporal Durable Response Workflow
                 │
        ┌────────┴────────┐
        ▼                 ▼
  Human Approval      Governed Tools
        │                 │
        └────────┬────────┘
                 ▼
       SLI Recovery Validation
```

## Core Capabilities

### Alert correlation and incident aggregation

- Normalizes alerts from multiple observability sources.
- Correlates events by service, fingerprint, labels, and time window.
- Deduplicates repeated alerts and consolidates related symptoms into one incident.
- Preserves the complete alert timeline for investigation and audit.
- Recalculates incident severity when a more critical signal arrives.

### Evidence-driven Agent diagnosis

- Builds an investigation context from alerts, service metadata, topology, metrics, logs, traces, and deployment history.
- Follows a hypothesis-driven loop: propose a hypothesis, query evidence, validate the hypothesis, and revise when evidence contradicts it.
- Separates evidence collection from reasoning so that conclusions remain traceable.
- Provides a deterministic rule-based diagnosis fallback when model inference is unavailable.
- Returns confidence, supporting evidence, and recommended next steps instead of an unsupported conclusion.

### Durable Temporal workflows

- Models incident response as a durable workflow rather than a single synchronous request.
- Supports retries, timeouts, workflow signals, and recovery after Worker restarts.
- Pauses safely at human approval gates.
- Keeps diagnosis, approval, remediation, and recovery validation as explicit stages.
- Uses idempotent activity boundaries to prevent duplicate side effects.

### Risk-aware remediation governance

- Classifies remediation actions by incident severity and operational risk.
- Applies fail-closed policy evaluation before tools can produce side effects.
- Requires human approval for high-risk actions such as rollback, traffic changes, or service restart.
- Restricts tools by action type, target service, caller identity, and allowed parameters.
- Records action requests, approvals, executions, and outcomes in the audit trail.

### Recovery validation

- Verifies recovery through service-level indicators rather than relying on command success.
- Checks error rate, latency, saturation, alert state, and dependency health.
- Distinguishes “action completed” from “incident recovered”.
- Keeps incidents open when the remediation did not restore the defined SLI conditions.

## Technology Stack

| Layer | Technology |
|---|---|
| Application | Java 21, Spring Boot |
| Agent orchestration | Spring AI, structured tool calling |
| Durable workflows | Temporal Java SDK |
| Persistence | PostgreSQL |
| Short-lived state | Redis |
| Observability | Prometheus, OpenTelemetry |
| API | REST, JSON, Spring Validation |
| Operations | Spring Actuator, Docker Compose |

## Incident Lifecycle

```text
OPEN
  → INVESTIGATING
  → AWAITING_APPROVAL
  → MITIGATING
  → RECOVERY_VALIDATION
  → RESOLVED
```

Failure paths are explicit and auditable. A rejected action returns the incident to investigation, while a failed recovery validation keeps the incident active and triggers another evidence collection cycle.

## API Overview

### Ingest an alert

```http
POST /api/incidents/events
Content-Type: application/json
```

```json
{
  "source": "prometheus",
  "fingerprint": "checkout-latency",
  "service": "checkout",
  "title": "Checkout latency high",
  "severity": "P1",
  "labels": {
    "cluster": "production-a"
  },
  "annotations": {
    "summary": "p99 latency exceeded the SLO threshold"
  }
}
```

### Query incidents

```http
GET /api/incidents
GET /api/incidents/{incidentId}
```

### Start diagnosis

```http
POST /api/incidents/{incidentId}/agent-diagnosis
```

### Request remediation

```http
POST /api/incidents/{incidentId}/actions
Content-Type: application/json
```

```json
{
  "action": "rollback",
  "requestedBy": "on-call-engineer",
  "parameters": {
    "service": "checkout",
    "targetVersion": "previous-stable"
  }
}
```

### Approve or reject a high-risk action

```http
POST /api/incidents/{incidentId}/approval/approve
POST /api/incidents/{incidentId}/approval/reject
```

### Execute and resolve

```http
POST /api/incidents/{incidentId}/execute
POST /api/incidents/{incidentId}/resolve
```

## Local Development

### Requirements

- Java 21
- Maven 3.9+
- Docker Engine and Docker Compose

### Start infrastructure

```bash
docker compose up -d
```

### Start the service

```bash
mvn spring-boot:run
```

### Run tests

```bash
mvn test
```

### Health and metrics

```text
GET /actuator/health
GET /actuator/metrics
```

## Reliability and Security Principles

- **Evidence before action**: remediation requires an explicit investigation context.
- **Fail closed**: uncertain or incomplete policy decisions never authorize side effects.
- **Human-in-the-loop**: high-risk operations require approval from an authorized operator.
- **Least privilege**: each tool receives only the permissions and parameters required for its action.
- **Idempotency**: retries must not create duplicate remediation effects.
- **Auditable decisions**: incidents, evidence, hypotheses, approvals, and actions are traceable.
- **Recovery is measurable**: an incident is resolved only when SLI conditions return to normal.

## Project Structure

```text
src/main/java/com/liuhui/incidentops
├── api          REST controllers and exception handling
├── domain       Incident, alert, evidence, diagnosis, and action models
├── service      Correlation, diagnosis, governance, and workflow services
└── workflow     Temporal workflow and activity contracts
```

## License

This project is provided for engineering research and demonstration purposes.
