package base;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import utils.ExcelUtil;
import utils.ExtentReporter;

public class BasePage {
	public WebDriver driver;
	public WebDriverWait wait;
	protected List<Map<String, String>> testData;

	public BasePage(WebDriver _driver) {
		this.driver = _driver;
		this.wait = new WebDriverWait(_driver, Duration.ofSeconds(60));
//		testData = ExcelUtil.getSheetData("C:/Ixigo/ixigo-flightBooking/src/test/resources/testData/Ixigo.xlsx", "QA");
		testData=ExcelUtil.getData();
	}

	public WebElement waitForClickable(By locator) {
		return wait.until(ExpectedConditions.elementToBeClickable(locator));
	}

	public void clickElement(By locator, String elementName, String page) {
		waitForClickable(locator).click();
		ExtentReporter.extentTest.info(elementName + " is clicked on " + page + " page");
	}

	public void clickElementWithJS(By locator, String elementName, String page) {
		WebElement element = wait.until(ExpectedConditions.presenceOfElementLocated(locator));
		((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
		ExtentReporter.extentTest.info(elementName + " is clicked on " + page + " page");
	}

	public void enterText(By locator, String inputValue, String elementName, String page) {
		waitForClickable(locator).sendKeys(inputValue);
		ExtentReporter.extentTest.info(inputValue + " is entered in " + elementName + " in " + page + " page");
	}

	public void clearAndEnterText(By locator, String inputValue, String elementName, String page) {
		WebElement element = waitForClickable(locator);
		((org.openqa.selenium.JavascriptExecutor) driver).executeScript(
			"arguments[0].value=''; arguments[0].dispatchEvent(new Event('input', {bubbles:true}));", element);
		try { Thread.sleep(300); } catch (InterruptedException e) {}
		element.sendKeys(inputValue);
		ExtentReporter.extentTest.info(inputValue + " is entered in " + elementName + " in " + page + " page");
	}

	public WebElement webElement(By locator) {
		return driver.findElement(locator);
	}

	public void selectOptionByText(By locator, String optionText, String elementName, String page) {
		new Select(waitForClickable(locator)).selectByVisibleText(optionText);
		ExtentReporter.extentTest.info(optionText + " is selected in " + elementName + " in " + page + " page");
	}

	public boolean isElementDisplayed(By locator) {
		WebElement element = wait.until(ExpectedConditions.visibilityOf(webElement(locator)));
		return element.isDisplayed();
	}

}
