package com.petbattle.services;

import com.petbattle.core.PetVote;
import com.petbattle.core.Tournament;
import io.quarkus.launcher.shaded.org.slf4j.Logger;
import io.quarkus.launcher.shaded.org.slf4j.LoggerFactory;
import io.quarkus.vertx.ConsumeEvent;
import io.vertx.core.json.JsonObject;

import javax.enterprise.context.ApplicationScoped;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@ApplicationScoped
public class TournamentStateService {
    private final Logger log = LoggerFactory.getLogger(TournamentStateService.class);
    private Tournament currentTournament;

    @ConsumeEvent("StartTournament")
    public void startTournament(int tournamentID) {
    }

    @ConsumeEvent("StopTournament")
    public void stopTournament(String name) {
    }

    @ConsumeEvent("CreateTournament")
    public int createTournament(String name) {
        Random r = new Random();
        int TID = r.nextInt();
        return TID;
    }

    @ConsumeEvent("TournamentLeaderBoard")
    public JsonObject getTournamentLB(int tournamentID) {
        List<PetVote> res = new ArrayList<>();
        res.add(new PetVote("0"));
        res.add(new PetVote("2"));
        res.add(new PetVote("3"));
        res.add(new PetVote("4"));
        res.add(new PetVote("5"));
        return JsonObject.mapFrom(res);
    }

}