package stepDefinitions;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import org.testng.Assert;
import utilities.APIRequestHelper;
import utilities.BaseClass;
import utilities.ExcelReader;
import static io.restassured.RestAssured.given;
import java.io.IOException;
import java.util.*;

public class GETPetSteps {

    private Response response;
    private Map<String, String> petData = new HashMap<>();
    private List<String> expectedPetIds = new ArrayList<>();
    private List<String> expectedPetNames = new ArrayList<>();

    @Given("the user is on the Swagger Pet store page")
    public void the_user_is_on_the_swagger_pet_store_page() {
        System.out.println("🔹 User is on the Swagger Petstore API page.");
    }

    // ✅ **GET PET BY ID**
    @Given("I have a stored pet ID from the POST request")
    public void i_have_a_stored_pet_id_from_post_request() {
        String storedPetId = POSTPetSteps.getValidPetId();

        if (storedPetId != null && !storedPetId.isEmpty()) {
            System.out.println("✅ Using first stored pet ID: " + storedPetId);
            petData.put("id", storedPetId);
        } else {
            throw new RuntimeException("❌ No valid pet ID found from POST. Ensure a pet was created.");
        }
    }

    @When("I send a GET request to retrieve the pet by ID")
    public void i_send_a_get_request_to_retrieve_the_pet_by_id() {
        String petId = petData.get("id");

        System.out.println("🔹 Sending GET Request for Pet ID: " + petId);
        response = APIRequestHelper.getPetById(petId);
        
        System.out.println("🔹 Response Status Code: " + response.getStatusCode());
        System.out.println("🔹 Response Body: " + response.getBody().asPrettyString());
    }

    @Then("I should receive a {string} response for retrieving a pet by ID")
    public void i_should_receive_a_response_for_retrieving_a_pet_by_ID(String expectedStatus) {
        int expectedCode = Integer.parseInt(expectedStatus.split(" ")[0]);

        System.out.println("🔍 Validating Response...");
        response.then().statusCode(expectedCode);

        if (expectedCode == 200) {
            Assert.assertEquals(response.jsonPath().getString("id"), petData.get("id"), "❌ Mismatch in Pet ID!");
            Assert.assertNotNull(response.jsonPath().getString("name"), "❌ Pet Name is missing!");
            Assert.assertNotNull(response.jsonPath().getString("status"), "❌ Pet Status is missing!");
        } else if (expectedCode == 404) {
            System.out.println("⚠ Pet not found as expected (404). No further validation needed.");
        }
    }

    // ✅ **GET PETS BY STATUS**
    @Given("I have GET test data from sheet {string} at row {int}")
    public void i_have_GET_test_data_from_sheet_at_row(String sheetName, Integer rowNum) throws IOException {
        System.out.println("📌 Using test data from Sheet: " + sheetName + ", Row: " + rowNum);
        String excelFilePath = BaseClass.getPropertyValue("excelFilePath");

        ExcelReader excelReader = new ExcelReader(excelFilePath);

        try {
            expectedPetIds.clear();
            expectedPetNames.clear();

            // **Read only pets for the row specified in the feature file**
            String petId = excelReader.getCellData(sheetName, rowNum, 0).trim();
            String petName = excelReader.getCellData(sheetName, rowNum, 3).trim();
            String status = excelReader.getCellData(sheetName, rowNum, 7).trim();

            // **Only store ID & name for validation if not empty**
            if (!petId.isEmpty()) expectedPetIds.add(petId);
            if (!petName.isEmpty()) expectedPetNames.add(petName);

            petData.put("status", status);
            petData.put("expectedStatus", excelReader.getCellData(sheetName, rowNum, 9).trim());

            System.out.println("📋 Expected Pet ID: " + petId);
            System.out.println("📋 Expected Pet Name: " + petName);
            System.out.println("📋 Status: " + status);
        } catch (Exception e) {
            throw new RuntimeException("❌ Error reading Excel data at sheet: " + sheetName + ", row: " + rowNum, e);
        }
    }

    @When("I send a GET request to retrieve pets by status")
    public void i_send_a_GET_request_to_retrieve_pets_by_status() {
        String status = petData.get("status");

        if (status == null || status.isEmpty()) {
            throw new RuntimeException("❌ Status value is empty! Ensure valid test data is used.");
        }

        System.out.println("🔹 Sending GET Request for pets with status: " + status);

        response = given()
                .queryParam("status", status)
                .header("Content-Type", "application/json; charset=UTF-8")
                .when()
                .get("https://petstore.swagger.io/v2/pet/findByStatus");

        System.out.println("🔹 Response Status Code: " + response.getStatusCode());
    }

    @Then("I should receive a {string} response for retrieving pets by status")
    public void i_should_receive_a_response_for_retrieving_pets_by_status(String expectedStatus) {
        int expectedCode = Integer.parseInt(expectedStatus.split(" ")[0]);

        System.out.println("🔍 Validating Response...");
        response.then().statusCode(expectedCode);

        if (expectedCode == 200) {
            List<String> returnedPetIds = response.jsonPath().getList("id", String.class);
            List<String> returnedPetNames = response.jsonPath().getList("name", String.class);

            System.out.println("📌 Expected Pet ID: " + expectedPetIds);
            System.out.println("📌 Expected Pet Name: " + expectedPetNames);
            System.out.println("📌 Returned Pet IDs: " + returnedPetIds);
            System.out.println("📌 Returned Pet Names: " + returnedPetNames);

            // ✅ Validate only pets listed in the feature file
            for (String petId : expectedPetIds) {
                Assert.assertTrue(returnedPetIds.contains(petId), "❌ Expected Pet ID not found: " + petId);
            }

            for (String petName : expectedPetNames) {
                Assert.assertTrue(returnedPetNames.contains(petName), "❌ Expected Pet Name not found: " + petName);
            }

            System.out.println("✅ All expected pets are present in the response!");
        } else if (expectedCode == 404) {
            System.out.println("⚠ No pets found for status as expected (404).");
        }
    }
}
