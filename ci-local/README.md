# Local Jenkins + Artifactory Sandbox

Spin up a disposable environment to validate the `jenkins-ocp-tests` pipeline without touching production Jenkins or registries.

## Prerequisites
- Docker / Docker Compose v2
- GitHub access to clone this repo

## Usage
1. Build and launch Jenkins + Artifactory:
   ```bash
   docker compose -f ci-local/docker-compose.yml up -d --build
   ```
2. Wait for Artifactory (`http://localhost:8081`) and Jenkins (`http://localhost:8080`, admin/admin) to become healthy.
3. Inside Jenkins, verify the seeded `gatling-jenkins-ocp-tests` pipeline (auto-created via JCasC). Update the repo URL/branch if needed.
4. Generate an Artifactory access token or set a password, then update the `artifactory-creds` credential in Jenkins so Jib can push images.
5. Update the `ocp-token` credential with a valid token if you want to hit a real OpenShift cluster. For purely local smoke tests, leave the default dummy and skip the OpenShift stage.
6. Trigger the pipeline manually. It will run `mvn jib:build` to push the Gatling runner image into Artifactory (`localhost:8081`) and attempt to execute the OpenShift stage (configure connectivity or mock it out as needed).

## Tear Down
```bash
docker compose -f ci-local/docker-compose.yml down -v
```

## Notes
- The Jenkins image installs Maven, `oc`, and `jq` so the pipeline matches CI tooling.
- Artifactory data persists under the `artifactory_data` volume; wipe with `docker volume rm` if you want a clean registry.
- Adjust `ci-local/jenkins/plugins.txt` to add plugins if the pipeline needs extras.
