package hooks;

import org.openqa.selenium.WebDriver;

import com.aventstack.extentreports.ExtentTest;

import config.ConfigReader;
import drivers.DriverContext;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import utils.ExcelUtil;
import utils.ExtentReporter;
import utils.FailedScenarioTracker;
import utils.ScreenshotUtil;

public class Hooks {
	private WebDriver driver;
	public static ExtentTest test;

	@Before("@ui")
	public void beforeScenario(Scenario scenario) {
		ExtentReporter.initializeReport();
		test = ExtentReporter.createTest(scenario.getName());
		test.info("Starting Scenario " + scenario.getName());
		String excelPath = System.getProperty("user.dir") + "/src/test/resources/testData/Ixigo.xlsx";
		ExcelUtil.getSheetData(excelPath, "QA", scenario.getName());
		ConfigReader config = new ConfigReader();
		driver = DriverContext.driverInitialize(ConfigReader.getKeyValue("browser"));
	}

	@After("@ui")
	public void afterScenario(Scenario scenario) throws InterruptedException {
		try {
			Thread.sleep(1000);
			if (DriverContext.getDriver() != null) {
				String path = ScreenshotUtil.takeScreenshot();
				Thread.sleep(2000);
				if (path != null && !path.isEmpty()) {
					test.addScreenCaptureFromPath(path);
				}
			}
		} catch (Exception e) {
			test.info("Screenshot capture failed: " + e.getMessage());
		}

		try {
			if (DriverContext.getDriver() != null) {
				DriverContext.getDriver().quit();
			}
		} catch (Exception e) {
			test.info("Browser close failed: " + e.getMessage());
		}

		if (scenario.isFailed()) {
			test.fail(scenario.getName() + " is failed");
			FailedScenarioTracker.recordScenarioResult(
					scenario.getName(), scenario.getUri() + ":" + scenario.getLine(), false);
		} else {
			test.pass(scenario.getName() + " is passed");
			FailedScenarioTracker.recordScenarioResult(scenario.getName(), null, true);
		}
		ExtentReporter.initializeReport().flush();
	}

	@Before("@pdf")
	public void beforeScenarioPdf(Scenario scenario) {
		ExtentReporter.initializeReport();
		test = ExtentReporter.createTest(scenario.getName());
		test.info("Starting Scenario " + scenario.getName());
		/*String excelPath = System.getProperty("user.dir") + "/src/test/resources/testData/Ixigo.xlsx";
		ExcelUtil.getSheetData(excelPath, "QA", scenario.getName());*/
		ConfigReader config = new ConfigReader();
	}

	@After("@pdf")
	public void afterScenarioPdf(Scenario scenario) throws InterruptedException {
		if (scenario.isFailed()) {
			test.fail(scenario.getName() + " is failed");
			FailedScenarioTracker.recordScenarioResult(
					scenario.getName(), scenario.getUri() + ":" + scenario.getLine(), false);
		} else {
			test.pass(scenario.getName() + " is passed");
			FailedScenarioTracker.recordScenarioResult(scenario.getName(), null, true);
		}
		ExtentReporter.initializeReport().flush();
	}

	@Before("@api")
	public void beforeScenarioApi(Scenario scenario) {
		ExtentReporter.initializeReport();
		test = ExtentReporter.createTest(scenario.getName());
		test.info("Starting API Scenario: " + scenario.getName());
	}

	@After("@api")
	public void afterScenarioApi(Scenario scenario) {
		if (scenario.isFailed()) {
			test.fail(scenario.getName() + " is failed");
			FailedScenarioTracker.recordScenarioResult(
					scenario.getName(), scenario.getUri() + ":" + scenario.getLine(), false);
		} else {
			test.pass(scenario.getName() + " is passed");
			FailedScenarioTracker.recordScenarioResult(scenario.getName(), null, true);
		}
		ExtentReporter.initializeReport().flush();
	}

	public static ExtentTest getTest() {
		return test;
	}
}
