#!/usr/bin/env bash
set -euo pipefail

if [[ $# -lt 2 ]]; then
  echo "Usage: $0 <registry-host/project> <tag>" >&2
  echo "Example: $0 image-registry.openshift-image-registry.svc:5000/perf-lab/gatling-runner latest" >&2
  exit 1
fi

IMAGE_PATH=$1
IMAGE_TAG=$2
REPO_ROOT=$(cd "$(dirname "$0")/.." && pwd)

FULL_IMAGE="${IMAGE_PATH}:${IMAGE_TAG}"

echo "Building ${FULL_IMAGE}..."
docker build -f "$REPO_ROOT/jenkins-ocp-tests/Dockerfile" -t "$FULL_IMAGE" "$REPO_ROOT"

echo "Pushing ${FULL_IMAGE}..."
docker push "$FULL_IMAGE"

echo "Done."
