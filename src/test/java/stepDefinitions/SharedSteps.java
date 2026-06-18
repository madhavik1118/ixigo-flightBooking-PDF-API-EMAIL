package stepDefinitions;

import org.openqa.selenium.WebDriver;

import config.ConfigReader;
import drivers.DriverContext;
import io.cucumber.java.en.Given;

public class SharedSteps extends BaseStep {
	private WebDriver driver;

	public SharedSteps() {
		this.driver = DriverContext.getDriver();
	}

	@Given("I open the browser and open the ixigo app")
	public void NavigateToixigoApp() {
		driver.navigate().to(ConfigReader.getKeyValue("baseurl"));
	}
}
