package com.petbattle.core;

import io.quarkus.mongodb.panache.MongoEntity;
import io.quarkus.mongodb.panache.PanacheMongoEntityBase;
import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.codecs.pojo.annotations.BsonIgnore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

@MongoEntity(collection = "Tournaments")
public class Tournament extends PanacheMongoEntityBase {
    @BsonIgnore
    private final Logger log = LoggerFactory.getLogger(Tournament.class);
    @BsonId
    private String tournamentID;

    private long tournamentStartTS;
    private long tournamentEndTS;
    private Map<String, PetVote> tournamentPets;

    public Tournament() {
        UUID uuid = UUID.randomUUID();
        this.tournamentID = uuid.toString();
        this.tournamentEndTS = 0;
        this.tournamentStartTS = 0;
        this.tournamentPets = new HashMap<>();
    }

    public Tournament(String tournamentID) {
        this.tournamentID = tournamentID;
        this.tournamentEndTS = 0;
        this.tournamentStartTS = 0;
        this.tournamentPets = new HashMap<>();
    }

    public String getTournamentID() {
        return this.tournamentID;
    }

    public long getTournamentStartDT() {
        return this.tournamentStartTS;
    }

    public long getTournamentEndDT() {
        return this.tournamentEndTS;
    }

    public void addPet(String petID) {
        tournamentPets.put(petID, new PetVote(petID, 0, 0));
    }

    public boolean isStarted() {
        return (this.tournamentStartTS != 0);
    }

    public boolean isEnded() {
        return (this.tournamentEndTS != 0);
    }



    @BsonIgnore
    public void addFinalVoteForPet(PetVote finalVote) {
        this.tournamentPets.replace(finalVote.getPetId(), finalVote);
    }

    public Tournament StartTournament() {
        this.tournamentStartTS = System.currentTimeMillis();
        return this;
    }

    public Tournament StopTournament() {
        this.tournamentEndTS = System.currentTimeMillis();
        return this;
    }


    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof Tournament)) {
            return false;
        }
        Tournament tournament = (Tournament) o;
        return tournamentID == tournament.tournamentID && tournamentStartTS == tournament.tournamentStartTS && tournamentEndTS == tournament.tournamentEndTS;
    }

    @Override
    public int hashCode() {
        return Objects.hash(tournamentID, tournamentStartTS, tournamentEndTS);
    }

    @Override
    public String toString() {
        return "{" +
                " tournamentID='" + getTournamentID() + "'" +
                ", tournamentStartDT='" + getTournamentStartDT() + "'" +
                ", tournamentEndDT='" + getTournamentEndDT() + "'" +
                "}";
    }

    public TournamentState getTournamentState() {
        if (!this.isStarted())
            return TournamentState.NotStarted;

        if ((!this.isEnded()))
            return TournamentState.Running;

        return TournamentState.Finished;
    }

    public enum TournamentState {
        NotStarted,
        Finished,
        Running
    }
}