package com.petbattle.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.petbattle.core.PetVote;
import com.petbattle.core.Tournament;
import io.quarkus.vertx.ConsumeEvent;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class TournamentStateService {

    private final Logger log = LoggerFactory.getLogger(TournamentStateService.class);
    private Tournament currentTournament;

    @ConsumeEvent("StartTournament")
    public Uni<Object> startTournament(String tournamentID) {
        log.info("startTournament {}", tournamentID);
        if (this.currentTournament == null) return Uni.createFrom().failure(new Exception("Tournament Not Created"));

        if (!tournamentID.equalsIgnoreCase(currentTournament.getTournamentID())) {
            log.warn("Incorrect Tournament id {} passed in request, active tournament {}", tournamentID, currentTournament.getTournamentID());
            return Uni.createFrom().failure(new Exception("Incorrect TournamentId"));
        } else {
            if (!this.currentTournament.isStarted())
                currentTournament.StartTournament();
        }
        return Uni.createFrom().item(new Object());
    }

    @ConsumeEvent("StopTournament")
    public Uni<Object> stopTournament(String tournamentID) {
        log.info("stopTournament {}", tournamentID);
        if (this.currentTournament == null) return Uni.createFrom().failure(new Exception("Tournament Not Created"));

        if (!tournamentID.equalsIgnoreCase(currentTournament.getTournamentID())) {
            log.warn("Incorrect Tournament id {} passed in request, active tournament {}", tournamentID, currentTournament.getTournamentID());
            return Uni.createFrom().failure(new Exception("Incorrect TournamentId"));
        } else {
            if (this.currentTournament.isStarted()) {
                currentTournament.StopTournament();


                //TODO Now save the tournament to the DB and null tournamentID
                currentTournament = null;
            }
        }
        return Uni.createFrom().item(new Object());
    }


    @ConsumeEvent("GetTournamentStatus")
    public Uni<JsonObject> statusTournament(String tournamentID) {
        log.info("statusTournament {}", tournamentID);
        if (this.currentTournament == null) return Uni.createFrom().failure(new Exception("Tournament Not Created"));

        if (!tournamentID.equalsIgnoreCase(currentTournament.getTournamentID())) {
            log.warn("Incorrect Tournament id {} passed in request, active tournament {}", tournamentID, currentTournament.getTournamentID());
            return Uni.createFrom().failure(new Exception("Incorrect TournamentId"));
        } else {
            JsonObject res = new JsonObject();
            res.put("State", currentTournament.getTournamentState());
            return Uni.createFrom().item(res);
        }
    }

    @ConsumeEvent("CreateTournament")
    public Uni<JsonObject> createTournament(String name) {
        log.info("createTournament");
        JsonObject res = new JsonObject();
        if (this.currentTournament == null) {
            currentTournament = new Tournament();
            res.put("TournamentID", currentTournament.getTournamentID());
        } else {
            res.put("TournamentID", this.currentTournament.getTournamentID());
        }
        return Uni.createFrom().item(res);
    }

    @ConsumeEvent("GetLeaderboard")
    public Uni<String> getTournamentLB(String tournamentID) throws JsonProcessingException {
        log.info("getTournamentLB {}", tournamentID);
        if (this.currentTournament == null) return Uni.createFrom().failure(new Exception("Tournament Not Created"));

        if (!tournamentID.equalsIgnoreCase(currentTournament.getTournamentID())) {
            log.warn("Incorrect Tournament id {} passed in request, active tournament {}", tournamentID, currentTournament.getTournamentID());
            return Uni.createFrom().failure(new Exception("Incorrect TournamentId"));
        }

        ObjectMapper mapper = new ObjectMapper();
        String jsonArray = mapper.writeValueAsString(this.currentTournament.getLeaderboard());

        return Uni.createFrom().item(jsonArray);
    }

    @ConsumeEvent("AddPetToTournament")
    public Uni<Object> addPetToTournament(JsonObject params) {
        String tourID = params.getString("tournamentId");
        String petID = params.getString("petId");
        log.info("addPetToTournament {}:{}", tourID, petID);

        if (this.currentTournament == null) return Uni.createFrom().failure(new Exception("Tournament Not Created"));

        if (!tourID.equalsIgnoreCase(currentTournament.getTournamentID())) {
            log.warn("Incorrect Tournament id {} passed in request, active tournament {}", tourID, currentTournament.getTournamentID());
            return Uni.createFrom().failure(new Exception("Incorrect TournamentId"));
        }

        if (currentTournament.isStarted()) {
            log.warn("Tournament started, addPetToTournament {}:{} not allowed", tourID, petID);
            return Uni.createFrom().failure(new Exception("TournamentId already active unable to add pet"));
        }
        currentTournament.addPet(petID);
        return Uni.createFrom().item(new Object());
    }

    @ConsumeEvent("ProcessPetVote")
    public Uni<Object> voteForPetInTournament(JsonObject params) {
        String tourID = params.getString("tournamentId");
        String petID = params.getString("petId");
        long timestamp = params.getLong("timestamp");
        String direction = params.getString("dir");

        if (this.currentTournament == null) return Uni.createFrom().failure(new Exception("Tournament Not Created"));

        log.info("voteForPetInTournament {}:{} dir {} @TS {}", tourID, petID, direction, timestamp);
        if (!tourID.equalsIgnoreCase(currentTournament.getTournamentID())) {
            log.warn("Incorrect Tournament id {} passed in request, active tournament {}", tourID, currentTournament.getTournamentID());
            return Uni.createFrom().failure(new Exception("Incorrect TournamentId"));
        }
        if (direction.equalsIgnoreCase("up"))
            this.currentTournament.upVotePet(petID);
        else
            this.currentTournament.downVotePet(petID);

        return Uni.createFrom().item(new Object());
    }
}