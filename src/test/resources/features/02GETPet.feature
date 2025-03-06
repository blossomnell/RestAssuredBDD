Feature: Retrieve Pet Information

  Background:
    Given the user is on the Swagger Pet store page

 @get
  Scenario: Retrieve the pet that was created
    Given I have a stored pet ID from the POST request
    When I send a GET request to retrieve the pet by ID
    Then I should receive a "200 OK" response for retrieving a pet by ID

  @tag
  Scenario Outline: Retrieve pets by status
    Given I have GET test data from sheet "<sheetName>" at row <rowNum>
    When I send a GET request to retrieve pets by status
    Then I should receive a "<statusCode>" response for retrieving pets by status

    Examples:
      | sheetName | rowNum | statusCode  |
      | POST      | 4      | 200 OK      |
      | POST      | 5      | 200 OK      |
      | POST      | 6      | 200 OK      |
      | POST      | 7      | 200 OK      |
