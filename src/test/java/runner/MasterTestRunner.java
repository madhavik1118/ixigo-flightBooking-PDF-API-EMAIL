package runner;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import utils.EmailUtil;
import utils.FailedScenarioTracker;

/**
 * Master Test Runner that orchestrates the complete test execution lifecycle:
 * 
 * 1. Executes all test scenarios (both @ui and @api)
 * 2. Tracks failed scenarios
 * 3. Re-executes only the failed test scripts from the previous run
 * 4. Sends an email with the Extent Report attached after execution completion
 * 
 * Usage: Run this class as a Java application (main method)
 *        OR use Maven: mvn exec:java -Dexec.mainClass="runner.MasterTestRunner" -Dexec.classpathScope=test
 */
public class MasterTestRunner {

    private static final String EXTENT_REPORT_PATH = System.getProperty("user.dir") + "/target/ExtentReport.html";
    private static final String RERUN_FILE = "target/failed-scenarios.txt";
    private static final String ALL_FAILURES_FILE = "target/all-failed-scenarios.txt";
    private static final String PROJECT_DIR = System.getProperty("user.dir");

    public static void main(String[] args) {
        System.out.println("================================================================");
        System.out.println("  MASTER TEST RUNNER - Starting Full Execution Lifecycle");
        System.out.println("================================================================");

        // Clear accumulated failures file from any previous run
        clearFile(ALL_FAILURES_FILE);

        // Step 1: Run @ui tagged scenarios (Flight booking tests)
        System.out.println("\n>>> STEP 1: Running @ui tagged scenarios...\n");
        runMavenTests("@ui");
        accumulateFailures();

        // Step 2: Run @api tagged scenarios (API tests)
        System.out.println("\n>>> STEP 2: Running @api tagged scenarios...\n");
        runMavenTests("@api");
        accumulateFailures();

        // Step 3: Run @pdf tagged scenarios (PDF validation tests)
        System.out.println("\n>>> STEP 3: Running @pdf tagged scenarios...\n");
        runMavenTests("@pdf");
        accumulateFailures();

        // Step 4: Check if any failures were accumulated across all steps
        System.out.println("\n>>> STEP 4: Checking for failed scenarios...\n");
        boolean hasFailures = isFilePopulated(ALL_FAILURES_FILE);

        // Step 5: Re-execute failed tests if any failures accumulated
        if (hasFailures) {
            List<String> failedEntries = readFileEntries(ALL_FAILURES_FILE);
            System.out.println("\n>>> STEP 5: Re-executing " + failedEntries.size() + " failed scenario(s)...\n");
            for (String entry : failedEntries) {
                System.out.println("    - " + entry);
            }
            runRerunTests();
        } else {
            System.out.println("\n>>> STEP 5: No failed scenarios to rerun. Skipping.\n");
        }

        // Step 6: Send email with report
        System.out.println("\n>>> STEP 6: Sending email with Extent Report...\n");
        sendExecutionEmail();

        System.out.println("\n================================================================");
        System.out.println("  MASTER TEST RUNNER - Execution Complete");
        System.out.println("================================================================");
    }

    /**
     * Runs Maven tests with the specified Cucumber tag filter.
     */
    private static int runMavenTests(String tagExpression) {
        try {
            String command = "mvn test -Dcucumber.filter.tags=\"" + tagExpression + "\" -Dsurefire.useFile=false";
            System.out.println("  Executing: " + command);
            System.out.println("  Working Dir: " + PROJECT_DIR);

            ProcessBuilder pb = new ProcessBuilder("cmd", "/c", "mvn", "test",
                    "-Dtest=TestRunner",
                    "-Dcucumber.filter.tags=" + tagExpression,
                    "-Dsurefire.useFile=false");
            pb.directory(new File(PROJECT_DIR));
            pb.redirectErrorStream(true);

            Process process = pb.start();
            printProcessOutput(process);
            int exitCode = process.waitFor();

            System.out.println("  Maven test exit code: " + exitCode);
            return exitCode;

        } catch (IOException | InterruptedException e) {
            System.err.println("  ERROR running Maven tests: " + e.getMessage());
            e.printStackTrace();
            return 1;
        }
    }

    /**
     * Re-runs failed scenarios by reading the accumulated failures file,
     * converting classpath references to file paths, and passing them directly
     * to Cucumber via cucumber.features property with only the cucumber engine enabled.
     */
    private static int runRerunTests() {
        try {
            System.out.println("  Re-running failed scenarios using Cucumber engine directly...");
            System.out.println("  Failures file: " + ALL_FAILURES_FILE);

            // Read the accumulated failures file and convert classpath: references to file paths
            String featuresArg = buildFeaturesArgFromFile(ALL_FAILURES_FILE);
            if (featuresArg == null || featuresArg.isEmpty()) {
                System.out.println("  No valid feature references found. Skipping.");
                return 0;
            }
            System.out.println("  Features arg: " + featuresArg);

            ProcessBuilder pb = new ProcessBuilder("cmd", "/c", "mvn", "test",
                    "-Dcucumber.features=" + featuresArg,
                    "-Dcucumber.glue=stepDefinitions,hooks",
                    "-Dcucumber.plugin=pretty,html:target/rerun-cucumber-reports.html",
                    "-Dsurefire.includeJUnit5Engines=cucumber",
                    "-Dsurefire.useFile=false");
            pb.directory(new File(PROJECT_DIR));
            pb.redirectErrorStream(true);

            Process process = pb.start();
            printProcessOutput(process);
            int exitCode = process.waitFor();

            System.out.println("  Rerun exit code: " + exitCode);
            return exitCode;

        } catch (IOException | InterruptedException e) {
            System.err.println("  ERROR during rerun: " + e.getMessage());
            e.printStackTrace();
            return 1;
        }
    }

    /**
     * After each step, reads failed-scenarios.txt (written by Cucumber rerun plugin)
     * and appends unique entries to the accumulated all-failed-scenarios.txt file.
     */
    private static void accumulateFailures() {
        try {
            Path rerunPath = Paths.get(RERUN_FILE);
            Path allFailuresPath = Paths.get(ALL_FAILURES_FILE);

            if (!Files.exists(rerunPath)) return;

            String content = Files.readString(rerunPath).trim();
            if (content.isEmpty()) return;

            // Read existing accumulated entries to avoid duplicates
            Set<String> existingEntries = new LinkedHashSet<>();
            if (Files.exists(allFailuresPath)) {
                List<String> existing = Files.readAllLines(allFailuresPath);
                existing.forEach(line -> {
                    if (!line.isBlank()) existingEntries.add(line.trim());
                });
            }

            // Parse new failures (could be space-separated or newline-separated)
            String[] newEntries = content.split("[\\s]+");
            StringBuilder toAppend = new StringBuilder();
            for (String entry : newEntries) {
                entry = entry.trim();
                if (!entry.isEmpty() && !existingEntries.contains(entry)) {
                    existingEntries.add(entry);
                    toAppend.append(entry).append(System.lineSeparator());
                }
            }

            if (toAppend.length() > 0) {
                Files.writeString(allFailuresPath, toAppend.toString(),
                        StandardOpenOption.CREATE, StandardOpenOption.APPEND);
                System.out.println("  Accumulated " + newEntries.length + " failure(s) from this step.");
            }

        } catch (IOException e) {
            System.err.println("  ERROR accumulating failures: " + e.getMessage());
        }
    }

    /**
     * Reads a file and converts classpath: references to file paths.
     */
    private static String buildFeaturesArgFromFile(String filePath) {
        try {
            Path path = Paths.get(filePath);
            if (!Files.exists(path)) return null;

            String content = Files.readString(path).trim();
            if (content.isEmpty()) return null;

            // Replace classpath: prefix with actual source path
            content = content.replace("classpath:", "src/test/resources/");
            // Join multiple lines with space for cucumber.features property
            content = content.replaceAll("[\\r\\n]+", " ").trim();
            return content;
        } catch (IOException e) {
            System.err.println("  ERROR reading file: " + e.getMessage());
            return null;
        }
    }

    /**
     * Checks if a file exists and has content.
     */
    private static boolean isFilePopulated(String filePath) {
        try {
            Path path = Paths.get(filePath);
            if (Files.exists(path)) {
                String content = Files.readString(path).trim();
                return !content.isEmpty();
            }
        } catch (IOException e) {
            System.err.println("  ERROR reading file: " + e.getMessage());
        }
        return false;
    }

    /**
     * Reads the entries from a file.
     */
    private static List<String> readFileEntries(String filePath) {
        try {
            Path path = Paths.get(filePath);
            if (Files.exists(path)) {
                List<String> lines = Files.readAllLines(path);
                lines.removeIf(String::isBlank);
                return lines;
            }
        } catch (IOException e) {
            System.err.println("  ERROR reading file: " + e.getMessage());
        }
        return List.of();
    }

    /**
     * Clears a file (or creates it empty) to start fresh.
     */
    private static void clearFile(String filePath) {
        try {
            Files.writeString(Paths.get(filePath), "", 
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            // Ignore - file may not exist yet
        }
    }

    /**
     * Sends the execution report email.
     * Reads failed scenario names from file since tests run in a separate JVM.
     */
    private static void sendExecutionEmail() {
        List<String> failedNames = FailedScenarioTracker.readFailedScenarioNames();

        // Read accumulated failures file to count failures
        List<String> allFailures = readFileEntries(ALL_FAILURES_FILE);
        int failed = allFailures.size();

        // We don't have exact total/passed/skipped from the child JVM,
        // so read from the failed-scenario-names file for the email
        int total = 0;
        int passed = 0;
        int skipped = 0;

        EmailUtil.sendReportFromConfig(EXTENT_REPORT_PATH, total, passed, failed, skipped, failedNames);
    }

    /**
     * Prints process output to console.
     */
    private static void printProcessOutput(Process process) throws IOException {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println("    " + line);
            }
        }
    }
}
