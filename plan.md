# Project Implementation Plan

## 1. Objectives
- [x] Reproduce the monitoring setup described in article-gatling-tests.md to observe Gatling-driven load tests against a Spring Boot REST API.
- [x] Provide a reproducible developer environment using Docker Compose for the monitoring stack and standalone Gatling executions from the host.

## 2. Scope Overview
1. [x] **REST API service** – Spring Boot MVC app exposing `/api/fast-response` and `/api/slow-response` with deterministic and delayed replies.
2. [x] **Prometheus** – scrapes itself, Grafana, and the REST API (via Actuator metrics).
3. [x] **InfluxDB** – stores Gatling metrics exposed through Graphite protocol.
4. [x] **Grafana** – pre-provisioned data sources (Prometheus, InfluxDB) and dashboards (application + Gatling metrics).
5. [x] **Gatling simulations** – Java simulations targeting both REST endpoints, configured to publish metrics to InfluxDB.
6. [x] **Docker Compose** – orchestrates `service`, `prometheus`, `grafana`, and `influxdb`. Gatling runs outside the compose stack.

## 3. Assumptions
- [x] Java 17, Maven, Docker, and Docker Compose are available on developer machines.
- [x] Gatling simulations live in a Maven module capable of producing `gatling:test` goals.
- [x] Spring Boot app already exposes Actuator endpoints needed by Prometheus (`/private/metrics`).

## 4. Implementation Phases & Tasks
### Phase A – Project Skeleton
1. [x] Initialize Git repo structure (if not already) with modules for `service`, `gatling`, and infrastructure configs.
2. [x] Configure Maven parent/build logic ensuring both the Spring Boot app and Gatling simulations build successfully.

### Phase B – REST API Service
1. [x] Implement `PerformanceTestsController` with `/api/fast-response` and `/api/slow-response` methods mirroring the sample logic (random delays 1-2s for slow endpoint).
2. [x] Enable Spring Actuator, expose custom `/private/metrics`, and secure it if needed.
3. [x] Package app as fat JAR (`gatling-java.jar`) and confirm runs locally (`mvn spring-boot:run`).
4. [x] Create Dockerfile using `openjdk:17.0.1-jdk-slim`, copying the built JAR and exposing port `8080`.

### Phase C – Monitoring Stack
**Prometheus**
1. [x] Prepare `config/prometheus-docker.yml` using sample scrape jobs (self, Grafana `/metrics`, Spring service `/private/metrics`).
2. [x] Build Dockerfile from `prom/prometheus:v2.48.1`, copy config, expose `9090`.

**InfluxDB**
1. [x] Write `influxdb.conf` with `[[graphite]]` block enabled on TCP `2003`.
2. [x] Create `entrypoint.sh` that boots InfluxDB, waits for readiness, creates admin user/db per env vars, and marks initialization.
3. [x] Dockerfile based on `influxdb:1.3.1-alpine`, copy config + entrypoint, expose `8086`/`2003`.

**Grafana**
1. [x] Define provisioning files:
   - `datasources.yml` with Prometheus + InfluxDB entries (matching service names/ports).
   - `dashboards.yml` pointing to `/etc/grafana/provisioning/dashboards`.
2. [x] Add dashboard JSONs (`application-metrics.json`, `gatling-metrics.json`).
3. [x] Dockerfile from `grafana/grafana:10.2.2`, copy provisioning + dashboards, expose `3000`.

### Phase D – Gatling Module
1. [x] Implement `SlowEndpointSimulation` and `FastEndpointSimulation` per documentation, using helper builders for request chains and populations.
2. [x] Update `gatling.conf` to enable `graphite` writer pointing to `localhost:2003`, root path `gatling`.
3. [x] Verify simulations locally using Maven commands (e.g., `mvn gatling:test -Dgatling.simulationClass=...`).

### Phase E – Docker Compose Integration
1. [x] Create `docker-compose.yml` with services `influxdb`, `prometheus`, `grafana`, `service` referencing build contexts above.
2. [x] Map required ports:
   - InfluxDB `8086`, `2003`
   - Prometheus `9090`
   - Grafana `3000`
   - Service `8080`
3. [x] Declare environment variables for InfluxDB admin user/password/db name.
4. [x] Validate startup via `docker-compose up --build`, ensure all services healthy.

**Compose usage reminder**
- Execute `docker-compose up --build` from the repo root to build all images and start Prometheus, InfluxDB, Grafana, and the Spring service.
- Access points:
   - Service: http://localhost:8080/api/fast-response (or `/slow-response`)
   - Prometheus: http://localhost:9090/
   - Grafana: http://localhost:3000/ (admin/admin by default)
   - InfluxDB HTTP API: http://localhost:8086/
- Use `docker-compose down -v` after test runs to reclaim containers and volumes.

### Phase F – End-to-End Validation
1. [x] With stack running, trigger Gatling simulations; confirm metrics land in InfluxDB and Grafana dashboards populate.
2. [x] Check Prometheus targets (`/targets`) to ensure service metrics scraping works.
3. [x] Document troubleshooting steps (e.g., firewall, port conflicts, credential mismatches).

## 5. Deliverables
- [x] Source code for Spring Boot service, Gatling simulations, and configuration assets.
- [x] Dockerfiles for each service plus `docker-compose.yml`.
- [x] Provisioned Grafana dashboards.
- [x] README updates describing setup, commands, and verification steps.

## 6. Risks & Mitigations
- [ ] **Metric mismatch**: Ensure Gatling Graphite config aligns with InfluxDB port & database.
- [ ] **Service startup order**: Use `depends_on` and readiness checks (especially for Prometheus scraping the service).
- [ ] **Resource usage**: Document minimum host requirements to avoid Docker resource starvation.

