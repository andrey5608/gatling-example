package org.andrey5608.jenkins;

import io.gatling.app.Gatling;
import io.gatling.core.config.GatlingPropertiesBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class GatlingEntryPoint {

    private static final String DEFAULT_SIMULATION = "org.andrey5608.performance.FastEndpointSimulation";

    private GatlingEntryPoint() {
    }

    public static void main(String[] args) {
        String simulationClass = resolveSimulationClass(args);
        String resultsDir = System.getenv().getOrDefault("GATLING_RESULTS_DIR", "/tmp/gatling-results");
        String runDescription = System.getenv().getOrDefault("GATLING_RUN_DESCRIPTION", "jenkins-ocp-run");

        ensureResultsDir(resultsDir);

        GatlingPropertiesBuilder builder = new GatlingPropertiesBuilder()
            .simulationClass(simulationClass)
            .resultsDirectory(resultsDir)
            .runDescription(runDescription);

        int exitCode = new Gatling().start(builder.build());
        if (exitCode != 0) {
            throw new IllegalStateException("Gatling finished with non-zero status: " + exitCode);
        }
    }

    private static void ensureResultsDir(String resultsDir) {
        try {
            Files.createDirectories(Path.of(resultsDir));
        } catch (IOException e) {
            throw new IllegalStateException("Unable to prepare Gatling results directory: " + resultsDir, e);
        }
    }

    private static String resolveSimulationClass(String[] args) {
        val simulation = DEFAULT_SIMULATION;
        if (args != null && args.length > 0 && args[0] != null && !args[0].isBlank()) {
            return args[0];
        }
        String envSimulation = System.getenv("SIMULATION_CLASS");
        if (envSimulation != null && !envSimulation.isBlank()) {
            simulation = envSimulation;
        }
        System.out.println("Using simulation class: " + simulation);
        return simulation;
    }
}
