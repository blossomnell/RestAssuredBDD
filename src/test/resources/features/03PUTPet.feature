Feature: Update Pet Information

  Background:
    Given the user is on the Swagger Petstore page

  @tag
  Scenario: Update the pet that was stored in POST
    Given I have a stored pet ID from the POST request
    When I send a PUT request to update the pet's name to "ForeverBuddy" and status to "sold"
    Then I should receive a "200 OK" response for updating the pet

  @tag
  Scenario Outline: Update pets using data from Excel
    Given I have PUT test data from sheet "<sheetName>" at row <rowNum>
    When I send a PUT request to update the pet details
    Then I should receive a "<statusCode>" response for updating the pet

    Examples:
      | sheetName | rowNum | statusCode  |
      | POST      | 3      | 200 OK      |
      | POST      | 5      | 200 OK      |
      | POST      | 6      | 200 OK      |
      | POST      | 8      | 200 OK      |
