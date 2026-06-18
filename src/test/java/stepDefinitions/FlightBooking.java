package stepDefinitions;

import org.openqa.selenium.WebDriver;

import drivers.DriverContext;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import utils.ExtentReporter;

public class FlightBooking extends BaseStep {

	@And("I click on book now button")
	public void ClickOnBookNowButton() {
		flightsPage.bookNow();
	}

	@And("I click on continue button")
	public void clickOnContinue() {
		flightsPage.continueBooking();
	}

	@And("I click on I don't want Free Cancellation button")
	public void clickNoCancellation() throws InterruptedException {
		bookingPage.clickNoCancellation();
	}

	@And("I enter Adult {int} details with title {string} firstName {string} lastName {string} dob {string} nationality {string}")
	public void enterAdultDetails(int index, String title, String firstName, String lastName, String dob, String nationality) throws InterruptedException {
		bookingPage.fillPassengerDetails("adult", index, title, firstName, lastName, dob, nationality);
	}

	@And("I enter Adult {int} details with title {string} firstName {string} lastName {string} Date of Birth {string} nationality {string}")
	public void enterAdultDetailsWithDateOfBirth(int index, String title, String firstName, String lastName, String dob, String nationality) throws InterruptedException {
		bookingPage.fillPassengerDetails("adult", index, title, firstName, lastName, dob, nationality);
	}

	@And("I enter Adult {int} details with title {string} firstName {string} lastName {string} Date of Birth {string}nationality {string}")
	public void enterAdultDetailsWithDateOfBirthNoSpace(int index, String title, String firstName, String lastName, String dob, String nationality) throws InterruptedException {
		bookingPage.fillPassengerDetails("adult", index, title, firstName, lastName, dob, nationality);
	}

	@And("I enter Adult {int} details with title {string} firstName {string} lastName {string} nationality {string}")
	public void enterAdultDetailsNoDob(int index, String title, String firstName, String lastName, String nationality) throws InterruptedException {
		bookingPage.fillPassengerDetails("adult", index, title, firstName, lastName, null, nationality);
	}

	@And("I enter Child {int} details with title {string} firstName {string} lastName {string} dob {string} nationality {string}")
	public void enterChildDetails(int childIndex, String title, String firstName, String lastName, String dob, String nationality) throws InterruptedException {
		bookingPage.fillPassengerDetails("child", childIndex, title, firstName, lastName, dob, nationality);
	}

	@And("I enter Child {int} details with title {string} firstName {string} lastName {string} nationality {string}")
	public void enterChildDetailsNoDob(int childIndex, String title, String firstName, String lastName, String nationality) throws InterruptedException {
		bookingPage.fillPassengerDetails("child", childIndex, title, firstName, lastName, null, nationality);
	}

	@And("I enter Child {int} details with title {string} firstName {string} lastName {string} Date of Birth {string} nationality {string}")
	public void enterChildDetailsWithDateOfBirth(int childIndex, String title, String firstName, String lastName, String dob, String nationality) throws InterruptedException {
		bookingPage.fillPassengerDetails("child", childIndex, title, firstName, lastName, dob, nationality);
	}

	@And("I enter contact details with countryCode {string} mobile {string} email {string} and click Continue")
	public void enterContactDetails(String countryCode, String mobile, String email) throws InterruptedException {
		bookingPage.fillContactDetails(countryCode, mobile, email);
	}

	@And("I click on Confirm button in Review Details page")
	public void clickConfirmButton() throws InterruptedException {
		bookingPage.clickConfirm();
	}

	@And("I click on No Thanks button in Free Cancellation page")
	public void clickNoThanks() throws InterruptedException {
		bookingPage.clickNoThanks();
	}

	@And("I click on Skip to Payment in Add-ons page")
	public void clickSkipToPayment() throws InterruptedException {
		bookingPage.clickSkipToPayment();
	}

	@And("I handle Fare increased popup if displayed")
	public void handleFareIncreasedPopup() throws InterruptedException {
		bookingPage.handleFareIncreasedPopup();
	}

	@Then("I should be on the Payment page")
	public void verifyPaymentPage() throws InterruptedException {
		bookingPage.verifyPaymentPage();
	}

	@And("I close the browser")
	public void closeBrowser() {
		// Don't quit here - let the @After hook handle browser cleanup
		// This avoids NoSuchSessionException when the hook tries to take a screenshot
	}
}
