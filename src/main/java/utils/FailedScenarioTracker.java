package utils;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility to track failed test scenarios during execution.
 * Stores failed scenario details in a file for re-execution.
 * 
 * The rerun file is stored at: target/failed-scenarios.txt
 * Format: feature_file_path:line_number (Cucumber rerun format)
 */
public class FailedScenarioTracker {

    private static final String FAILED_SCENARIOS_FILE = "target/failed-scenarios.txt";
    private static final String FAILED_SCENARIO_NAMES_FILE = "target/failed-scenario-names.txt";
    private static final List<String> failedScenarioNames = new ArrayList<>();
    private static final List<String> failedScenarioLocations = new ArrayList<>();
    private static int totalScenarios = 0;
    private static int passedScenarios = 0;
    private static int failedScenarioCount = 0;
    private static int skippedScenarios = 0;

    /**
     * Records a scenario result after execution.
     */
    public static synchronized void recordScenarioResult(String scenarioName,
                                                          String scenarioLocation,
                                                          boolean passed) {
        totalScenarios++;
        if (passed) {
            passedScenarios++;
        } else {
            failedScenarioCount++;
            failedScenarioNames.add(scenarioName);
            if (scenarioLocation != null && !scenarioLocation.isEmpty()) {
                failedScenarioLocations.add(scenarioLocation);
            }
        }
    }

    /**
     * Records a skipped scenario.
     */
    public static synchronized void recordSkipped() {
        skippedScenarios++;
    }

    /**
     * Sets the count of skipped scenarios directly (from test results).
     */
    public static synchronized void setSkippedCount(int count) {
        skippedScenarios = count;
    }

    /**
     * Writes the failed scenario locations to a rerun file.
     * This file can be used by Cucumber's @rerun plugin or custom runner.
     */
    public static void writeRerunFile() {
        try {
            // Write rerun locations (feature:line format)
            Path rerunPath = Paths.get(FAILED_SCENARIOS_FILE);
            Files.createDirectories(rerunPath.getParent());

            if (!failedScenarioLocations.isEmpty()) {
                Files.write(rerunPath, failedScenarioLocations);
                System.out.println("[FailedScenarioTracker] Rerun file written: " + FAILED_SCENARIOS_FILE);
                System.out.println("[FailedScenarioTracker] Failed scenarios to rerun: " + failedScenarioLocations.size());
            } else {
                // Write empty file to indicate no failures
                Files.writeString(rerunPath, "");
                System.out.println("[FailedScenarioTracker] No failed scenarios. Rerun file is empty.");
            }

            // Write failed scenario names (for email report)
            Path namesPath = Paths.get(FAILED_SCENARIO_NAMES_FILE);
            if (!failedScenarioNames.isEmpty()) {
                Files.write(namesPath, failedScenarioNames);
            } else {
                Files.writeString(namesPath, "");
            }

        } catch (IOException e) {
            System.err.println("[FailedScenarioTracker] ERROR writing rerun file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Reads the failed scenario names from the file (used after rerun).
     */
    public static List<String> getFailedScenarioNames() {
        return new ArrayList<>(failedScenarioNames);
    }

    /**
     * Reads failed scenario locations from the rerun file.
     */
    public static List<String> readRerunFile() {
        try {
            Path rerunPath = Paths.get(FAILED_SCENARIOS_FILE);
            if (Files.exists(rerunPath)) {
                List<String> lines = Files.readAllLines(rerunPath);
                lines.removeIf(String::isBlank);
                return lines;
            }
        } catch (IOException e) {
            System.err.println("[FailedScenarioTracker] ERROR reading rerun file: " + e.getMessage());
        }
        return new ArrayList<>();
    }

    /**
     * Reads failed scenario names from the names file.
     */
    public static List<String> readFailedScenarioNames() {
        try {
            Path namesPath = Paths.get(FAILED_SCENARIO_NAMES_FILE);
            if (Files.exists(namesPath)) {
                List<String> lines = Files.readAllLines(namesPath);
                lines.removeIf(String::isBlank);
                return lines;
            }
        } catch (IOException e) {
            System.err.println("[FailedScenarioTracker] ERROR reading names file: " + e.getMessage());
        }
        return new ArrayList<>();
    }

    /**
     * Checks if there are any failed scenarios to rerun.
     */
    public static boolean hasFailedScenarios() {
        return !failedScenarioLocations.isEmpty();
    }

    /**
     * Resets all counters (call before a new execution run).
     */
    public static synchronized void reset() {
        failedScenarioNames.clear();
        failedScenarioLocations.clear();
        totalScenarios = 0;
        passedScenarios = 0;
        failedScenarioCount = 0;
        skippedScenarios = 0;
    }

    // Getters for test counts
    public static int getTotalScenarios() { return totalScenarios; }
    public static int getPassedScenarios() { return passedScenarios; }
    public static int getFailedScenarioCount() { return failedScenarioCount; }
    public static int getSkippedScenarios() { return skippedScenarios; }

    /**
     * Prints the execution summary to console.
     */
    public static void printSummary() {
        System.out.println("========================================");
        System.out.println("  TEST EXECUTION SUMMARY");
        System.out.println("========================================");
        System.out.println("  Total    : " + totalScenarios);
        System.out.println("  Passed   : " + passedScenarios);
        System.out.println("  Failed   : " + failedScenarioCount);
        System.out.println("  Skipped  : " + skippedScenarios);
        System.out.println("========================================");
        if (!failedScenarioNames.isEmpty()) {
            System.out.println("  FAILED SCENARIOS:");
            for (String name : failedScenarioNames) {
                System.out.println("    - " + name);
            }
            System.out.println("========================================");
        }
    }
}
