package runner;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;
import io.cucumber.core.options.Constants;

@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features/ApiUserManagement.feature")
@ConfigurationParameter(key = Constants.GLUE_PROPERTY_NAME, value = "stepDefinitions, hooks")
@ConfigurationParameter(key = Constants.PLUGIN_PROPERTY_NAME,
        value = "pretty, html:target/api-cucumber-reports.html")
@ConfigurationParameter(key = Constants.FILTER_TAGS_PROPERTY_NAME, value = "@api")
@ConfigurationParameter(key = "cucumber.ansi-colors.disabled", value = "true")
public class ApiTestRunner {
}
