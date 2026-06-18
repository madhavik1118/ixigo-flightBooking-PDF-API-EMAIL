package runner;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.Suite;
import io.cucumber.core.options.Constants;

/**
 * Runner for re-executing only the failed test scenarios from the previous run.
 * 
 * This runner uses the Cucumber rerun plugin output file (target/failed-scenarios.txt)
 * which contains the feature file locations of failed scenarios.
 * 
 * Usage:
 *   1. Run TestRunner first (it writes failed-scenarios.txt via rerun plugin)
 *   2. Then run this class to re-execute only the failed ones
 *   
 * Maven command:
 *   mvn test -Dtest=RerunFailedTestRunner -Dsurefire.useFile=false
 */
@Suite
@IncludeEngines("cucumber")
@ConfigurationParameter(key = Constants.GLUE_PROPERTY_NAME, value = "stepDefinitions, hooks")
@ConfigurationParameter(key = Constants.PLUGIN_PROPERTY_NAME,
        value = "pretty, html:target/rerun-cucumber-reports.html")
@ConfigurationParameter(key = Constants.FEATURES_PROPERTY_NAME, value = "@target/failed-scenarios.txt")
@ConfigurationParameter(key = "cucumber.ansi-colors.disabled", value = "true")
public class RerunFailedTestRunner {
}
