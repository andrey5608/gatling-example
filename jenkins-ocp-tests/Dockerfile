# syntax=docker/dockerfile:1.6

FROM maven:3.9.6-eclipse-temurin-17 AS deps
WORKDIR /workspace
COPY pom.xml pom.xml
COPY gatling/pom.xml gatling/pom.xml
RUN mvn -pl gatling -am dependency:go-offline

FROM maven:3.9.6-eclipse-temurin-17 AS runner
WORKDIR /workspace
COPY --from=deps /root/.m2 /root/.m2
COPY . .
ENV SIMULATION_CLASS=org.andrey5608.performance.FastEndpointSimulation \
    GATLING_EXTRA_OPTS=""
RUN cat <<'EOF' > /usr/local/bin/run-gatling.sh \
    && chmod +x /usr/local/bin/run-gatling.sh
#!/usr/bin/env bash
set -euo pipefail
cd /workspace
mvn -pl gatling gatling:test \
  -Dgatling.simulationClass="${SIMULATION_CLASS}" \
  ${GATLING_EXTRA_OPTS}
EOF
ENTRYPOINT ["/usr/local/bin/run-gatling.sh"]
