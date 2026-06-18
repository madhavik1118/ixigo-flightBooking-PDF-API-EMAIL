package stepDefinitions;

import io.cucumber.java.en.And;

public class GmailAccountSteps extends BaseStep {

	public GmailAccountSteps() {
		
	}
	@And("I login with Gmail Account")
	public void loginToGmailAccount() throws InterruptedException {
		gmailLoginPage.loginWithGmailAccount();
	}
}
