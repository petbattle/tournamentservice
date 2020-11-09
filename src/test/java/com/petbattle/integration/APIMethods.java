package com.petbattle.integration;

import io.restassured.response.Response;

import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static io.restassured.http.ContentType.TEXT;
import static org.hamcrest.CoreMatchers.*;

public class APIMethods {

    public static void CallCancelTournament(String accessToken, String TID) {

        given()
                .auth()
                .preemptive()
                .oauth2(accessToken)
                .contentType(JSON)
                .when()
                .delete("/tournament/{tid}/cancel", TID)
                .then()
                .statusCode(204);
    }

    public static String CallCreateTournament(String accessToken) {
        Response x = given()
                .contentType(JSON)
                .auth()
                .preemptive()
                .oauth2(accessToken)
                .when()
                .post("/tournament")
                .then()
                .statusCode(200)
                .contentType(JSON)
                .body(notNullValue())
                .extract()
                .response();

        return x.getBody().jsonPath().getString("TournamentID");
    }

    public static void CallStopTournament(String accessToken, String TID) {
        given()
                .contentType(JSON)
                .auth()
                .preemptive()
                .oauth2(accessToken)
                .when()
                .delete("/tournament/{tid}", TID)
                .then()
                .statusCode(200);
    }

    public static void CallStartTournament(String accessToken, String TID) {
        given()
                .contentType(JSON)
                .auth()
                .preemptive()
                .oauth2(accessToken)
                .when()
                .put("/tournament/{tid}", TID)
                .then()
                .statusCode(200);
    }

    public static void CallGetTournamentState(String accessToken, String TID, String finished) {
        given()
                .contentType(JSON)
                .auth()
                .preemptive()
                .oauth2(accessToken)
                .when()
                .get("/tournament/{tid}", TID)
                .then()
                .statusCode(200)
                .body("State", equalTo(finished));
    }

    public static void CallAddPet(String accessToken, String TID, String PID, int i) {
        given()
                .contentType(JSON)
                .auth()
                .preemptive()
                .oauth2(accessToken)
                .when()
                .post("/tournament/{tid}/add/{pid}", TID, PID)
                .then()
                .statusCode(i);
    }

    public static void CallVote4Pet(String accessToken, String TID, String PID, String DIR, int i) {
        given()
                .contentType(JSON)
                .auth()
                .preemptive()
                .oauth2(accessToken)
                .when()
                .post("/tournament/{tid}/vote/{pid}?dir={dir}", TID, PID, DIR)
                .then()
                .statusCode(i);
    }


    public static Response CallGetLeaderBoard(String accessToken, String TID) {
        return given()
                .contentType(JSON)
                .auth()
                .preemptive()
                .oauth2(accessToken)
                .when()
                .get("/tournament/{tid}/leaderboard", TID)
                .then()
                .statusCode(200)
                .contentType(JSON)
                .extract()
                .response();
    }

    public static void CallGetMetricsAndVerify(String assertion) {
        given()
                .when()
                .get("/metrics")
                .then()
                .statusCode(200)
                .contentType(TEXT)
                .log().all()
                .body(containsString(assertion));
    }
}
