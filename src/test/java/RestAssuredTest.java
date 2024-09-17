import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.Filter;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.json.JSONObject;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.hamcrest.Matchers.equalTo;




public class RestAssuredTest {

    @BeforeMethod
    public void authenticate() {
        // Create JSON body for the request
        JSONObject body = new JSONObject();
        body.put("username", "admin");
        body.put("password", "password123");

        // Perform POST request to the auth endpoint
        Response response = RestAssured
                .given()
                .header("Content-Type", "application/json")  // Add the Content-Type header
                .body(body.toString())  // Send JSON body as a string
                .post("https://restful-booker.herokuapp.com/auth");  // Auth endpoint

        // Validate the status code is 200
        response.then().statusCode(200);

        // Pretty print the response for debugging purposes
        response.prettyPrint();

        // Parse the response to extract the token
        JSONObject jsonResponse = new JSONObject(response.asString());
        authToken = jsonResponse.getString("token");

        // Print the extracted token
        System.out.println("Authentication Token: " + authToken);
    }

    @Test
    public void createBookingTest() {
        // Create request body for booking
        JSONObject body = new JSONObject();
        body.put("firstname", "Johny");
        body.put("lastname", "SilverHand");
        body.put("totalprice", 123);
        body.put("depositpaid", true);
        body.put("additionalneeds", "Breakfast");

        // Create the nested object for bookingdates
        JSONObject bookingDates = new JSONObject();
        bookingDates.put("checkin", "2025-01-01");
        bookingDates.put("checkout", "2025-01-01");

        // Add the nested object to the main body
        body.put("bookingdates", bookingDates);

        // Send POST request to create a booking with headers
        Response response = RestAssured
                .given()
                .header("Content-Type", "application/json")       // Content-Type header
                .header("Accept", "application/json")             // Accept header
                .header("Authorization", "Basic YWRtaW46cGFzc3dvcmQxMjM=")  // Basic Auth header
                //.cookie("token", "<token_value>")               // Alternatively, use Cookie header if needed
                .body(body.toString())                            // Request body
                .post("https://restful-booker.herokuapp.com/booking");

        // Extract the bookingid from the response
        int bookingId = response.jsonPath().getInt("bookingid");

        System.out.println("Booking ID: " + bookingId);

        // Perform a GET request to retrieve the booking details using the extracted bookingId
        Response getResponse = RestAssured
                .given()
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .get("https://restful-booker.herokuapp.com/booking/" + bookingId);

        // Validate the response status code and body content for the created booking
        getResponse.then()
                .statusCode(200)
                .body("firstname", equalTo("Johny"))
                .body("lastname", equalTo("SilverHand"))
                .body("totalprice", equalTo(123))
                .body("depositpaid", equalTo(true))
                .body("bookingdates.checkin", equalTo("2025-01-01"))
                .body("bookingdates.checkout", equalTo("2025-01-01"))
                .body("additionalneeds", equalTo("Breakfast"));
    }

    @Test
    public void getBookingsTest() {

        Response response = RestAssured.given().log().all().spec(spec).get("https://restful-booker.herokuapp.com/booking/");
        response.then()
                .statusCode(200)
                .body("bookingid", everyItem(greaterThan(0)));
    }

    @Test
    public void patchBookingTest() {
        // Define the booking ID to be patched (this can be dynamic if needed)
        int bookingId = 1;  // Replace this with the actual booking ID

        // Create the JSON body with the new totalprice
        JSONObject body = new JSONObject();
        body.put("totalprice", 777);

        // Perform PATCH request to update the totalprice
        Response patchResponse = RestAssured
                .given()
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Authorization", "Basic YWRtaW46cGFzc3dvcmQxMjM=") // Replace with correct auth if needed
                .body(body.toString())  // JSON body for PATCH request
                .patch("https://restful-booker.herokuapp.com/booking/" + bookingId);

        // Check the response status code and totalprice
        patchResponse.then()
                .statusCode(200)  // Ensure the status code is 200 OK
                .body("totalprice", equalTo(777))  // Check that totalprice was updated to 777
                .log().all();  // Log response details for debugging

        // Additional verification with a GET request to confirm the update
        Response getResponse = RestAssured
                .given()
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .get("https://restful-booker.herokuapp.com/booking/" + bookingId);

        // Validate that the totalprice has been updated in the GET response as well
        getResponse.then()
                .statusCode(200)
                .body("totalprice", equalTo(777))  // Check that totalprice is 777 after the update
                .log().all();  // Log response details for debugging
    }

    @Test
    public void putBookingTest() {
        // Define the booking ID to be updated (this can be dynamic if needed)
        int bookingId = 2;  // Replace with an actual booking ID

        // Create the JSON body with new firstname, lastname, and additionalneeds
        JSONObject body = new JSONObject();
        body.put("firstname", "Jim");
        body.put("lastname", "Beam");
        body.put("totalprice", 123); // Include required fields like totalprice and depositpaid as well
        body.put("depositpaid", true);

        // Create the nested object for bookingdates (this is mandatory in the PUT request)
        JSONObject bookingDates = new JSONObject();
        bookingDates.put("checkin", "2025-01-01");
        bookingDates.put("checkout", "2025-01-01");

        // Add bookingdates and additionalneeds to the main body
        body.put("bookingdates", bookingDates);
        body.put("additionalneeds", "Dinner");

        // Perform PUT request to update the booking
        Response putResponse = RestAssured
                .given()
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Authorization", "Basic YWRtaW46cGFzc3dvcmQxMjM=") // Replace with correct auth if needed
                .body(body.toString())  // JSON body for PUT request
                .put("https://restful-booker.herokuapp.com/booking/" + bookingId);  // Update booking ID

        // Check the response status code and verify changes
        putResponse.then()
                .statusCode(200)
                .body("firstname", equalTo("Jim"))
                .body("lastname", equalTo("Beam"))
                .body("additionalneeds", equalTo("Dinner"))
                .log().all();
    }
    @Test
    public void deleteBookingTest() {

        int bookingId = 1;

        // Perform DELETE request to delete the booking
        Response deleteResponse = RestAssured
                .given()
                .header("Content-Type", "application/json")
                .cookie("token", authtoken)  // Replace with the correct token value
                .delete("https://restful-booker.herokuapp.com/booking/" + bookingId);

        // Validate the response status code
        deleteResponse.then()
                .statusCode(201)
                .log().all();


    }
}







