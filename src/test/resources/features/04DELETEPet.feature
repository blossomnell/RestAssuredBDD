Feature: Delete Pet Information

Background:
  Given the user is on the Swagger Petstore page

@tag
Scenario: Delete the pet that was stored in POST
  When I send a DELETE request for the stored pet
  Then I should receive a "200 OK" response for deleting the pet

@tag
Scenario Outline: Attempt to delete pets using data from Excel
  Given I have DELETE test data from sheet "<sheetName>" at row <rowNum>
  When I send a DELETE request for the pet
  Then I should receive a "<statusCode>" response for deleting the pet

  Examples:
    | sheetName | rowNum | statusCode        |
    | POST      | 9      | 400 Bad Request  |
    | POST      | 10     | 400 Bad Request  |

