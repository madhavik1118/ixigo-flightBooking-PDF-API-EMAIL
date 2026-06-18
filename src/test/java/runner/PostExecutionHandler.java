package runner;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import utils.EmailUtil;
import utils.FailedScenarioTracker;

/**
 * Post-Execution Handler that can be invoked after test execution to:
 * 1. Parse test results from surefire reports
 * 2. Send email with Extent Report
 * 3. Trigger re-execution of failed tests
 * 
 * Usage: 
 *   mvn exec:java -Dexec.mainClass="runner.PostExecutionHandler" -Dexec.classpathScope=test
 *   OR
 *   mvn exec:java -Dexec.mainClass="runner.PostExecutionHandler" -Dexec.classpathScope=test -Dexec.args="rerun"
 */
public class PostExecutionHandler {

    private static final String PROJECT_DIR = System.getProperty("user.dir");
    private static final String EXTENT_REPORT_PATH = PROJECT_DIR + "/target/ExtentReport.html";
    private static final String CUCUMBER_REPORT_PATH = PROJECT_DIR + "/target/cucumber-reports.html";
    private static final String SUREFIRE_DIR = PROJECT_DIR + "/target/surefire-reports";
    private static final String RERUN_FILE = PROJECT_DIR + "/target/failed-scenarios.txt";

    public static void main(String[] args) {
        boolean doRerun = args.length > 0 && args[0].equalsIgnoreCase("rerun");

        System.out.println("================================================================");
        System.out.println("  POST-EXECUTION HANDLER");
        System.out.println("================================================================");

        // Step 1: Parse results
        TestResults results = parseSurefireResults();
        System.out.println("\n  Test Results from Surefire:");
        System.out.println("    Total   : " + results.total);
        System.out.println("    Passed  : " + results.passed);
        System.out.println("    Failed  : " + results.failed);
        System.out.println("    Skipped : " + results.skipped);

        if (!results.failedScenarios.isEmpty()) {
            System.out.println("\n  Failed Scenarios:");
            for (String name : results.failedScenarios) {
                System.out.println("    - " + name);
            }
        }

        // Step 2: Re-run failed tests if requested
        if (doRerun && results.failed > 0) {
            System.out.println("\n>>> Re-running " + results.failed + " failed scenario(s)...");
            rerunFailedTests(results.failedScenarios);
            // Re-parse results after rerun
            TestResults rerunResults = parseSurefireResults();
            System.out.println("\n  Rerun Results:");
            System.out.println("    Total   : " + rerunResults.total);
            System.out.println("    Passed  : " + rerunResults.passed);
            System.out.println("    Failed  : " + rerunResults.failed);
        }

        // Step 3: Send email
        System.out.println("\n>>> Sending email with test report...");
        String reportPath = new File(EXTENT_REPORT_PATH).exists() ? EXTENT_REPORT_PATH : CUCUMBER_REPORT_PATH;

        EmailUtil.sendReportFromConfig(
                reportPath,
                results.total,
                results.passed,
                results.failed,
                results.skipped,
                results.failedScenarios
        );

        System.out.println("\n================================================================");
        System.out.println("  POST-EXECUTION HANDLER - Complete");
        System.out.println("================================================================");
    }

    /**
     * Parses Surefire test results from XML reports.
     */
    private static TestResults parseSurefireResults() {
        TestResults results = new TestResults();

        File surefireDir = new File(SUREFIRE_DIR);
        if (!surefireDir.exists() || !surefireDir.isDirectory()) {
            System.out.println("  WARNING: Surefire reports directory not found: " + SUREFIRE_DIR);
            // Try to get from FailedScenarioTracker in-memory data
            results.total = FailedScenarioTracker.getTotalScenarios();
            results.passed = FailedScenarioTracker.getPassedScenarios();
            results.failed = FailedScenarioTracker.getFailedScenarioCount();
            results.skipped = FailedScenarioTracker.getSkippedScenarios();
            results.failedScenarios = FailedScenarioTracker.getFailedScenarioNames();
            return results;
        }

        // Parse XML files in surefire-reports
        File[] xmlFiles = surefireDir.listFiles((dir, name) -> name.startsWith("TEST-") && name.endsWith(".xml"));
        if (xmlFiles == null || xmlFiles.length == 0) {
            System.out.println("  WARNING: No surefire XML reports found.");
            return results;
        }

        for (File xmlFile : xmlFiles) {
            try {
                String content = Files.readString(xmlFile.toPath());

                // Parse testsuite attributes
                Pattern suitePattern = Pattern.compile(
                        "tests=\"(\\d+)\".*?failures=\"(\\d+)\".*?errors=\"(\\d+)\".*?skipped=\"(\\d+)\"");
                Matcher suiteMatcher = suitePattern.matcher(content);
                if (suiteMatcher.find()) {
                    int tests = Integer.parseInt(suiteMatcher.group(1));
                    int failures = Integer.parseInt(suiteMatcher.group(2));
                    int errors = Integer.parseInt(suiteMatcher.group(3));
                    int skipped = Integer.parseInt(suiteMatcher.group(4));

                    results.total += tests;
                    results.failed += (failures + errors);
                    results.skipped += skipped;
                    results.passed += (tests - failures - errors - skipped);
                }

                // Parse failed test case names
                Pattern failurePattern = Pattern.compile("testcase.*?name=\"([^\"]+)\"[^>]*>\\s*<failure");
                Matcher failureMatcher = failurePattern.matcher(content);
                while (failureMatcher.find()) {
                    results.failedScenarios.add(failureMatcher.group(1));
                }

            } catch (IOException e) {
                System.err.println("  Error reading: " + xmlFile.getName() + " - " + e.getMessage());
            }
        }

        return results;
    }

    /**
     * Re-runs failed test scenarios.
     */
    private static void rerunFailedTests(List<String> failedScenarios) {
        if (failedScenarios.isEmpty()) {
            System.out.println("  No failed scenarios to rerun.");
            return;
        }

        try {
            // Build scenario name filter for Cucumber
            StringBuilder nameFilter = new StringBuilder();
            for (String name : failedScenarios) {
                if (nameFilter.length() > 0) {
                    nameFilter.append("|");
                }
                // Escape regex special characters in scenario names
                nameFilter.append(Pattern.quote(name));
            }

            System.out.println("  Rerun filter: " + nameFilter);

            ProcessBuilder pb = new ProcessBuilder("cmd", "/c", "mvn", "test",
                    "-Dcucumber.filter.name=" + nameFilter,
                    "-Dsurefire.useFile=false");
            pb.directory(new File(PROJECT_DIR));
            pb.redirectErrorStream(true);

            Process process = pb.start();

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println("    [RERUN] " + line);
                }
            }

            int exitCode = process.waitFor();
            System.out.println("  Rerun exit code: " + exitCode);

        } catch (Exception e) {
            System.err.println("  ERROR during rerun: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Inner class to hold test results.
     */
    static class TestResults {
        int total = 0;
        int passed = 0;
        int failed = 0;
        int skipped = 0;
        List<String> failedScenarios = new ArrayList<>();
    }
}
