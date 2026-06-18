package drivers;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.Collections;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.RemoteWebDriver;

import config.ConfigReader;

public class DriverContext {

	private static ThreadLocal<WebDriver> driver = new ThreadLocal<>();

	public static WebDriver driverInitialize(String browser) {
		String executionMode = ConfigReader.getKeyValue("execution.mode");
		if (executionMode == null) {
			executionMode = "local";
		}

		WebDriver localDriver;

		if ("grid".equalsIgnoreCase(executionMode)) {
			localDriver = createRemoteDriver(browser);
		} else {
			localDriver = createLocalDriver(browser);
		}

		localDriver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));

		String headlessMode = ConfigReader.getKeyValue("headless");
		if (!"true".equalsIgnoreCase(headlessMode)) {
			localDriver.manage().window().maximize();
		}

		driver.set(localDriver);
		return localDriver;
	}

	private static WebDriver createLocalDriver(String browser) {
		switch (browser.toLowerCase()) {
		case "chrome":
			ChromeOptions cOptions = new ChromeOptions();
			cOptions.addArguments("--remote-allow-origins=*");
			cOptions.addArguments("--disable-blink-features=AutomationControlled");
			cOptions.addArguments("--disable-session-crashed-bubble");
			cOptions.addArguments("--disable-infobars");
			cOptions.addArguments("--no-default-browser-check");
			cOptions.setExperimentalOption("excludeSwitches", Collections.singletonList("enable-automation"));
			cOptions.setExperimentalOption("useAutomationExtension", false);

			// Use pre-authenticated Chrome profile to bypass Google 2FA
			String chromeProfilePath = ConfigReader.getKeyValue("chrome.profile.path");
			if (chromeProfilePath != null && !chromeProfilePath.isEmpty()) {
				cOptions.addArguments("--user-data-dir=" + chromeProfilePath);
				cOptions.addArguments("--profile-directory=Default");
			}

			String headless = ConfigReader.getKeyValue("headless");
			if ("true".equalsIgnoreCase(headless)) {
				cOptions.addArguments("--headless=new");
				cOptions.addArguments("--window-size=1920,1080");
				cOptions.addArguments("--disable-gpu");
				cOptions.addArguments("--no-sandbox");
				cOptions.addArguments("--disable-dev-shm-usage");
				cOptions.addArguments("user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/148.0.0.0 Safari/537.36");
			}
			return new ChromeDriver(cOptions);

		case "firefox":
			FirefoxOptions firefoxOptions = new FirefoxOptions();
			return new FirefoxDriver(firefoxOptions);

		case "edge":
			EdgeOptions edgeOptions = new EdgeOptions();
			edgeOptions.addArguments("--remote-allow-origins=*");
			edgeOptions.addArguments("--no-sandbox");
			edgeOptions.addArguments("--disable-dev-shm-usage");
			edgeOptions.addArguments("--disable-blink-features=AutomationControlled");
			edgeOptions.setExperimentalOption("excludeSwitches", Collections.singletonList("enable-automation"));
			edgeOptions.setExperimentalOption("useAutomationExtension", false);
			return new EdgeDriver(edgeOptions);

		default:
			throw new IllegalArgumentException("Unsupported browser: " + browser);
		}
	}

	private static WebDriver createRemoteDriver(String browser) {
		String gridUrl = ConfigReader.getKeyValue("grid.url");
		if (gridUrl == null || gridUrl.isEmpty()) {
			gridUrl = "http://localhost:4444";
		}

		try {
			URL url = new URL(gridUrl);

			switch (browser.toLowerCase()) {
			case "chrome":
				ChromeOptions chromeOptions = new ChromeOptions();
				chromeOptions.addArguments("--remote-allow-origins=*");
				chromeOptions.addArguments("--no-sandbox");
				chromeOptions.addArguments("--disable-dev-shm-usage");
				return new RemoteWebDriver(url, chromeOptions);

			case "firefox":
				FirefoxOptions firefoxOptions = new FirefoxOptions();
				return new RemoteWebDriver(url, firefoxOptions);

			case "edge":
				EdgeOptions edgeOptions = new EdgeOptions();
				edgeOptions.addArguments("--remote-allow-origins=*");
				return new RemoteWebDriver(url, edgeOptions);

			default:
				throw new IllegalArgumentException("Unsupported browser: " + browser);
			}
		} catch (MalformedURLException e) {
			throw new RuntimeException("Invalid Selenium Grid URL: " + gridUrl, e);
		}
	}

	public static WebDriver getDriver() {
		return driver.get();
	}
}
