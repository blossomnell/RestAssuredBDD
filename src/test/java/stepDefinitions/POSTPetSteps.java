package stepDefinitions;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import org.testng.Assert;
import utilities.ExcelReader;
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

    // Static variable to store one valid pet ID for chaining
    private static String validPetId;

    public static String getValidPetId() {
        return validPetId;
    }

    @Given("the user is on the Swagger Petstore page")
    public void the_user_is_on_the_swagger_petstore_page() {
        System.out.println("User is on the Swagger Petstore API page.");
    }

    @When("they send a POST request to add a new pet")
    public void they_send_a_post_request_to_add_a_new_pet() {
        String requestBody = "{ \"id\": 101, \"name\": \"Buddy\", \"status\": \"available\" }";

        response = given()
                .header("Content-Type", "application/json; charset=UTF-8")
                .body(requestBody)
                .when()
                .post("https://petstore.swagger.io/v2/pet");

        // Print response for debugging
        System.out.println("Response Status Code: " + response.getStatusCode());
        System.out.println("Response Body: " + response.getBody().asString());
    }

    @Then("the pet should be successfully added")
    public void the_pet_should_be_successfully_added() {
        response.then().statusCode(200);
    }

    @Given("I have test data from sheet {string} at row {int}")
    public void i_have_test_data_from_sheet_at_row(String sheet, Integer row) throws IOException {
        this.sheetName = sheet;
        this.rowNum = row;

        System.out.println("Using test data from Sheet: " + sheetName + ", Row: " + rowNum);
        String excelFilePath = BaseClass.getPropertyValue("excelFilePath");
        System.out.println("Excel File Path: " + excelFilePath);

        ExcelReader excelReader = new ExcelReader(excelFilePath);

        try {
            petData.put("id", excelReader.getCellData(sheet, row, 0).trim());
            petData.put("category_name", excelReader.getCellData(sheet, row, 2).trim());
            petData.put("name", excelReader.getCellData(sheet, row, 3).trim());
            petData.put("photoUrls", excelReader.getCellData(sheet, row, 4).trim());
            petData.put("tags_name", excelReader.getCellData(sheet, row, 6).trim());
            petData.put("status", excelReader.getCellData(sheet, row, 7).trim());
            petData.put("expectedStatus", excelReader.getCellData(sheet, row, 8).trim());

            // Log missing required fields
            if (petData.get("id").isEmpty()) {
                System.out.println("⚠ Warning: 'id' field is empty for row " + row);
            }
            if (petData.get("name").isEmpty()) {
                System.out.println("⚠ Warning: 'name' field is empty for row " + row);
            }
            if (petData.get("status").isEmpty()) {
                System.out.println("⚠ Warning: 'status' field is empty for row " + row);
            }

        } catch (Exception e) {
            throw new RuntimeException("Error reading Excel data at sheet: " + sheet + ", row: " + row, e);
        }
    }

    @When("I create a pet with this data")
    public void i_create_a_pet_with_this_data() {
        // Construct JSON request body using a properly formatted String
        String jsonRequest = "{";

        // Add ID conditionally
        if (!petData.get("id").isEmpty()) {
            if (petData.get("id").matches("\\d+")) {
                jsonRequest += "\"id\": " + petData.get("id") + ", ";
            } else {
                jsonRequest += "\"id\": \"" + petData.get("id") + "\", ";
            }
        }

        jsonRequest += "\"category\": { \"name\": \"" + petData.get("category_name") + "\" }, ";

        // Always include "name", even if it's empty
        if (!petData.containsKey("name") || petData.get("name").isEmpty()) {
            System.out.println("⚠ 'name' is missing or empty, setting as empty string.");
            jsonRequest += "\"name\": \"\", "; // Send empty string instead of removing it
        } else {
            jsonRequest += "\"name\": \"" + petData.get("name") + "\", ";
        }

        jsonRequest += "\"photoUrls\": [ \"" + petData.get("photoUrls") + "\" ], ";
        jsonRequest += "\"tags\": [ { \"name\": \"" + petData.get("tags_name") + "\" } ], ";

        // Add "status" only if it's present
        if (!petData.get("status").isEmpty()) {
            jsonRequest += "\"status\": \"" + petData.get("status") + "\" ";
        }

        jsonRequest += "}";

        // Print final request body for debugging
        System.out.println("Final JSON Request: " + jsonRequest);

        response = given()
                .header("Content-Type", "application/json; charset=UTF-8")
                .body(jsonRequest)
                .when()
                .post("https://petstore.swagger.io/v2/pet");

        // Print response for debugging
        System.out.println("Response Status Code: " + response.getStatusCode());
        System.out.println("Response Body: " + response.getBody().asString());

        // **Store one valid ID for chaining (Only if it's a successful response)**
        if (response.getStatusCode() == 200) {
            validPetId = response.jsonPath().getString("id");
            System.out.println("✅ Stored valid pet ID for chaining: " + validPetId);
        }
    }

    @Then("I should receive a response with status code {string}")
    public void i_should_receive_a_response_with_status_code(String expectedStatus) {
        String[] statusParts = expectedStatus.split(" ", 2);
        int expectedCode = Integer.parseInt(statusParts[0]);
        String expectedMessage = statusParts.length > 1 ? statusParts[1] : "";

        // Print actual response for debugging
        System.out.println("Actual Response Status Code: " + response.getStatusCode());
        System.out.println("Actual Response Body: " + response.getBody().asString());

        response.then().statusCode(expectedCode);

        String actualMessage = response.getStatusLine();

        if (!expectedMessage.isEmpty()) {
            Assert.assertTrue(actualMessage.contains(expectedMessage),
                    "Expected message to contain: " + expectedMessage + ", but got: " + actualMessage);
        }
    }
}
