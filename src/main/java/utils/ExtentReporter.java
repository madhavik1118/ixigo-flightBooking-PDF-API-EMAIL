package utils;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;

import config.ConfigReader;

public class ExtentReporter {
	public static ExtentReports extent;
	public static ExtentTest extentTest;

	public static ExtentReports initializeReport() {
		if (extent == null) {
//			ConfigReader config=new ConfigReader();
			extent = new ExtentReports();
			ExtentSparkReporter spark = new ExtentSparkReporter("target/ExtentReport.html");
			extent.attachReporter(spark);
		}
		return extent;
	}
	public static ExtentTest createTest(String scenarioName) {
		extentTest= extent.createTest(scenarioName);
		return extentTest;
	}
}
