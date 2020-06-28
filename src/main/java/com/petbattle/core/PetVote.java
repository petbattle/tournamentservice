package com.petbattle.core;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serializable;
import java.util.Objects;

public class PetVote implements Comparable<PetVote>, Serializable {
    private int upVotes;
    private int downVotes;
    private String petId;

    public PetVote(int upVotes, int downVotes, String petId) {
        this.upVotes = upVotes;
        this.downVotes = downVotes;
        this.petId = petId;
    }

    public PetVote(String petId) {
        this.upVotes = 0;
        this.downVotes = 0;
        this.petId = petId;
    }

    public PetVote() {
    }

    public int getUpVotes() {
        return upVotes;
    }

    public int getDownVotes() {
        return downVotes;
    }

    public String getPetId(){
        return this.petId;
    }

    public void upVote() {
        this.upVotes++;
    }

    public void downVote() {
        this.downVotes++;
    }

    @JsonIgnore
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
