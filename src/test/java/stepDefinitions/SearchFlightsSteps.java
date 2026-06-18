package stepDefinitions;

import drivers.DriverContext;
import io.cucumber.java.en.*;
import pages.HomePage;

public class SearchFlightsSteps extends BaseStep {

	@Given("I enter the from and to location")
	public void enterFromAndToLocation() throws InterruptedException {
		homePage.selectFromAndTo();
	}

	@And("I select the travel date")
	public void selectTravelDate() throws InterruptedException {
		homePage.selectTheTravelDate();
	}

	@And("I select the Adults and child passengers")
	public void selectThePassengers() {
		homePage.selectPassengers();
	}

	@When("I click on search button")
	public void clickOnSearchButton() throws InterruptedException {
		homePage.searchForFlights();
	}

	@Then("I can see the list of flights available")
	public void verifyTheFlightsPage() {
		flightsPage.flightShouldDisplay();
	}
}
