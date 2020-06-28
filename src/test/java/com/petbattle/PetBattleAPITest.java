package com.petbattle;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;

import javax.ws.rs.Path;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static org.hamcrest.CoreMatchers.*;

@QuarkusTest
public class PetBattleAPITest {

    @Test
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
        System.out.println(TID);
    }

    @Test
    public void testCreateStartStopStatusTournamentEndpoints() {
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
                .get("/tournament/{tid}", TID)
                .then()
                .statusCode(200)
                .body("State", equalTo("NotStarted"));

        given()
                .contentType(JSON)
                .when()
                .put("/tournament/{tid}", TID)
                .then()
                .statusCode(204);

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
                .delete("/tournament/{tid}", TID)
                .then()
                .statusCode(204);

        given()
                .contentType(JSON)
                .when()
                .get("/tournament/{tid}", TID)
                .then()
                .statusCode(200)
                .body("State", equalTo("Finished"));

    }

    @Test
    public void testCreatTournamentAddPetsEndpoints() {
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
                .post("/tournament/{tid}/add/12345", TID)
                .then()
                .statusCode(204);
    }

    @Test
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
                .statusCode(204);

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
                .post("/tournament/{tid}/add/12345", TID)
                .then()
                .statusCode(204);
    }


}
