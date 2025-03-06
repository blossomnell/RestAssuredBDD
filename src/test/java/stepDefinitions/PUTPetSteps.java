package stepDefinitions;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import utilities.BaseClass;
import utilities.ExcelReader;
import utilities.LoggerLoad;
import static io.restassured.RestAssured.given;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class PUTPetSteps {

    private Response response;
    private String baseUrl = "https://petstore.swagger.io/v2/pet";
    private int storedPetId;
    private Map<String, String> petData = new HashMap<>();

    @When("I send a PUT request to update the pet's name to {string} and status to {string}")
    public void i_send_a_put_request_to_update_the_pet_name_and_status(String updatedName, String updatedStatus) {
        storedPetId = Integer.parseInt(POSTPetSteps.getValidPetId()); // Fetch pet ID stored in POST
        LoggerLoad.info("Using stored pet ID for PUT request: " + storedPetId);

        petData.put("id", String.valueOf(storedPetId));
        petData.put("name", updatedName);
        petData.put("status", updatedStatus);

        String requestBody = "{" +
                "\"id\": " + petData.get("id") + ", " +
                "\"name\": \"" + petData.get("name") + "\", " +
                "\"status\": \"" + petData.get("status") + "\"" +
                "}";

        response = given()
                .header("Content-Type", "application/json")
                .body(requestBody)
                .when()
                .put(baseUrl);

        LoggerLoad.info("PUT Request sent for Pet ID: " + storedPetId);
        LoggerLoad.info("Response Status Code: " + response.getStatusCode());
        LoggerLoad.info("Response Body: " + response.getBody().asString());
    }

    @Given("I have PUT test data from sheet {string} at row {int}")
    public void i_have_put_test_data_from_sheet_at_row(String sheetName, int rowNum) {
        try {
            String excelFilePath = BaseClass.getPropertyValue("excelFilePath"); // Get Excel path
            ExcelReader reader = new ExcelReader(excelFilePath); // Pass file path to ExcelReader

            // Fetch pet details from Excel
            storedPetId = Integer.parseInt(reader.getCellData(sheetName, rowNum, 0));
            petData.put("name", reader.getCellData(sheetName, rowNum, 3) + "_Updated");
            petData.put("status", reader.getCellData(sheetName, rowNum, 7));

            LoggerLoad.info("Fetched test data from Excel - Pet ID: " + storedPetId + ", New Name: " + petData.get("name") + ", New Status: " + petData.get("status"));
        } catch (IOException e) {
            LoggerLoad.error("Error reading Excel file: " + e.getMessage());
            throw new RuntimeException(e); // Failing the test if Excel reading fails
        }
    }

    @When("I send a PUT request to update the pet details")
    public void i_send_a_put_request_to_update_the_pet_details() {
        String requestBody = "{" +
                "\"id\": " + storedPetId + ", " +
                "\"name\": \"" + petData.get("name") + "\", " +
                "\"status\": \"" + petData.get("status") + "\"" +
                "}";

        response = given()
                .header("Content-Type", "application/json")
                .body(requestBody)
                .when()
                .put(baseUrl);

        LoggerLoad.info("PUT Request sent for Pet ID: " + storedPetId);
        LoggerLoad.info("Response Status Code: " + response.getStatusCode());
        LoggerLoad.info("Response Body: " + response.getBody().asString());
    }

    @Then("I should receive a {string} response for updating the pet")
    public void i_should_receive_a_response_for_updating_the_pet(String expectedStatus) {
        int expectedCode = Integer.parseInt(expectedStatus.split(" ")[0]);
        response.then().statusCode(expectedCode);
        LoggerLoad.info("Validated PUT response with expected status: " + expectedStatus);
    }
}
