package utilities;

import io.restassured.response.Response;
import static io.restassured.RestAssured.given;
//import java.util.Map;

public class APIRequestHelper {

    private static final String BASE_URL = "https://petstore.swagger.io/v2/pet";

    /**
     * Sends a POST request to create a new pet.
     * @param requestBody JSON request body as a String.
     * @return Response from API.
     */
    public static Response createPet(String requestBody) {
        return given()
                .header("Content-Type", "application/json")
                .body(requestBody)
                .when()
                .post(BASE_URL);
    }

    /**
     * Sends a GET request to fetch pet details by ID.
     * @param petId ID of the pet.
     * @return Response from API.
     */
    public static Response getPetById(String petId) {
        String requestUrl = BASE_URL + "/" + petId;
        
        // ✅ Print the actual request being sent
        System.out.println("🔹 Sending GET Request to: " + requestUrl);

        Response response = given()
                .header("Accept", "application/json")
                .when()
                .get(requestUrl);

        // ✅ Debug response for further validation
        System.out.println("🔹 Response Status Code: " + response.getStatusCode());
        System.out.println("🔹 Response Body: " + response.getBody().asPrettyString());

        return response;
    }

    /**
     * Sends a GET request to fetch pets by status.
     * @param status The pet status (e.g., available, pending, sold).
     * @return Response from API.
     */
    public static Response getPetsByStatus(String status) {
        String requestUrl = BASE_URL + "/findByStatus";

        // ✅ Print the actual request being sent
        System.out.println("🔹 Sending GET Request to: " + requestUrl + "?status=" + status);

        Response response = given()
                .header("Accept", "application/json")
                .queryParam("status", status)
                .when()
                .get(requestUrl);

        // ✅ Debug response for further validation
        System.out.println("🔹 Response Status Code: " + response.getStatusCode());
        System.out.println("🔹 Response Body: " + response.getBody().asPrettyString());

        return response;
    }

    
    /**
     * Sends a PUT request to update a pet.
     * @param petId Pet ID.
     * @param updatedData Updated data in JSON format.
     * @return Response from API.
     */
    public static Response updatePet(String petId, String updatedData) {
        return given()
                .header("Content-Type", "application/json")
                .body(updatedData)
                .when()
                .put(BASE_URL + "/" + petId);
    }

    /**
     * Sends a DELETE request to remove a pet.
     * @param petId Pet ID to delete.
     * @return Response from API.
     */
    public static Response deletePet(String petId) {
        return given()
                .when()
                .delete(BASE_URL + "/" + petId);
    }
}
