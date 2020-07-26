package com.petbattle.integration;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.get;
import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

@QuarkusTest
@DisplayName("API Test Cases")
public class ITPetBattleAPITest {

    @Test
    @DisplayName("Test Creation of a tournament and then cancel it")
    public void testNewTournamentEndpoint() {
        System.out.println("Create a tournament");
        Response response = given()
                .contentType(JSON)
                .when()
                .post("/tournament")
                .then()
                .statusCode(200)
                .contentType(JSON)
                .body(notNullValue())
                .extract()
                .response();

        String TID = response.getBody().jsonPath().getString("TournamentID");
        System.out.println("Cancel the created tournament");
        given()
                .contentType(JSON)
                .when()
                .delete("/tournament/{tid}/cancel", TID)
                .then()
                .statusCode(204);
    }

    @Test
    @DisplayName("Test Creation of a tournament, start it, stop it , get the state and finally cancel it")
    public void testCreateStartStopStatusTournamentEndpoints() {
        //Create Tournament
        Response response = given()
                .contentType(JSON)
                .when()
                .post("/tournament")
                .then()
                .statusCode(200)
                .contentType(JSON)
                .body(notNullValue())
                .extract()
                .response();

        String TID = response.getBody().jsonPath().getString("TournamentID");

        //Get Tournament state
        given()
                .contentType(JSON)
                .when()
                .get("/tournament/{tid}", TID)
                .then()
                .statusCode(200)
                .body("State", equalTo("NotStarted"));

        //Start the tournament
        given()
                .contentType(JSON)
                .when()
                .put("/tournament/{tid}", TID)
                .then()
                .statusCode(200);

        //Get Tournament state
        given()
                .contentType(JSON)
                .when()
                .get("/tournament/{tid}", TID)
                .then()
                .statusCode(200)
                .body("State", equalTo("Running"));

        //Stop the tournament
        given()
                .contentType(JSON)
                .when()
                .delete("/tournament/{tid}", TID)
                .then()
                .statusCode(200);

        //Get Tournament state
        given()
                .contentType(JSON)
                .when()
                .get("/tournament/{tid}", TID)
                .then()
                .statusCode(200)
                .body("State", equalTo("Finished"));

        //Cancel the tournament
        given()
                .contentType(JSON)
                .when()
                .delete("/tournament/{tid}/cancel", TID)
                .then()
                .statusCode(204);
    }

    @Test
    @DisplayName("Test Creation of a tournament, start it, stop it , get the state and finally cancel it")
    public void testCreateTournamentAddPetsEndpoints() {

        //Create a tournament
        Response response = given()
                .contentType(JSON)
                .when()
                .post("/tournament")
                .then()
                .statusCode(200)
                .contentType(JSON)
                .body(notNullValue())
                .extract()
                .response();

        String TID = response.getBody().jsonPath().getString("TournamentID");

        //Add a pet
        given()
                .contentType(JSON)
                .when()
                .post("/tournament/{tid}/add/12345", TID)
                .then()
                .statusCode(200);

        //Cancle the tournament
        given()
                .contentType(JSON)
                .when()
                .delete("/tournament/{tid}/cancel", TID)
                .then()
                .statusCode(204);
    }

    @Test
    @DisplayName("Test Creation of a tournament, start it, add a pet and finally cancel it")
    public void testCreateTournamentStartAddPetsEndpoints() {
        Response response = given()
                .contentType(JSON)
                .when()
                .post("/tournament")
                .then()
                .statusCode(200)
                .contentType(JSON)
                .body(notNullValue())
                .extract()
                .response();

        String TID = response.getBody().jsonPath().getString("TournamentID");

        //Start the tournament
        given()
                .contentType(JSON)
                .when()
                .put("/tournament/{tid}", TID)
                .then()
                .statusCode(200);

        //Get the tournament state
        given()
                .contentType(JSON)
                .when()
                .get("/tournament/{tid}", TID)
                .then()
                .statusCode(200)
                .body("State", equalTo("Running"));

        //We shouldn't be able to add a pet to a running tournament
        given()
                .contentType(JSON)
                .when()
                .post("/tournament/{tid}/add/12345", TID)
                .then()
                .statusCode(500);

        //Cancel the tournament
        given()
                .contentType(JSON)
                .when()
                .delete("/tournament/{tid}/cancel", TID)
                .then()
                .statusCode(204);
    }

    @Test
    @Order(5)
    public void testCreateStatusStartInvalidTournamentId() {
        Response response = given()
                .contentType(JSON)
                .when()
                .post("/tournament")
                .then()
                .statusCode(200)
                .contentType(JSON)
                .body(notNullValue())
                .extract()
                .response();

        String TID = response.getBody().jsonPath().getString("TournamentID");

        given()
                .contentType(JSON)
                .when()
                .get("/tournament/{tid}", "INVALIDTESTID")
                .then()
                .statusCode(500);

        given()
                .contentType(JSON)
                .when()
                .put("/tournament/{tid}", "INVALIDTESTID")
                .then()
                .statusCode(500);

        given()
                .contentType(JSON)
                .when()
                .delete("/tournament/{tid}", "INVALIDTESTID")
                .then()
                .statusCode(500);

        given()
                .contentType(JSON)
                .when()
                .post("/tournament/{tid}/add/12345", "INVALIDTESTID")
                .then()
                .statusCode(500);

        given()
                .contentType(JSON)
                .when()
                .delete("/tournament/{tid}/cancel", TID)
                .then()
                .statusCode(204);
    }

    @Test
    @Order(6)
    public void testCreateTournamentStartAddPetsVoteEndpoints() {
        Response response = given()
                .contentType(JSON)
                .when()
                .post("/tournament")
                .then()
                .statusCode(200)
                .contentType(JSON)
                .body(notNullValue())
                .extract()
                .response();

        String TID = response.getBody().jsonPath().getString("TournamentID");

        given()
                .contentType(JSON)
                .when()
                .post("/tournament/{tid}/add/1", TID)
                .then()
                .statusCode(200);

        given()
                .contentType(JSON)
                .when()
                .post("/tournament/{tid}/add/2", TID)
                .then()
                .statusCode(200);

        given()
                .contentType(JSON)
                .when()
                .post("/tournament/{tid}/add/3", TID)
                .then()
                .statusCode(200);

        given()
                .contentType(JSON)
                .when()
                .post("/tournament/{tid}/add/4", TID)
                .then()
                .statusCode(200);

        //Start the tournament
        given()
                .contentType(JSON)
                .when()
                .put("/tournament/{tid}", TID)
                .then()
                .statusCode(200);

        //Get the tournament status
        given()
                .contentType(JSON)
                .when()
                .get("/tournament/{tid}", TID)
                .then()
                .statusCode(200)
                .body("State", equalTo("Running"));

        //Upvote pet1
        given()
                .contentType(JSON)
                .when()
                .post("/tournament/{tid}/vote/1?dir=up", TID)
                .then()
                .statusCode(200);

        //Downvote pet 4
        given()
                .contentType(JSON)
                .when()
                .post("/tournament/{tid}/vote/4?dir=down", TID)
                .then()
                .statusCode(200);


        //Get votes for pet
        Response res0 = given()
                .contentType(JSON)
                .when()
                .get("/tournament/{tid}/votes/4", TID)
                .then()
                .statusCode(200)
                .extract()
                .response();

        System.out.println("BOOMMMMMMMM---->"+res0.asString());

        Response res = get("/tournament/{tid}/leaderboard", TID)
                .then()
                .statusCode(200)
                .contentType(JSON)
                .extract()
                .response();

        //Stop the test
        given()
                .contentType(JSON)
                .when()
                .delete("/tournament/{tid}", TID)
                .then()
                .statusCode(200);

        Response res2 = get("/tournament/{tid}/leaderboard", TID)
                .then()
                .statusCode(200)
                .contentType(JSON)
                .extract()
                .response();

        String json1 = res.asString();
        String json2 = res2.asString();

        assertThat(json1,equalTo(json2));

//        JsonPath jp = new JsonPath(json);

        //TODO : Add validation

        //Cancel the tournament
        given()
                .contentType(JSON)
                .when()
                .delete("/tournament/{tid}/cancel", TID)
                .then()
                .statusCode(204);
    }

    @Test
    @Order(6)
    public void testValidateVoteEndpoint() {
        Response response = given()
                .contentType(JSON)
                .when()
                .post("/tournament")
                .then()
                .statusCode(200)
                .contentType(JSON)
                .body(notNullValue())
                .extract()
                .response();

        String TID = response.getBody().jsonPath().getString("TournamentID");

        given()
                .contentType(JSON)
                .when()
                .post("/tournament/{tid}/add/1", TID)
                .then()
                .statusCode(200);

        //Start the tournament
        given()
                .contentType(JSON)
                .when()
                .put("/tournament/{tid}", TID)
                .then()
                .statusCode(200);

        given()
                .contentType(JSON)
                .when()
                .get("/tournament/{tid}", TID)
                .then()
                .statusCode(200)
                .body("State", equalTo("Running"));

        //Missing vote direction
        given()
                .contentType(JSON)
                .when()
                .post("/tournament/{tid}/vote/1", TID)
                .then()
                .statusCode(400);


        //Invalid vote direction
        given()
                .contentType(JSON)
                .when()
                .post("/tournament/{tid}/vote/1?dir=fail", TID)
                .then()
                .statusCode(400);

        //Correct voting
        given()
                .contentType(JSON)
                .when()
                .post("/tournament/{tid}/vote/1?dir=up", TID)
                .then()
                .statusCode(200);

        given()
                .contentType(JSON)
                .when()
                .delete("/tournament/{tid}/cancel", TID)
                .then()
                .statusCode(204);
    }
}
