package com.petbattle.core;

import io.quarkus.mongodb.panache.MongoEntity;
import io.quarkus.mongodb.panache.PanacheMongoEntityBase;
import io.quarkus.mongodb.panache.PanacheMongoRepositoryBase;
import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.codecs.pojo.annotations.BsonIgnore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

@MongoEntity(collection="Tournaments")
public class Tournament extends PanacheMongoEntityBase {
    public enum TournamentState {
        NotStarted,
        Finished,
        Running
    }

    @BsonIgnore
    private final Logger log = LoggerFactory.getLogger(Tournament.class);

    @BsonId
    private String tournamentID;

    private long tournamentStartTS;
    private long tournamentEndTS;

    private Map<String, PetVote> tournamentPets;

    public String getTournamentID() {
        return this.tournamentID;
    }

    public long getTournamentStartDT() {
        return this.tournamentStartTS;
    }

    public long getTournamentEndDT() {
        return this.tournamentEndTS;
    }


    public Tournament() {
        UUID uuid = UUID.randomUUID();
        this.tournamentID = uuid.toString();
        this.tournamentPets = new HashMap<>();
        this.tournamentEndTS = 0;
        this.tournamentStartTS = 0;
    }

    public void addPet(String petID) {
        tournamentPets.putIfAbsent(petID, new PetVote(petID));
    }

    public boolean isStarted(){
        return (this.tournamentStartTS != 0);
    }

    public boolean isEnded(){
        return (this.tournamentEndTS != 0);
    }

    public int getPetTally(String petID) {
        PetVote currPetVote = tournamentPets.get(petID);
        if (currPetVote != null) {
            return currPetVote.getVoteTally();
        } else {
            return 0;
        }
    }

    public List<PetVote> getLeaderboard() {
        List<PetVote> lbList = new ArrayList<>(tournamentPets.values());
        Collections.sort(lbList);
        return lbList;
    }

    public Tournament StartTournament() {
        this.tournamentStartTS = System.currentTimeMillis();
        return this;
    }

    public Tournament StopTournament() {
        this.tournamentEndTS = System.currentTimeMillis();
        return this;
    }

    public void upVotePet(String petID) {
        if (this.tournamentStartTS == 0) return;
        PetVote currPetVote = tournamentPets.get(petID);
        if (currPetVote != null) {
            currPetVote.upVote();
            tournamentPets.put(petID, currPetVote);
            log.info("UpVote {}",currPetVote.toString());
        }
    }

    public void downVotePet(String petID) {
        if (this.tournamentStartTS == 0) return;
        PetVote currPetVote = tournamentPets.get(petID);
        if (currPetVote != null) {
            currPetVote.downVote();
            tournamentPets.put(petID, currPetVote);
            log.info("DownVote {}",currPetVote.toString());
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof Tournament)) {
            return false;
        }
        Tournament tournament = (Tournament) o;
        return tournamentID == tournament.tournamentID && tournamentStartTS == tournament.tournamentStartTS && tournamentEndTS == tournament.tournamentEndTS && Objects.equals(tournamentPets, tournament.tournamentPets);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tournamentID, tournamentStartTS, tournamentEndTS, tournamentPets);
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

        return  TournamentState.Finished;
    }
}