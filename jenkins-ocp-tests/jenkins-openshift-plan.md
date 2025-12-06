# Jenkins + OpenShift Gatling Plan

## Goal
Run the existing Gatling simulations from Jenkins inside OpenShift 4 so that:
- Load targets the already-deployed Spring Boot service.
- The service continues exporting metrics to Prometheus, which Grafana picks up centrally.
- Jenkins publishes a small set of Gatling run KPIs to a Prometheus endpoint (e.g., Pushgateway) for CI visibility.

## Assumptions
- The Spring Boot application and Prometheus stack are already deployed in OpenShift and reachable via an internal route/service.
- Grafana reads from the central Prometheus instance (no changes to this repo required).
- Jenkins has network access to the OpenShift API and Prometheus Pushgateway.

## High-Level Flow
1. Jenkins checks out this repository for reference (no modifications needed).
2. Jenkins authenticates to OpenShift using a service account token and selects the target project/namespace.
3. Jenkins uses `mvn -pl jenkins-ocp-tests jib:build` to containerize the shared Gatling launcher and push it to Artifactory.
4. Jenkins launches a short-lived OpenShift pod from that image. The container executes `org.andrey5608.jenkins.GatlingEntryPoint`, which resolves the simulation from `SIMULATION_CLASS`, runs Gatling, and writes reports under `/tmp/gatling-results` (configurable via `GATLING_RESULTS_DIR`).
5. After completion, Jenkins copies the results locally, parses the JSON stats, and pushes summary metrics (success flag, p95 latency, etc.) to Prometheus via Pushgateway.
6. Existing Grafana dashboards continue displaying application metrics; central Grafana can also chart the new CI metrics without touching this repo.

## Jenkins Requirements
- Jenkins controller either running in OpenShift (preferred) or external with `oc` CLI installed.
- Credentials: OpenShift token (service account) and Artifactory username/password used by the Jib build.
- Nodes/agents with `oc`, `jq`, `curl`, and `mvn` so the pipeline can build, publish, and inspect Gatling reports.

## OpenShift Resources
- **Service Account** with permissions to `oc project`, `oc run`, and delete pods in the target namespace.
- **Runner Image**: Built by Jib from `jenkins-ocp-tests/pom.xml`. It packages the compiled Gatling simulations (via the gatling test-jar) and exposes `org.andrey5608.jenkins.GatlingEntryPoint` as the container `mainClass`.
- Publish the image to Artifactory (or any OCI registry) by running `mvn -pl jenkins-ocp-tests jib:build -Djib.to.image=<registry/path>`. Override `SIMULATION_CLASS`/`GATLING_RESULTS_DIR` per pod via env vars rather than rebuilding the image.

## Jenkinsfile Skeleton
See `jenkins-ocp-tests/Jenkinsfile` for a ready-to-run declarative pipeline. Highlights:
- Builds and pushes the Gatling runner image with Jib using Artifactory credentials (`ARTIFACTORY_CREDENTIALS_ID`).
- Logs into OpenShift (`OC_TOKEN_CREDENTIALS_ID`), runs the Gatling pod with env overrides, and streams logs until completion.
- Copies `$RESULTS_DIR` (default `/tmp/gatling-results`) from the completed pod so Jenkins can archive HTML reports and feed Prometheus.
- Invokes `jenkins-ocp-tests/scripts/publish_metrics.sh <statsDir> <pushgateway> <job> <simulation>` to push KPIs such as `gatling_run_success`, `gatling_response_p95_ms`, and `gatling_requests_total` to your Pushgateway.
- Archives the Gatling reports for root-cause analysis even if the pipeline fails.

## Observability Strategy
- Prometheus already scrapes application metrics; the Gatling load simply exercises the same endpoints, so dashboards update automatically.
- The Pushgateway metrics provide CI status (`gatling_run_success`, `gatling_response_p95_ms`, custom tags for build number or Git SHA). Add new Grafana panels pointing at these series in the central dashboard repo.
- Retain Gatling HTML reports as Jenkins artifacts for deep dives.

## Operational Tips
- Keep the 60 s ramp-up and warm-up in simulations; specify different `SIM` values via Jenkins parameters for fast vs. slow tests.
- Set CPU/memory requests on the Gatling pod to avoid throttling and noisy data.
- Schedule periodic runs (e.g., hourly) or trigger on demand before deployments to validate latency budgets.
- Rotate the OpenShift token via Jenkins credentials to avoid outages.
- Ensure `jq`, `curl`, and `oc` are installed on the Jenkins agent so the helper scripts can parse Gatling stats and talk to OpenShift/Prometheus.
