package org.example;

import io.restassured.path.json.JsonPath;
import org.apache.http.HttpStatus;
import org.testng.annotations.Test;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

public class ApiTest {
    private static final String endpoint = "https://gorest.co.in/public/v2/users";
    ////Update the accessToken value with your own access token after logging in https://gorest.co.in/
    private static final String accessToken = "d9065c76bb0770dfae09fcaa681fb293c906b5b7c8cafde46085b93db3136856";
    //Update this id with the latest value from "Trying it Out" section in https://gorest.co.in/
    private static final int id = 1700791;

    @Test(priority = 1,
            description = "TC-001: Verify that the API returns the correct user details by ID - Positive Scenario")
    public void testGetUserByIdPositive(){
        // Step 1.1 Send a GET request to the endpoint with a valid user ID.
        given().queryParam("id", id)
                .when().get(endpoint + "/" + id)
                .then()
                .log().body()

        // Step 1.2 Verify that the response status code is 200 (OK).
                .statusCode(200)

        // Step 1.3 Validate that the returned user details match the expected values.
                .body("id", equalTo(id));
        }

    @Test(priority = 1,
            description = "TC-002: Verify that the API returns the correct user details by ID - Negative Scenario")
    public void testGetUserByIdNegative(){
        // Step 1.1 Send a GET request to the endpoint with an invalid user ID.
        var response = given().queryParam("id", 99999999)
                .when().get(endpoint + "/" + 99999999)
                .then()
                .log().body()

        // Step 1.2 Verify that the response status code is 404 (Not Found).
                .statusCode(404);

        // Step 1.3 Check that the response body indicates the user was not found.
        response.body("message", equalTo("Resource not found"));
    }

    @Test(priority = 2,
            description = "TC-003: Validate that a new user can be successfully created - Positive Scenario.")
    public void testCreateUserPositive() {
        String randomEmail = "user" + UUID.randomUUID() + "@example.com";

        // Step 1.1 Send a POST request to the endpoint with valid user data in the request body.
        String body = """
            {
            "name": "Test Testington",
            "email": "%s",
            "gender": "female",
            "status": "inactive"
            }
            """.formatted(randomEmail);

        // Step 1.2 Verify that the response status code is 201 (Created).
        var response = given()
                .header("Authorization", "Bearer " + accessToken)
                .header("Content-Type", "application/json")
                .body(body).when().post(endpoint).then();
        response.log().body().statusCode(201);

        // Step 2.1 Check the response body for the created user's ID.
        JsonPath jsonPath = response.extract().jsonPath();
        int id = jsonPath.getInt("id");

        // Step 2.2 Send a GET request using the obtained ID to ensure the user was created.
                given()
                .header("Authorization", "Bearer " + accessToken)
                .header("Content-Type", "application/json")
                .when().get(endpoint + "/" + id)
                .then()
                .log().body()
                .statusCode(HttpStatus.SC_OK)  // Check for a successful status code
                .body("id", equalTo(id));
    }

    @Test(priority = 2,
            description = "TC-004: Validate that a new user can be successfully created - Negative Scenario.")
    public void testCreateUserNegative() {
        // Step 1.1 Send a POST request to the endpoint with empty user data in the request body.
        String body = """
            {
            "name": " ",
            "email": " ",
            "gender": " ",
            "status": " "
            }
            """;

        // Step 1.2 Verify that the response status code is 422 Unprocessable Content.
        var response = given()
                .header("Authorization", "Bearer " + accessToken)
                .header("Content-Type", "application/json")
                .body(body).when().post(endpoint).then();
        response.log().body().statusCode(422);

    }

    @Test(priority = 3,
            description = "TC-005: Validate that user details can be successfully updated.")
    public void updateUser() {
        // Step 1 Update existing user from TC-001 with new information
        String body = """
                {
                "id": %d,
                "name": "Change Name",
                "email": "change-email@email.com",
                "gender": "female",
                "status": "inactive"
                }
                """.formatted(id);

        given()
                .header("Authorization", "Bearer " + accessToken)
                .header("Content-Type", "application/json")
                .body(body)
                .when()
                .put(endpoint + "/" + id)
                .then()
                .log().body()

        // Step 2: Verify that the response status code is 200 (OK)
                .statusCode(200)

        // Step 3: Add assertions to validate the updated user details
                .body("name", equalTo("Change Name"))
                .body("email", equalTo("change-email@email.com"))
                .body("gender", equalTo("female"))
                .body("status", equalTo("inactive"));
    }

    @Test(priority = 4,
            description = "TC-006: Validate that a user can be successfully deleted.")
    public void deleteUser() {
        // Step 1: Delete the user with the specified ID
        String body = """
                {
                "id": %d
                }
                """.formatted(id);

        given()
                .header("Authorization", "Bearer " + accessToken)
                .header("Content-Type", "application/json")
                .body(body)
                .when()
                .delete(endpoint + "/" + id)
                .then()
                .log().body()

        // Step 2: Verify that the response status code is 404 (Not Found)
                .statusCode(equalTo(204));
    }
}