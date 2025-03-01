package stepDefinitions;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import org.testng.Assert;
import utilities.ExcelReader;
import utilities.LoggerLoad;
import static io.restassured.RestAssured.given;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import utilities.BaseClass;

public class POSTPetSteps {

    private String sheetName;
    private int rowNum;
    private Response response;
    private Map<String, String> petData = new HashMap<>();

    private static String validPetId;

    public static String getValidPetId() {
        return validPetId;
    }

    @Given("the user is on the Swagger Petstore page")
    public void the_user_is_on_the_swagger_petstore_page() {
        LoggerLoad.info("User is on the Swagger Petstore API page.");
    }

    @Given("I have test data from sheet {string} at row {int}")
    public void i_have_test_data_from_sheet_at_row(String sheet, Integer row) throws IOException {
        this.sheetName = sheet;
        this.rowNum = row;

        LoggerLoad.info("Using test data from Sheet: " + sheetName + ", Row: " + rowNum);
        String excelFilePath = BaseClass.getPropertyValue("excelFilePath");
        LoggerLoad.info("Excel File Path: " + excelFilePath);

        ExcelReader excelReader = new ExcelReader(excelFilePath);

        try {
            petData.put("id", excelReader.getCellData(sheet, row, 0).trim());
            petData.put("category_name", excelReader.getCellData(sheet, row, 2).trim());
            petData.put("name", excelReader.getCellData(sheet, row, 3).trim());
            petData.put("photoUrls", excelReader.getCellData(sheet, row, 4).trim());
            petData.put("tags_name", excelReader.getCellData(sheet, row, 6).trim());
            petData.put("status", excelReader.getCellData(sheet, row, 7).trim());
            petData.put("expectedStatus", excelReader.getCellData(sheet, row, 8).trim());

            LoggerLoad.info("Retrieved Test Data: " + petData);
        } catch (Exception e) {
            LoggerLoad.error("Error reading Excel data at sheet: " + sheet + ", row: " + row + " Exception: " + e.toString());
            throw new RuntimeException(e);
        }
    }

    @When("I create a pet with this data")
    @When("they send a POST request to add a new pet")
    public void send_post_request_to_create_pet() {
        sendPostRequest();
    }

    private void sendPostRequest() {
        LoggerLoad.info("Preparing to send POST request with pet data: " + petData);

        if (petData.isEmpty()) {
            LoggerLoad.warn("petData is empty. Executing default POST request for the first scenario.");
            String requestBody = "{ \"id\": 101, \"name\": \"Buddy\", \"status\": \"available\" }";

            response = given()
                    .header("Content-Type", "application/json; charset=UTF-8")
                    .body(requestBody)
                    .when()
                    .post("https://petstore.swagger.io/v2/pet");

            LoggerLoad.info("Response Status Code: " + response.getStatusCode());
            LoggerLoad.info("Response Body: " + response.getBody().asString());
            return;
        }

        StringBuilder jsonRequest = new StringBuilder("{");

        if (petData.containsKey("id") && !petData.get("id").isEmpty()) {
            jsonRequest.append("\"id\": ").append(petData.get("id")).append(", ");
        }

        jsonRequest.append("\"category\": { \"name\": \"").append(petData.get("category_name")).append("\" }, ");
        jsonRequest.append("\"name\": \"").append(petData.get("name")).append("\", ");
        jsonRequest.append("\"photoUrls\": [ \"").append(petData.get("photoUrls")).append("\" ], ");
        jsonRequest.append("\"tags\": [ { \"name\": \"").append(petData.get("tags_name")).append("\" } ], ");

        if (petData.containsKey("status") && !petData.get("status").isEmpty()) {
            jsonRequest.append("\"status\": \"").append(petData.get("status")).append("\" ");
        }

        jsonRequest.append("}");

        LoggerLoad.info("Final JSON Request: " + jsonRequest);

        response = given()
                .header("Content-Type", "application/json; charset=UTF-8")
                .body(jsonRequest.toString())
                .when()
                .post("https://petstore.swagger.io/v2/pet");

        LoggerLoad.info("Response Status Code: " + response.getStatusCode());
        LoggerLoad.info("Response Body: " + response.getBody().asString());

        if (response.getStatusCode() == 200 && validPetId == null) {
            validPetId = response.jsonPath().getString("id");
            LoggerLoad.info("Stored first valid pet ID for chaining: " + validPetId);
        } else {
            LoggerLoad.warn("Pet creation failed. No ID stored for chaining.");
        }
    }

    @Then("the pet should be successfully added")
    public void the_pet_should_be_successfully_added() {
        Assert.assertEquals(response.getStatusCode(), 200, "Pet creation failed!");
        LoggerLoad.info("Pet was successfully added!");
    }

    @Then("I should receive a response with status code {string}")
    public void i_should_receive_a_response_with_status_code(String expectedStatus) {
        int expectedCode = Integer.parseInt(expectedStatus.split(" ")[0]);

        LoggerLoad.info("Actual Response Status Code: " + response.getStatusCode());
        LoggerLoad.info("Actual Response Body: " + response.getBody().asString());

        response.then().statusCode(expectedCode);

        if (expectedCode == 400) {
            Assert.assertTrue(response.getBody().asString().contains("bad input"), "Expected 'bad input' message for 400 error.");
        }
    }
}
