package pages;

import java.time.Duration;
import java.util.List;
import java.util.Set;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import base.BasePage;
import utils.ExcelUtil;
import utils.ExtentReporter;

public class GmailLoginPage extends BasePage {

	public GmailLoginPage(WebDriver driver) {
		super(driver);
	}

	By userName = By.xpath("//input[@type='email' or @id='identifierId']");
	By password = By.xpath("//input[@type='password' or @name='Passwd']");
	By nextButton = By.xpath("//span[text()='Next']/ancestor::button | //button[contains(@class,'VfPpkd')]//span[text()='Next']/ancestor::button");

	public void loginWithGmailAccount() throws InterruptedException {
		Thread.sleep(2000);
		
		// Check if user is already logged in via Chrome profile (no login needed)
		if (isAlreadyLoggedIn()) {
			ExtentReporter.extentTest.info("User is already logged in via Chrome profile. Skipping Gmail login.");
			return;
		}
		
		String currentWindow = driver.getWindowHandle();
		int initialWindowCount = driver.getWindowHandles().size();

		ExtentReporter.extentTest.info("Current window count before Google click: " + initialWindowCount);
		
		clickOnGoogleAccount();
		
		// Wait for the Google login popup window to open
		Thread.sleep(1500);
		int newWindowCount = driver.getWindowHandles().size();
		ExtentReporter.extentTest.info("Window count after Google click: " + newWindowCount);

		if (newWindowCount > initialWindowCount) {
			// Popup flow - switch to Google window
			switchToGoogleAcountWindow(currentWindow);
			ExtentReporter.extentTest.info("Switched to Google popup window");

			// Log the page title and URL for debugging
			Thread.sleep(1500);
			ExtentReporter.extentTest.info("Google popup URL: " + driver.getCurrentUrl());
			ExtentReporter.extentTest.info("Google popup Title: " + driver.getTitle());

			// Handle "Choose an account" / "Use another account" page
			By useAnotherAccount = By.xpath("//*[contains(text(),'Use another account')]");
			By addAnotherAccount = By.xpath("//*[contains(text(),'Add another account')]");
			try {
				WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(10));
				try {
					shortWait.until(ExpectedConditions.elementToBeClickable(useAnotherAccount)).click();
					ExtentReporter.extentTest.info("Clicked 'Use another account'");
				} catch (Exception e1) {
					try {
						shortWait.until(ExpectedConditions.elementToBeClickable(addAnotherAccount)).click();
						ExtentReporter.extentTest.info("Clicked 'Add another account'");
					} catch (Exception e2) {
						ExtentReporter.extentTest.info("No account chooser found, looking for email field directly");
					}
				}
				Thread.sleep(1000);
			} catch (Exception e) {
				ExtentReporter.extentTest.info("Account chooser handling exception: " + e.getMessage());
			}

			// Wait for page to fully load
			new WebDriverWait(driver, Duration.ofSeconds(15)).until(
					webDriver -> ((JavascriptExecutor) webDriver)
							.executeScript("return document.readyState").equals("complete"));

			// Wait for email field and enter username
			ExtentReporter.extentTest.info("Looking for email input field...");
			wait.until(ExpectedConditions.visibilityOfElementLocated(userName));
			wait.until(ExpectedConditions.elementToBeClickable(userName));
			enterText(userName, ExcelUtil.getCellValue(testData, "Gmail"), "Username", "Gmail Login");
			clickElement(nextButton, "Next Button", "Gmail Login");

			// Wait for password page to fully load after transition
			wait.until(ExpectedConditions.visibilityOfElementLocated(password));
			Thread.sleep(1000);
			wait.until(ExpectedConditions.elementToBeClickable(password));
			enterText(password, ExcelUtil.getCellValue(testData, "Password"), "Password", "Gmail Login");

			// Wait for the Next button on password page to be clickable
			wait.until(ExpectedConditions.elementToBeClickable(nextButton));
			clickElement(nextButton, "Next Button", "Gmail Login");

			// Wait briefly for login to process before switching back
			Thread.sleep(3000);
			driver.switchTo().window(currentWindow);
		} else {
			// No popup opened - try alternate login approach
			ExtentReporter.extentTest.info("No popup window detected. Trying alternate login flow.");
			ExtentReporter.extentTest.info("Current URL: " + driver.getCurrentUrl());
			
			// Check if there's a Google login form directly on the page
			try {
				wait.until(ExpectedConditions.visibilityOfElementLocated(userName));
				enterText(userName, ExcelUtil.getCellValue(testData, "Gmail"), "Username", "Gmail Login");
				clickElement(nextButton, "Next Button", "Gmail Login");
				
				wait.until(ExpectedConditions.visibilityOfElementLocated(password));
				Thread.sleep(1000);
				wait.until(ExpectedConditions.elementToBeClickable(password));
				enterText(password, ExcelUtil.getCellValue(testData, "Password"), "Password", "Gmail Login");
				clickElement(nextButton, "Next Button", "Gmail Login");
			} catch (Exception e) {
				ExtentReporter.extentTest.info("Alternate login flow also failed: " + e.getMessage());
				throw e;
			}
		}
	}

	/**
	 * Checks if the user is already logged in on ixigo (via Chrome profile session).
	 * Looks for indicators like user profile icon, logged-in user name, or absence of login button.
	 */
	private boolean isAlreadyLoggedIn() {
		try {
			WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(10));
			
			// Check if we're already past the login page (e.g., on traveller details page)
			String currentUrl = driver.getCurrentUrl();
			ExtentReporter.extentTest.info("Current URL during login check: " + currentUrl);
			
			// If URL contains booking/traveller/review keywords, user is already logged in
			if (currentUrl.contains("traveller") || currentUrl.contains("booking") || 
				currentUrl.contains("review") || currentUrl.contains("passenger")) {
				return true;
			}
			
			// Check if there's a user profile element visible (indicating logged-in state)
			By loggedInIndicator = By.xpath("//*[contains(@class,'user-profile') or contains(@class,'user-icon') or contains(@class,'logged-in') or contains(@class,'avatar')]");
			By travelerForm = By.xpath("//input[contains(@placeholder,'First') or contains(@placeholder,'first') or contains(@name,'firstName')]");
			
			try {
				shortWait.until(ExpectedConditions.visibilityOfElementLocated(travelerForm));
				ExtentReporter.extentTest.info("Traveller form detected - user is already logged in");
				return true;
			} catch (Exception e) {
				// Not on traveller form yet
			}
			
			try {
				WebDriverWait veryShortWait = new WebDriverWait(driver, Duration.ofSeconds(5));
				veryShortWait.until(ExpectedConditions.visibilityOfElementLocated(loggedInIndicator));
				ExtentReporter.extentTest.info("User profile indicator found - already logged in");
				return true;
			} catch (Exception e) {
				// Not logged in
			}
			
			return false;
		} catch (Exception e) {
			return false;
		}
	}

	public void clickOnGoogleAccount() throws InterruptedException {
		// Try multiple approaches to click the Google sign-in button
		
		// Approach 1: Google iframe with "Sign in with Google Button" title
		By iframeLocator1 = By.cssSelector("iframe[title='Sign in with Google Button']");
		// Approach 2: Google iframe with accounts.google.com src
		By iframeLocator2 = By.cssSelector("iframe[src*='accounts.google.com']");
		// Approach 3: Direct Google login button on the page (no iframe)
		By googleLoginBtn = By.xpath("//*[contains(@id,'google') or contains(@class,'google')]");
		
		By gAccount = By.cssSelector("[id='container-div']");
		By gAccountAlt = By.cssSelector("div[role='button']");
		
		boolean clicked = false;
		
		// Try iframe approach 1
		try {
			WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(15));
			shortWait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(iframeLocator1));
			ExtentReporter.extentTest.info("Switched to Google iframe (title match)");
			try {
				shortWait.until(ExpectedConditions.elementToBeClickable(gAccount)).click();
				clicked = true;
			} catch (Exception e) {
				shortWait.until(ExpectedConditions.elementToBeClickable(gAccountAlt)).click();
				clicked = true;
			}
			driver.switchTo().defaultContent();
		} catch (Exception e) {
			driver.switchTo().defaultContent();
			ExtentReporter.extentTest.info("Iframe approach 1 failed: " + e.getMessage());
		}
		
		// Try iframe approach 2
		if (!clicked) {
			try {
				WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(10));
				shortWait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(iframeLocator2));
				ExtentReporter.extentTest.info("Switched to Google iframe (src match)");
				try {
					shortWait.until(ExpectedConditions.elementToBeClickable(gAccount)).click();
					clicked = true;
				} catch (Exception e) {
					shortWait.until(ExpectedConditions.elementToBeClickable(gAccountAlt)).click();
					clicked = true;
				}
				driver.switchTo().defaultContent();
			} catch (Exception e) {
				driver.switchTo().defaultContent();
				ExtentReporter.extentTest.info("Iframe approach 2 failed: " + e.getMessage());
			}
		}
		
		// Try direct button click (no iframe)
		if (!clicked) {
			try {
				WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(10));
				List<WebElement> googleButtons = driver.findElements(googleLoginBtn);
				ExtentReporter.extentTest.info("Found " + googleButtons.size() + " Google login elements");
				for (WebElement btn : googleButtons) {
					if (btn.isDisplayed()) {
						((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn);
						clicked = true;
						ExtentReporter.extentTest.info("Clicked Google login button directly");
						break;
					}
				}
			} catch (Exception e) {
				ExtentReporter.extentTest.info("Direct button approach failed: " + e.getMessage());
			}
		}
		
		if (!clicked) {
			// Log page source snippet for debugging
			String pageTitle = driver.getTitle();
			String currentUrl = driver.getCurrentUrl();
			ExtentReporter.extentTest.info("Page title: " + pageTitle + ", URL: " + currentUrl);
			throw new RuntimeException("Could not find or click Google Sign-In button on the page");
		}
		
		Thread.sleep(1000);
	}

	public void switchToGoogleAcountWindow(String originalWindow) {
		wait.until(ExpectedConditions.numberOfWindowsToBe(2));
		Set<String> allWindows = driver.getWindowHandles();
		for (String windowHandle : allWindows) {
			if (!windowHandle.equals(originalWindow)) {
				driver.switchTo().window(windowHandle);
				break;
			}
		}
	}

}
