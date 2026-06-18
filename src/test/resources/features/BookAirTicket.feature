Feature: Book flight ticket

  Background:
    Given I open the browser and open the ixigo app

  @ui
  Scenario: Book the flight ticket from Hyderabad to Banglore
    Given I enter the from and to location
    And I select the travel date
    And I select the Adults and child passengers
    When I click on search button
    Then I can see the list of flights available
    And I click on book now button
    And I click on continue button
    And I login with Gmail Account
    And I click on I don't want Free Cancellation button
    And I enter Adult 1 details with title "Mr" firstName "testFirstName" lastName "testLastName" Date of Birth "18/11/1990" nationality "India"
    And I enter Child 1 details with title "Mstr" firstName "testFirstName" lastName "testLastName" Date of Birth "18/11/2014" nationality "India"
    And I enter contact details with countryCode "India(+91)" mobile "9704277739" email "kmadhavi1811@gmail.com" and click Continue
    And I click on Confirm button in Review Details page
    And I click on No Thanks button in Free Cancellation page
    And I click on Skip to Payment in Add-ons page
    Then I should be on the Payment page

  @ui
  Scenario: Book the flight ticket from Chennai to Mumbai
    Given I enter the from and to location
    And I select the travel date
    And I select the Adults and child passengers
    When I click on search button
    Then I can see the list of flights available
    And I click on book now button
    And I click on continue button
    And I login with Gmail Account
