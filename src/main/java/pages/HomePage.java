package pages;

import java.time.LocalDateTime;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import base.BasePage;
import utils.ExcelUtil;

public class HomePage extends BasePage {

	public HomePage(WebDriver driver) {
		super(driver);

	}

	By from = By.xpath("//p[@data-testid='originId']");
	By sourceClearButton = By.xpath("//label[text()='From']//following-sibling::*[contains(@class,'close') or contains(@class,'clear') or name()='svg' or text()='×']");
	By sourceInput = By.xpath("//label[text()='From']//following-sibling::input");

	By sourceLocation(String source) {
		return By.xpath("//p[text()='" + source + "'] | //span[text()='" + source + "'] | //div[text()='" + source + "']");
	}

	By destinationInput = By.xpath("//label[text()='To']//following-sibling::input");

	By destinationLocation(String destination) {
		return By.xpath("//p[text()='" + destination + "'] | //span[text()='" + destination + "'] | //div[text()='" + destination + "'] | //*[@class and text()='" + destination + "']");
	}

	// Fallback: click first dropdown suggestion
	By firstDropdownOption = By.xpath("(//div[contains(@class,'city') or contains(@class,'airport') or contains(@class,'suggestion') or contains(@class,'option')]//p)[1]");

	By adultPassengers = By.xpath("//p[text()='Adults']/parent::div/following-sibling::div//button[text()='1']");
	By childPassengers = By.xpath("//p[text()='Children']/parent::div/following-sibling::div//button[text()='1']");

	By fromDate(String day) {
		return By.xpath("//div[@class='react-calendar__month-view__days']//abbr[text()='" + day
				+ "']/parent::button[not(@disabled)]"); 
			
		/*return By.xpath("//div[@class='react-calendar__month-view__days__day--weekend']//abbr[text()='" + day
				+ "']/parent::button[not(@disabled)]"); */
		
	}

	By doneButton = By.xpath("//button[text()='Done']");
	By searchButton = By.xpath("//button[text()='Search']");

	public void selectFromAndTo() throws InterruptedException {
		Thread.sleep(5000);
		clickElementWithJS(from, "Source Button", "Home Page");
		Thread.sleep(1000);
		// Clear and type source city using sendKeys (triggers autocomplete)
		WebElement srcInput = waitForClickable(sourceInput);
		srcInput.sendKeys(org.openqa.selenium.Keys.chord(org.openqa.selenium.Keys.CONTROL, "a"));
		srcInput.sendKeys(ExcelUtil.getCellValue(testData, "SourceCity"));
		Thread.sleep(1500);
		clickElement(sourceLocation(ExcelUtil.getCellValue(testData, "SourceLocation")), "Source Airport", "Home Page");
		Thread.sleep(1000);
		// Clear and type destination city using sendKeys (triggers autocomplete)
		WebElement destInput = waitForClickable(destinationInput);
		destInput.sendKeys(org.openqa.selenium.Keys.chord(org.openqa.selenium.Keys.CONTROL, "a"));
		destInput.sendKeys(ExcelUtil.getCellValue(testData, "DestinationCity"));
		Thread.sleep(1500);
		clickElement(destinationLocation(ExcelUtil.getCellValue(testData, "DestinationLocation")),
	
				"Destination Airport", "Home Page");
	}

	public void selectTheTravelDate() throws InterruptedException {
		Thread.sleep(1000);
		int journeyDay = LocalDateTime.now().plusDays(1).getDayOfMonth();
		clickElement(fromDate(String.valueOf(journeyDay)), "Journey day Calender", "Home Page");
	}

	public void selectPassengers() {
		clickElement(adultPassengers, "Adults passengers", "Home Pape");
		clickElement(childPassengers, "Child passengers", "Home Page");
		clickElement(doneButton, "Done Button", "Home Page");
	}

	public void searchForFlights() throws InterruptedException {
		Thread.sleep(500);
		clickElementWithJS(searchButton, "Search button", "Home Page");
	}

}
