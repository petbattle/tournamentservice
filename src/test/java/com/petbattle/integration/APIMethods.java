package com.petbattle.integration;

import io.restassured.response.Response;
import javax.ws.rs.core.Response.*;

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
                .delete("/api/tournament/{tid}/cancel", TID)
                .then()
                .statusCode(204);
    }

    public static void CallCreateTournamentInvalidAuth(String accessToken) {
        given()
                .contentType(JSON)
                .auth()
                .preemptive()
                .oauth2(accessToken)
                .when()
                .post("/api/tournament")
                .then()
                .statusCode(Status.UNAUTHORIZED.getStatusCode());
    }

    public static String CallCreateTournament(String accessToken) {
        Response x = given()
                .contentType(JSON)
                .auth()
                .preemptive()
                .oauth2(accessToken)
                .when()
                .post("/api/tournament")
                .then()
                .statusCode(Status.OK.getStatusCode())
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
                .delete("/api/tournament/{tid}", TID)
                .then()
                .statusCode(Status.OK.getStatusCode());
    }

    public static void CallStartTournament(String accessToken, String TID) {
        given()
                .contentType(JSON)
                .auth()
                .preemptive()
                .oauth2(accessToken)
                .when()
                .put("/api/tournament/{tid}", TID)
                .then()
                .statusCode(Status.OK.getStatusCode());
    }

    public static void CallGetTournamentState(String accessToken, String TID, String finished) {
        given()
                .contentType(JSON)
                .auth()
                .preemptive()
                .oauth2(accessToken)
                .when()
                .get("/api/tournament/{tid}", TID)
                .then()
                .statusCode(Status.OK.getStatusCode())
                .body("State", equalTo(finished));
    }

    public static void CallAddPet(String accessToken, String TID, String PID, int i) {
        given()
                .contentType(JSON)
                .auth()
                .preemptive()
                .oauth2(accessToken)
                .when()
                .post("/api/tournament/{tid}/add/{pid}", TID, PID)
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
                .post("/api/tournament/{tid}/vote/{pid}?dir={dir}", TID, PID, DIR)
                .then()
                .statusCode(i);
    }


    public static Response CallGetLeaderBoard(String accessToken) {
        return given()
                .contentType(JSON)
                .auth()
                .preemptive()
                .oauth2(accessToken)
                .when()
                .get("/api/tournament/leaderboard")
                .then()
                .statusCode(Status.OK.getStatusCode())
                .contentType(JSON)
                .extract()
                .response();
    }

    public static void CallGetMetricsAndVerify(String assertion) {
        given()
                .when()
                .get("/metrics")
                .then()
                .statusCode(Status.OK.getStatusCode())
                .contentType(TEXT)
                .log().all()
                .body(containsString(assertion));
    }
}
