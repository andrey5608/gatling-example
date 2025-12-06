package org.andrey5608.jenkins;

import io.gatling.app.Gatling;
import io.gatling.app.Gatling$;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class GatlingEntryPoint {

    private static final String DEFAULT_SIMULATION = "org.andrey5608.performance.FastEndpointSimulation";

    private GatlingEntryPoint() {
    }

    public static void main(String[] args) {
        String simulationClass = resolveSimulationClass(args);
        String resultsDir = System.getenv().getOrDefault("GATLING_RESULTS_DIR", "/tmp/gatling-results");
        String runDescription = System.getenv().getOrDefault("GATLING_RUN_DESCRIPTION", "jenkins-ocp-run");

        ensureResultsDir(resultsDir);

        String[] gatlingArgs = buildGatlingArgs(simulationClass, resultsDir, runDescription);
        int exitCode = Gatling$.MODULE$.fromArgs(gatlingArgs);
        if (exitCode != 0) {
            throw new IllegalStateException("Gatling finished with non-zero status: " + exitCode);
        }
    }

    private static String[] buildGatlingArgs(String simulationClass, String resultsDir, String runDescription) {
        List<String> args = new ArrayList<>();
        args.add("-s");
        args.add(simulationClass);
        args.add("-rd");
        args.add(runDescription);
        args.add("-rf");
        args.add(resultsDir);
        return args.toArray(new String[0]);
    }

    private static void ensureResultsDir(String resultsDir) {
        try {
            Files.createDirectories(Path.of(resultsDir));
        } catch (IOException e) {
            throw new IllegalStateException("Unable to prepare Gatling results directory: " + resultsDir, e);
        }
    }

    private static String resolveSimulationClass(String[] args) {
        String simulation = DEFAULT_SIMULATION;
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
