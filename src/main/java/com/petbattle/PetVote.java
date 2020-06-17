package com.petbattle;

import java.util.Objects;

public class PetVote {
    private int upVotes;
    private int downVotes;

    public PetVote() {
        this.upVotes = 0;
        this.downVotes = 0;
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
            "}";
    }


}