package com.petbattle.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.petbattle.core.PetVote;
import com.petbattle.core.Tournament;
import io.quarkus.vertx.ConsumeEvent;
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
    public void startTournament(String tournamentID) {
        log.info("startTournament {}",tournamentID);
        if (this.currentTournament == null) return;

        if (!tournamentID.equalsIgnoreCase(currentTournament.getTournamentID())){
            log.warn("Incorrect Tournament id {} passed in request, active tournament {}",tournamentID,currentTournament.getTournamentID());
        }else{
            if (!this.currentTournament.isStarted())
                currentTournament.StartTournament();
        }
    }

    @ConsumeEvent("StopTournament")
    public void stopTournament(String tournamentID) {
        log.info("stopTournament {}",tournamentID);
        if (this.currentTournament == null) return;

        if (!tournamentID.equalsIgnoreCase(currentTournament.getTournamentID())){
            log.warn("Incorrect Tournament id {} passed in request, active tournament {}",tournamentID,currentTournament.getTournamentID());
        }else{
            if (this.currentTournament.isStarted())
                currentTournament.StopTournament();
            // Now save the tournament to the DB and null tournamentID

        }
    }


    @ConsumeEvent("GetTournamentStatus")
    public JsonObject statusTournament(String tournamentID) {
        log.info("statusTournament {}",tournamentID);
        if (this.currentTournament == null) return null;

        if (!tournamentID.equalsIgnoreCase(currentTournament.getTournamentID())){
            log.warn("Incorrect Tournament id {} passed in request, active tournament {}",tournamentID,currentTournament.getTournamentID());
            return null;
        }else{
            JsonObject res = new JsonObject();
            return res.put("State",currentTournament.getTournamentState());
        }
    }

    @ConsumeEvent("CreateTournament")
    public JsonObject createTournament(String name) {
        log.info("createTournament");
        JsonObject res = new JsonObject();
        if (this.currentTournament == null) {
            currentTournament = new Tournament();
            res.put("TournamentID",currentTournament.getTournamentID());
        }else
        {
            res.put("TournamentID",this.currentTournament.getTournamentID());
        }
        return res;
    }

    @ConsumeEvent("GetLeaderboard")
    public String getTournamentLB(String tournamentID) throws JsonProcessingException {
        log.info("getTournamentLB {}",tournamentID);
        List<PetVote> res = new ArrayList<>();
        res.add(new PetVote("0"));
        res.add(new PetVote("2"));
        res.add(new PetVote("3"));
        res.add(new PetVote("4"));
        res.add(new PetVote("5"));

        ObjectMapper mapper = new ObjectMapper();
        String jsonArray = mapper.writeValueAsString(res);

        return jsonArray;
    }

    @ConsumeEvent("AddPetToTournament")
    public void addPetToTournament(JsonObject params) {
        log.info("addPetToTournament");
        if (this.currentTournament == null) return;

        String tourID = params.getString("tournamentId");
        String petID = params.getString("petId");
        if (currentTournament.isStarted()){
            log.warn("Tournament started, addPetToTournament {}:{} not allowed",tourID,petID);
            return;
        }
        log.info("addPetToTournament {}:{}",tourID,petID);
        if (!tourID.equalsIgnoreCase(currentTournament.getTournamentID())){
            log.warn("Incorrect Tournament id {} passed in request, active tournament {}",tourID,currentTournament.getTournamentID());
        }else{
            currentTournament.addPet(petID);
        }
    }

    @ConsumeEvent("ProcessPetVote")
    public void voteForPetInTournament(JsonObject params) {
        log.info("voteForPetInTournament");

        String tourID = params.getString("tournamentId");
        String petID = params.getString("petId");
        long timestamp= params.getLong("timestamp");
        String direction= params.getString("dir");

        if (this.currentTournament == null){
            log.warn("Tournament not started, voteForPetInTournament {}:{} not allowed",tourID,petID);
            return;
        }
        log.info("voteForPetInTournament {}:{} dir {} @TS {}",tourID,petID,direction,timestamp );
        if (!tourID.equalsIgnoreCase(currentTournament.getTournamentID())){
            log.warn("Incorrect Tournament id {} passed in request, active tournament {}",tourID,currentTournament.getTournamentID());
        }else{
            if (direction.equals("up"))
                this.currentTournament.upVotePet(petID);
            else
                this.currentTournament.downVotePet(petID);

        }
    }

}