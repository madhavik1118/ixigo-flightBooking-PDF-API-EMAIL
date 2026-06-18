package stepDefinitions;

import config.ConfigReader;
import drivers.DriverContext;
import pages.BookingPage;
import pages.FlightsPage;
import pages.GmailLoginPage;
import pages.HomePage;

public class BaseStep {
	protected HomePage homePage;
	protected FlightsPage flightsPage;
	protected GmailLoginPage gmailLoginPage;
	protected BookingPage bookingPage;

	public BaseStep() {
		ConfigReader config = new ConfigReader();
		homePage = new HomePage(DriverContext.getDriver());
		flightsPage = new FlightsPage(DriverContext.getDriver());
		gmailLoginPage = new GmailLoginPage(DriverContext.getDriver());
		bookingPage = new BookingPage(DriverContext.getDriver());
	}

}
