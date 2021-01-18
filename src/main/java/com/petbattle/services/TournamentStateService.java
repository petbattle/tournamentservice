package com.petbattle.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.petbattle.core.PetVote;
import com.petbattle.core.Tournament;
import com.petbattle.exceptions.TournamentException;
import com.petbattle.repository.TournamentRepository;
import com.petbattle.repository.TournamentTemporalRepository;
import io.quarkus.infinispan.client.Remote;
import io.quarkus.vertx.ConsumeEvent;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import org.bson.types.ObjectId;
import org.infinispan.client.hotrod.RemoteCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@ApplicationScoped
public class TournamentStateService {
    private static final String ACTIVE_TOURNAMENT_KEY = "PetBattleActiveTournamentKey";

    @Inject
    TournamentRepository tournamentRepository;

    @Inject
    TournamentTemporalRepository TourTemporalStore;

    @Inject
    @Remote("ActiveTournament")
    RemoteCache<String, String> activeTournament;

    private Tournament currentTournament;
    private final Logger log = LoggerFactory.getLogger(TournamentStateService.class);

    @PostConstruct
    public void setup() {
        String activeTourID = activeTournament.get(ACTIVE_TOURNAMENT_KEY);
        if ((activeTourID != null)&&(!activeTourID.isEmpty())) {
            log.info("Existing active tournament found {}...recovering",activeTourID);
            currentTournament = new Tournament(activeTourID);
        }else{
            log.info("No existing active tournament found");
        }
    }

    @ConsumeEvent("CreateTournament")
    public Uni<JsonObject> createTournament(String name) {
        log.info("createTournament");
        JsonObject res = new JsonObject();
        if (this.currentTournament == null) {
            this.currentTournament = new Tournament();
            log.info("Tournament Created {}",currentTournament);
            activeTournament.putAsync(ACTIVE_TOURNAMENT_KEY,currentTournament.getTournamentID(),1, TimeUnit.DAYS);
            TourTemporalStore.clearRepo();
            res.put("TournamentID", currentTournament.getTournamentID());
        } else {
            log.warn("Tournament already created {}",currentTournament);
            res.put("TournamentID", this.currentTournament.getTournamentID());
        }
        return Uni.createFrom().item(res);
    }

    @ConsumeEvent("GetTournament")
    public Uni<JsonObject> getTournament(String name) {
        log.info("getTournament");
        JsonObject res = new JsonObject();
        if (this.currentTournament != null) {
            log.debug("GetTournament {}", currentTournament);
            res.put("TournamentID", this.currentTournament.getTournamentID());
        }
        return Uni.createFrom().item(res);
    }

    @ConsumeEvent("StartTournament")
    public Uni<Void> startTournament(String tournamentID) {
        log.info("startTournament {}", tournamentID);
        if (this.currentTournament == null) return Uni.createFrom().failure(new Exception("Tournament Not Created"));

        if (!tournamentID.equalsIgnoreCase(currentTournament.getTournamentID())) {
            log.warn("Incorrect Tournament id {} passed in request, active tournament {}", tournamentID, currentTournament.getTournamentID());
            return Uni.createFrom().failure(new Exception("Incorrect TournamentId"));
        } else {
            if (!this.currentTournament.isStarted())
                currentTournament.StartTournament();
        }
        return Uni.createFrom().nullItem();
    }

    @ConsumeEvent("StopTournament")
    public Uni<Void> stopTournament(String tournamentID) {
        log.info("stopTournament {}", tournamentID);
        if (this.currentTournament == null) return Uni.createFrom().failure(new Exception("Tournament Not Created"));

        if (!tournamentID.equalsIgnoreCase(currentTournament.getTournamentID())) {
            log.warn("Incorrect Tournament id {} passed in request, active tournament {}", tournamentID, currentTournament.getTournamentID());
            return Uni.createFrom().failure(new Exception("Incorrect TournamentId, can only stop the current tournament"));
        } else {
            if (this.currentTournament.isStarted()) {
                currentTournament.StopTournament();
                //Copy from cache to repo and persist

                tournamentRepository.persist(currentTournament);
            }
        }
        return Uni.createFrom().nullItem();
    }


    @ConsumeEvent("GetTournamentStatus")
    public Uni<JsonObject> statusTournament(String tournamentID) {
        if (this.currentTournament == null) return Uni.createFrom().failure(new TournamentException("Null TournamentId"));

        if (!tournamentID.equalsIgnoreCase(currentTournament.getTournamentID())) {
            log.warn("Incorrect Tournament id {} passed in request, active tournament {}", tournamentID, currentTournament.getTournamentID());
            return Uni.createFrom().failure(new TournamentException("Incorrect TournamentId"));
        } else {
            JsonObject res = new JsonObject();
            res.put("State", this.currentTournament.getTournamentState());
            log.info("statusTournament {} is {}", tournamentID,this.currentTournament.getTournamentState());
            return Uni.createFrom().item(res);
        }
    }

    @ConsumeEvent("CancelCurrentTournament")
    public void cancelTournament(String name) {
        log.info("cancelTournament");
        currentTournament = null;
    }

    @ConsumeEvent("GetLeaderboard")
    public Uni<String> getTournamentLB(String tournamentID) throws JsonProcessingException {
        List<PetVote> res = new ArrayList<>();
        log.info("getTournamentLB {}", tournamentID);

        if (tournamentID==null) tournamentID = this.currentTournament.getTournamentID();

        if (this.currentTournament == null) {
            Tournament oldTournament = tournamentRepository.findById(new ObjectId(tournamentID));
            //TODO : Read form DB
//            res = oldTournament.getLeaderboard();
        } else {
            if (tournamentID.equalsIgnoreCase(currentTournament.getTournamentID())) {
                res = TourTemporalStore.getLeaderboard();
            }
        }

        ObjectMapper mapper = new ObjectMapper();
        String jsonArray = mapper.writeValueAsString(res);
        return Uni.createFrom().item(jsonArray);
    }

    @ConsumeEvent("AddPetToTournament")
    public Uni<Void> addPetToTournament(JsonObject params) {
        String tourID = params.getString("tournamentId");
        String petID = params.getString("petId");
        log.info("addPetToTournament {}:{}", tourID, petID);

        if (this.currentTournament == null) return Uni.createFrom().failure(new TournamentException("Tournament Not Created"));

        if (!tourID.equalsIgnoreCase(currentTournament.getTournamentID())) {
            log.warn("Incorrect Tournament id {} passed in request, active tournament {}", tourID, currentTournament.getTournamentID());
            return Uni.createFrom().failure(new TournamentException("Incorrect TournamentId"));
        }

        if (currentTournament.isStarted()) {
            log.warn("Tournament started, addPetToTournament {}:{} not allowed", tourID, petID);
            return Uni.createFrom().failure(new TournamentException("TournamentId already active unable to add pet"));
        }
        TourTemporalStore.addPet(petID);
        currentTournament.addPet(petID);

        return Uni.createFrom().nullItem();
    }

    @ConsumeEvent("ProcessPetVote")
    public Uni<Void> voteForPetInTournament(JsonObject params) {
        String tourID = params.getString("tournamentId");
        String petID = params.getString("petId");
        long timestamp = params.getLong("timestamp");
        String direction = params.getString("dir");

        if (this.currentTournament == null) return Uni.createFrom().failure(new TournamentException("Tournament Not Created"));

        log.info("voteForPetInTournament {}:{} dir {} @TS {}", tourID, petID, direction, timestamp);
        if (!tourID.equalsIgnoreCase(currentTournament.getTournamentID())) {
            log.warn("Incorrect Tournament id {} passed in request, active tournament {}", tourID, currentTournament.getTournamentID());
            return Uni.createFrom().failure(new Exception("Incorrect TournamentId"));
        }
        if (direction.equalsIgnoreCase("up"))
            TourTemporalStore.upVotePet(petID);
        else
            TourTemporalStore.downVotePet(petID);

        return Uni.createFrom().nullItem();
    }

    @ConsumeEvent("GetPetVote")
    public Uni<PetVote> getVoteForPetInTournament(JsonObject params) {
        String petID = params.getString("petId");
        if (this.currentTournament == null) return Uni.createFrom().failure(new TournamentException("Tournament Not Created"));
        log.info("getVoteForPetInTournament {}", petID);
        return Uni.createFrom().item(TourTemporalStore.getPetVote(petID));
    }
}
