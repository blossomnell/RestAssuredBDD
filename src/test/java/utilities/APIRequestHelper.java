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
        return given()
                .header("Accept", "application/json")
                .when()
                .get(BASE_URL + "/" + petId);
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
