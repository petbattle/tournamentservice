package com.petbattle;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Tournament {
    private int tournamentID;
    private long tournamentStartTS;
    private long tournamentEndTS;
    private Map<String,PetVote> tournamentPets;

    public int getTournamentID() {
        return this.tournamentID;
    }

    public long getTournamentStartDT() {
        return this.tournamentStartTS;
    }

    public long getTournamentEndDT() {
        return this.tournamentEndTS;
    }

    private Tournament() {}

    public Tournament(int tournamentID) {
        this.tournamentID = tournamentID;
        this.tournamentPets = new HashMap<>();
        this.tournamentEndTS = 0;
        this.tournamentStartTS = 0;
    }

    public void addPet(String petID){
        tournamentPets.putIfAbsent(petID, new PetVote());
    }

    public int getPetTally(String petID){
        PetVote currPetVote = tournamentPets.get(petID);
        if (currPetVote != null){
            return currPetVote.getVoteTally();
        }else{
            return 0;
        }        
    }


    public Tournament StartTournament(){
        this.tournamentStartTS = System.currentTimeMillis();
        return this;
    }

    public Tournament EndTournament(){
        this.tournamentEndTS = System.currentTimeMillis();
        return this;
    }

    public void upVotePet(String petID){
        if (this.tournamentStartTS == 0) return;
        PetVote currPetVote = tournamentPets.get(petID);
        if (currPetVote != null){
            currPetVote.upVote();
            tournamentPets.put(petID,currPetVote);
        }
    }

    public void downVotePet(String petID){
        if (this.tournamentStartTS == 0) return;
        PetVote currPetVote = tournamentPets.get(petID);
        if (currPetVote != null){
            currPetVote.downVote();
            tournamentPets.put(petID,currPetVote);
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
}