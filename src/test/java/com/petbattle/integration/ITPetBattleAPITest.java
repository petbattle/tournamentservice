package com.petbattle.integration;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.Response;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.get;
import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@QuarkusTest
public class ITPetBattleAPITest {

    @Test
    @Order(1) 
    public void testNewTournamentEndpoint() {
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
                .delete("/tournament/{tid}/cancel", TID)
                .then()
                .statusCode(204);

        System.out.println(TID);
    }

    @Test
    @Order(2)
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
    @Order(3)
    public void testCreatTournamentAddPetsEndpoints() {
        //Create the tournament
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
        //Stop the tournament
        given()
                .contentType(JSON)
                .when()
                .delete("/tournament/{tid}", TID)
                .then()
                .statusCode(200);

        response = given()
                .contentType(JSON)
                .when()
                .post("/tournament")
                .then()
                .statusCode(200)
                .contentType(JSON)
                .body(notNullValue())
                .extract()
                .response();



        TID = response.getBody().jsonPath().getString("TournamentID");

        given()
                .contentType(JSON)
                .when()
                .post("/tournament/{tid}/add/12345", TID)
                .then()
                .statusCode(200);

        given()
                .contentType(JSON)
                .when()
                .delete("/tournament/{tid}/cancel", TID)
                .then()
                .statusCode(204);
    }

    @Test
    @Order(4)
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

        //We shouldn't be able to add a pet to a running tournament
        given()
                .contentType(JSON)
                .when()
                .post("/tournament/{tid}/add/12345", TID)
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

        given()
                .contentType(JSON)
                .when()
                .get("/tournament/{tid}", TID)
                .then()
                .statusCode(200)
                .body("State", equalTo("Running"));

        given()
                .contentType(JSON)
                .when()
                .post("/tournament/{tid}/vote/1?dir=up", TID)
                .then()
                .statusCode(200);

        given()
                .contentType(JSON)
                .when()
                .post("/tournament/{tid}/vote/4?dir=down", TID)
                .then()
                .statusCode(200);



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
    }
}
