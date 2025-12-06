#!/usr/bin/env bash
set -euo pipefail

if [[ $# -lt 4 ]]; then
  echo "Usage: $0 <stats-dir> <pushgateway-url> <job-name> <simulation-name>" >&2
  exit 1
fi

STATS_DIR=$1
PUSHGATEWAY_URL=$2
JOB_NAME=$3
SIMULATION_NAME=$4

STATS_FILE="$STATS_DIR/js/global_stats.json"
if [[ ! -f "$STATS_FILE" ]]; then
  echo "Global stats file not found at $STATS_FILE" >&2
  exit 2
fi

total_requests=$(jq -r '.stats.numberOfRequests.total' "$STATS_FILE")
failed_requests=$(jq -r '.stats.numberOfRequests.ko' "$STATS_FILE")
p95_latency=$(jq -r '.stats.percentiles3.total' "$STATS_FILE")

success_flag=0
if [[ "$failed_requests" == "0" ]]; then
  success_flag=1
fi

function push_metric() {
  local metric_name=$1
  local metric_value=$2
  curl -sS -X POST "$PUSHGATEWAY_URL/metrics/job/$JOB_NAME" \
    --data-binary "$metric_name{simulation=\"$SIMULATION_NAME\"} $metric_value" >/dev/null
}

push_metric "gatling_run_success" "$success_flag"
push_metric "gatling_response_p95_ms" "$p95_latency"
push_metric "gatling_requests_total" "$total_requests"

if [[ "$success_flag" -eq 0 ]]; then
  push_metric "gatling_failed_requests" "$failed_requests"
fi
