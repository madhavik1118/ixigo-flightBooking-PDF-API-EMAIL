package pages;

import java.time.Duration;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import base.BasePage;

public class FlightsPage extends BasePage {

	public FlightsPage(WebDriver driver) {
		super(driver);
	}

	// Use contains to match variations like "Book", "Book Now"
	By bookButton = By.xpath("//button[contains(text(),'Book')]");
	By continueButton = By.xpath("//button[contains(text(),'Continue')]");
	// Overlay/modal backdrop that may block clicks
	By overlayBackdrop = By.cssSelector("div.fixed.bg-black.bg-opacity-50");

	public void flightShouldDisplay() {
		// Wait for flight results to load
		wait.until(ExpectedConditions.visibilityOfElementLocated(bookButton));
	}

	public void bookNow() {
		wait.until(ExpectedConditions.visibilityOfElementLocated(bookButton));
		dismissOverlayIfPresent();
		// Use JS click to bypass any remaining overlay issues
		clickElementWithJS(bookButton, "Book Now button", "Flights Page");
	}

	public void continueBooking() {
		wait.until(ExpectedConditions.visibilityOfElementLocated(continueButton));
		dismissOverlayIfPresent();
		// Use JS click to bypass any remaining overlay issues
		clickElementWithJS(continueButton, "Continue Button", "Flights Page");
	}

	private void dismissOverlayIfPresent() {
		try {
			WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(3));
			WebElement overlay = shortWait.until(ExpectedConditions.presenceOfElementLocated(overlayBackdrop));
			if (overlay.isDisplayed()) {
				// Try clicking the overlay to dismiss it
				overlay.click();
				// Wait for it to disappear
				new WebDriverWait(driver, Duration.ofSeconds(5))
						.until(ExpectedConditions.invisibilityOfElementLocated(overlayBackdrop));
			}
		} catch (Exception e) {
			// No overlay present, continue normally
		}
	}
}
