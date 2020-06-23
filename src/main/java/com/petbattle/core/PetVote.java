package com.petbattle.core;

import java.io.Serializable;
import java.util.Objects;

public class PetVote implements Comparable<PetVote>, Serializable {
    private int upVotes;
    private int downVotes;
    private String petId;

    public PetVote(String petId) {
        this.upVotes = 0;
        this.downVotes = 0;
        this.petId = petId;
    }

    public String getPetID(){
        return this.petId;
    }

    public void upVote() {
        this.upVotes++;
    }

    public void downVote() {
        this.downVotes++;
    }

    public int getVoteTally() {
        return upVotes - downVotes;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof PetVote)) {
            return false;
        }
        PetVote petVote = (PetVote) o;
        return upVotes == petVote.upVotes && downVotes == petVote.downVotes;
    }

    @Override
    public int hashCode() {
        return Objects.hash(upVotes, downVotes);
    }

    @Override
    public String toString() {
        return "{" +
            " upVotes='" + upVotes + "'" +
            ", downVotes='" + downVotes + "'" +
            ", runningTotal='" + getVoteTally()+ "'" +
            "}";
    }

    @Override
    public int compareTo(PetVote o){
        return  o.getVoteTally() - this.getVoteTally();
    }

}