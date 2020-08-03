package com.petbattle.integration;

import io.restassured.response.Response;

import static io.restassured.RestAssured.get;
import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;

public class APIMethods {

    public static void CallCancelTournament(String accessToken,String TID) {

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
        Response x =  given()
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

        return  x.getBody().jsonPath().getString("TournamentID");
    }

    public static void CallStopTournament(String accessToken,String TID) {
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

    public static void CallStartTournament(String TID) {
        given()
                .contentType(JSON)
                .when()
                .put("/tournament/{tid}", TID)
                .then()
                .statusCode(200);
    }

    public static void CallGetTournamentState(String TID, String finished) {
        given()
                .contentType(JSON)
                .when()
                .get("/tournament/{tid}", TID)
                .then()
                .statusCode(200)
                .body("State", equalTo(finished));
    }

    public static void CallAddPet(String TID, String PID, int i) {
        given()
                .contentType(JSON)
                .when()
                .post("/tournament/{tid}/add/{pid}", TID,PID)
                .then()
                .statusCode(i);
    }

    public static void CallVote4Pet(String TID, String PID, String DIR,int i) {
        given()
                .contentType(JSON)
                .when()
                .post("/tournament/{tid}/vote/{pid}?dir={dir}", TID,PID,DIR)
                .then()
                .statusCode(i);
    }


    public static Response CallGetLeaderBoard(String TID) {
        return get("/tournament/{tid}/leaderboard", TID)
                .then()
                .statusCode(200)
                .contentType(JSON)
                .extract()
                .response();
    }
}
