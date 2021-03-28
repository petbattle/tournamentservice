package com.petbattle.core;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.infinispan.protostream.annotations.ProtoFactory;
import org.infinispan.protostream.annotations.ProtoField;

import java.io.Serializable;
import java.util.Objects;

public class PetVote implements Comparable<PetVote>, Serializable {
    private int upVotes;
    private int downVotes;
    private String petId;

    public void setUpVotes(int upVotes) {
        this.upVotes = upVotes;
    }

    public void setDownVotes(int downVotes) {
        this.downVotes = downVotes;
    }

    public void setPetId(String petId) {
        this.petId = petId;
    }

    public PetVote(String petId, int upVotes, int downVotes) {
        this.upVotes = 0;
        this.downVotes = 0;
        this.petId = petId;
    }

    public PetVote() {
    }

    @ProtoField(number = 1, defaultValue = "0")
    public int getUpVotes() {
        return upVotes;
    }

    @ProtoField(number = 2, defaultValue = "0")
    public int getDownVotes() {
        return downVotes;
    }

    @ProtoField(number = 3)
    public String getPetId() {
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
                ", runningTotal='" + getVoteTally() + "'" +
                "}";
    }

    @Override
    public int compareTo(PetVote o) {
        return o.getVoteTally() - this.getVoteTally();
    }

}
