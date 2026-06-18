Feature: API User Management using RestAssured
  As a QA Engineer
  I want to automate API testing for user creation and retrieval
  So that I can validate the API responses and chain API calls

  @api
  Scenario: Create a user via POST and retrieve via GET using response ID
    Given I read the API test data from Excel for test case "CreateAndGetUser"
    When I send a POST request to create a user at "https://reqres.in/api/users"
    Then the POST response status code should be 201
    And the POST response body should contain the name and job from test data
    And I store the POST response ID and status code in Excel
    When I send a GET request to "https://reqres.in/api/users/id" using the created ID
    Then the GET response status code should be 200
    And the GET response body should contain valid user data using Matchers
    And I store the GET response data in Excel

  @api
  Scenario: Validate POST request with missing data returns appropriate status
    Given I read the API test data from Excel for test case "EmptyPostRequest"
    When I send a POST request with empty payload to "https://reqres.in/api/users"
    Then the response status code should be a valid HTTP code
    And I store the empty POST response in Excel for "EmptyPostRequest"

  @api
  Scenario: Validate GET request for non-existent user returns 404
    Given I read the API test data from Excel for test case "GetNonExistentUser"
    When I send a GET request to "https://reqres.in/api/users/" with invalid user ID from test data
    Then the GET response for invalid user should return 404
    And the response body should indicate resource not found
