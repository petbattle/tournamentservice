package com.petbattle.integration;

import com.petbattle.containers.InfinispanTestContainer;
import com.petbattle.containers.KeycloakTestContainer;
import com.petbattle.containers.MongoTestContainer;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.Response;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.petbattle.integration.APIMethods.*;
import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static io.restassured.http.ContentType.URLENC;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

@QuarkusTest
@DisplayName("API Test Cases")
@QuarkusTestResource(MongoTestContainer.class)
@QuarkusTestResource(InfinispanTestContainer.class)
@QuarkusTestResource(KeycloakTestContainer.class)
public class ITPetBattleAPITest {

    @ConfigProperty(name="quarkus.pbclient.test.secret")
    String clientSecret;

    @ConfigProperty(name="quarkus.oidc.auth-server-url")
    String keycloakHost;

    private String adminToken;
    private String playerToken;

    @BeforeEach
    private void init(){
        String payload = "username=player1&password=player1pwd&grant_type=password";

        Response x =  given()
                .log().all()
                .contentType(URLENC)
                .auth()
                .preemptive()
                .basic("pbclient",clientSecret)
                .when()
                .body(payload)
                .post(keycloakHost+"/protocol/openid-connect/token")
                .then()
                .log().all()
                .statusCode(200)
                .contentType(JSON)
                .body(notNullValue())
                .extract()
                .response();

        System.out.println(x.getBody().prettyPrint());
    }

    @Test
    @DisplayName("Test Creation of a tournament and then cancel it")
    public void testNewTournamentEndpoint() {
        String TID = CallCreateTournament(adminToken);
        CallCancelTournament(adminToken,TID);
    }

    @Test
    @DisplayName("Test Creation of a tournament, start it, stop it , get the state and finally cancel it")
    public void testCreateStartStopStatusTournamentEndpoints() {
        String TID = CallCreateTournament(adminToken);
        CallGetTournamentState(TID, "NotStarted");
        CallStartTournament(TID);
        CallGetTournamentState(TID, "Running");
        CallStopTournament(adminToken,TID);
        CallGetTournamentState(TID, "Finished");
        CallCancelTournament(adminToken,TID);
    }


    @Test
    @DisplayName("Test Creation of a tournament, start it, stop it , get the state and finally cancel it")
    public void testCreateTournamentAddPetsEndpoints() {
        String TID =  CallCreateTournament(adminToken);
        CallAddPet(TID, "12345", 200);
        CallCancelTournament(adminToken,TID);
    }

    @Test
    @DisplayName("Test Creation of a tournament, start it, add a pet and finally cancel it")
    public void testCreateTournamentStartAddPetsEndpoints() {
        String TID =  CallCreateTournament(adminToken);
        CallStartTournament(TID);
        CallGetTournamentState(TID, "Running");
        //We shouldn't be able to add a pet to a running tournament
        CallAddPet(TID, "12345", 500);
        CallCancelTournament(adminToken,TID);
    }

    @Test
    public void testCreateStatusStartInvalidTournamentId() {
        String TID =  CallCreateTournament(adminToken);

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

        CallAddPet("INVALIDTESTID", "12345", 500);
        CallCancelTournament(adminToken,TID);
    }

    @Test
    public void testCreateTournamentStartAddPetsVoteEndpoints() {
        String TID =  CallCreateTournament(adminToken);
        CallAddPet(TID, "1", 200);
        CallAddPet(TID, "2", 200);
        CallAddPet(TID, "3", 200);
        CallAddPet(TID, "4", 200);
        CallStartTournament(TID);
        CallGetTournamentState(TID, "Running");
        CallVote4Pet(TID,"1","up", 200);
        CallVote4Pet(TID, "4","down", 200);

        Response res = CallGetLeaderBoard(TID);

        //Stop the test
        CallStopTournament(adminToken,TID);

        Response res2 = CallGetLeaderBoard(TID);

        String json1 = res.asString();
        String json2 = res2.asString();

        assertThat(json1,equalTo(json2));

        //Cancel the tournament
        CallCancelTournament(adminToken,TID);
    }

    @Test
    public void testValidateVoteEndpoint() {
        String TID =  CallCreateTournament(adminToken);
        CallAddPet(TID, "1", 200);
        CallStartTournament(TID);
        CallGetTournamentState(TID, "Running");
        CallVote4Pet(TID, "1", "",400);
        CallVote4Pet(TID, "1","fail", 400);
        CallVote4Pet(TID, "1","up", 200);
        CallCancelTournament(adminToken,TID);
    }

    @Test
    public void testLeaderboardEndpoints() {
        String TID =  CallCreateTournament(adminToken);
        CallAddPet(TID, "1", 200);
        CallAddPet(TID, "2", 200);
        CallAddPet(TID, "3", 200);
        CallAddPet(TID, "4", 200);
        CallStartTournament(TID);
        CallGetTournamentState(TID, "Running");
        CallVote4Pet(TID,"1","up", 200);
        CallVote4Pet(TID,"4","up", 200);
        CallVote4Pet(TID,"4","up", 200);
        CallVote4Pet(TID,"4","up", 200);
        CallVote4Pet(TID,"2","up", 200);
        CallVote4Pet(TID,"2","up", 200);
        CallVote4Pet(TID,"4","up", 200);
        CallVote4Pet(TID,"3","up", 200);
        CallVote4Pet(TID,"3","up", 200);
        CallVote4Pet(TID,"3","up", 200);

        Response res1 = CallGetLeaderBoard(TID);

        List<String> vote1 = res1.jsonPath()
                .getList("petId");

        assertThat("Pet 4 should be highest rated",vote1.get(0).equals("4"));
        assertThat("Pet 3 should be next rated",vote1.get(1).equals("3"));
        assertThat("Pet 2 should be next rated",vote1.get(2).equals("2"));
        assertThat("Pet 1 should be next rated",vote1.get(3).equals("1"));


        CallVote4Pet(TID,"1","up", 200);
        CallVote4Pet(TID,"4","down", 200);
        CallVote4Pet(TID,"4","down", 200);
        CallVote4Pet(TID,"4","down", 200);
        CallVote4Pet(TID,"2","up", 200);
        CallVote4Pet(TID,"2","up", 200);
        CallVote4Pet(TID,"3","down", 200);
        CallVote4Pet(TID,"3","up", 200);
        CallVote4Pet(TID,"3","up", 200);
        CallVote4Pet(TID,"3","up", 200);

        Response res2 = CallGetLeaderBoard(TID);

        List<String> vote2 = res2.jsonPath()
                .getList("petId");

        System.out.println(res2.getBody().prettyPrint());

        assertThat("Pet 3 should be highest rated",vote2.get(0).equals("3"));
        assertThat("Pet 2 should be next rated",vote2.get(1).equals("2"));
        assertThat("Pet 1 should be next rated",vote2.get(2).equals("1"));
        assertThat("Pet 4 should be next rated",vote2.get(3).equals("4"));

        //Cancel the tournament
        CallCancelTournament(adminToken,TID);
    }
}
