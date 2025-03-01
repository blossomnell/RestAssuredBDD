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

    // Static variable to store the first valid pet ID for chaining (from Excel)
    private static String validPetId;

    public static String getValidPetId() {
        return validPetId;
    }

    @Given("the user is on the Swagger Petstore page")
    public void the_user_is_on_the_swagger_petstore_page() {
        System.out.println("🔹 User is on the Swagger Petstore API page.");
    }

    @Given("I have test data from sheet {string} at row {int}")
    public void i_have_test_data_from_sheet_at_row(String sheet, Integer row) throws IOException {
        this.sheetName = sheet;
        this.rowNum = row;

        System.out.println("📌 Using test data from Sheet: " + sheetName + ", Row: " + rowNum);
        String excelFilePath = BaseClass.getPropertyValue("excelFilePath");
        System.out.println("📂 Excel File Path: " + excelFilePath);

        ExcelReader excelReader = new ExcelReader(excelFilePath);

        try {
            petData.put("id", excelReader.getCellData(sheet, row, 0).trim());
            petData.put("category_name", excelReader.getCellData(sheet, row, 2).trim());
            petData.put("name", excelReader.getCellData(sheet, row, 3).trim());
            petData.put("photoUrls", excelReader.getCellData(sheet, row, 4).trim());
            petData.put("tags_name", excelReader.getCellData(sheet, row, 6).trim());
            petData.put("status", excelReader.getCellData(sheet, row, 7).trim());
            petData.put("expectedStatus", excelReader.getCellData(sheet, row, 8).trim());

            // Debugging log
            System.out.println("📋 Retrieved Test Data: " + petData);

        } catch (Exception e) {
            throw new RuntimeException("❌ Error reading Excel data at sheet: " + sheet + ", row: " + row, e);
        }
    }

    @When("I create a pet with this data")
    public void i_create_a_pet_with_this_data() {
        sendPostRequest();
    }

    @When("they send a POST request to add a new pet")
    public void they_send_a_post_request_to_add_a_new_pet() {
        // Directly sending a request with fixed pet data (for first scenario, but does NOT store the ID)
        String requestBody = "{ \"id\": 101, \"name\": \"Buddy\", \"status\": \"available\" }";

        response = given()
                .header("Content-Type", "application/json; charset=UTF-8")
                .body(requestBody)
                .when()
                .post("https://petstore.swagger.io/v2/pet");

        // Print response for debugging
        System.out.println("🔹 Response Status Code: " + response.getStatusCode());
        System.out.println("🔹 Response Body: " + response.getBody().asString());

        // ✅ This scenario does NOT store the pet ID for chaining
        System.out.println("🔹 First scenario executed, but pet ID is NOT stored.");
    }

    private void sendPostRequest() {
        // Debugging logs before sending request
        System.out.println("🔍 Debugging: Checking petData before sending POST request...");
        System.out.println("🔹 ID: " + petData.get("id"));
        System.out.println("🔹 Name: " + petData.get("name"));
        System.out.println("🔹 Status: " + petData.get("status"));
        System.out.println("🔹 Expected Status Code: " + petData.get("expectedStatus"));

        // Construct JSON request body dynamically
        StringBuilder jsonRequest = new StringBuilder("{");

        // Add ID
        if (!petData.get("id").isEmpty()) {
            jsonRequest.append("\"id\": ").append(petData.get("id")).append(", ");
        }

        jsonRequest.append("\"category\": { \"name\": \"").append(petData.get("category_name")).append("\" }, ");

        // Always include "name", even if it's empty
        jsonRequest.append("\"name\": \"").append(petData.get("name")).append("\", ");

        jsonRequest.append("\"photoUrls\": [ \"").append(petData.get("photoUrls")).append("\" ], ");
        jsonRequest.append("\"tags\": [ { \"name\": \"").append(petData.get("tags_name")).append("\" } ], ");

        // Handling Status based on API behavior
        if (petData.get("status").isEmpty()) {
            System.out.println("⚠ Warning: 'status' is empty. Removing it from request.");
        } else {
            jsonRequest.append("\"status\": \"").append(petData.get("status")).append("\" ");
        }

        jsonRequest.append("}");

        // Print final request body for debugging
        System.out.println("📨 Final JSON Request: " + jsonRequest);

        response = given()
                .header("Content-Type", "application/json; charset=UTF-8")
                .body(jsonRequest.toString())
                .when()
                .post("https://petstore.swagger.io/v2/pet");

        // Print response for debugging
        System.out.println("🔹 Response Status Code: " + response.getStatusCode());
        System.out.println("🔹 Response Body: " + response.getBody().asString());

        // ✅ Store first valid pet ID from Excel-driven tests only
        if (response.getStatusCode() == 200) {
            if (validPetId == null) {
                validPetId = response.jsonPath().getString("id");
                System.out.println("✅ Stored first valid pet ID from Excel for chaining: " + validPetId);
            }
        } else {
            System.out.println("❌ Pet creation failed. No ID stored for chaining.");
        }
    }

    @Then("the pet should be successfully added")
    public void the_pet_should_be_successfully_added() {
        Assert.assertEquals(response.getStatusCode(), 200, "❌ Pet creation failed!");
        System.out.println("✅ Pet was successfully added!");
    }

    @Then("I should receive a response with status code {string}")
    public void i_should_receive_a_response_with_status_code(String expectedStatus) {
        String[] statusParts = expectedStatus.split(" ", 2);
        int expectedCode = Integer.parseInt(statusParts[0]);

        // Print actual response for debugging
        System.out.println("🔹 Actual Response Status Code: " + response.getStatusCode());
        System.out.println("🔹 Actual Response Body: " + response.getBody().asString());

        response.then().statusCode(expectedCode);

        if (expectedCode == 400) {
            Assert.assertTrue(response.getBody().asString().contains("bad input"), "❌ Expected 'bad input' message for 400 error.");
        }
    }
}
