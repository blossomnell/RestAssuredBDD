Feature: Add a New Pet

  @tag
  Scenario: User is on Swagger page and sends a POST request
    Given the user is on the Swagger Petstore page
    When they send a POST request to add a new pet
    Then the pet should be successfully added

  @tag
  Scenario Outline: Create a pet using data from Excel
    Given I have test data from sheet "<sheetName>" at row <rowNum>
    When I create a pet with this data
    Then I should receive a response with status code "<statusCode>"


    Examples: 
      | sheetName | rowNum | statusCode        |
      | POST      |      2 | 200 OK            |
      | POST      |      3 | 200 OK            |
      | POST      |      4 | 200 OK            |
      | POST      |      5 | 200 OK            |
      | POST      |      6 | 200 OK            |
      | POST      |      7 | 200 OK            |
      | POST      |      8 | 200 OK            |
      | POST      |      9 | 400 Bad Request   |
      | POST      |     10 | 500 Server Error  |
