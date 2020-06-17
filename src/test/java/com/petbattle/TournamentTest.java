package com.petbattle;

import java.util.Random;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class TournamentTest {

    @Test
    public void testTournamentInit() {
        Random r = new Random();
        int TID = r.nextInt();
        String PID1 = String.valueOf( r.nextInt());
        String PID2 = String.valueOf( r.nextInt());
        Tournament newT = new Tournament( TID);
        newT.addPet(PID1);
        Assertions.assertEquals(newT.getPetTally(PID1), 0);
        Assertions.assertEquals(newT.getPetTally(PID2), 0);
        newT.downVotePet("random");
        newT.downVotePet("random");
        Assertions.assertEquals(newT.getPetTally("random"), 0);
    }

    @Test
    public void testTournamentCount() {
        Random r = new Random();
        int TID = r.nextInt();
        String PID1 = String.valueOf( r.nextInt());
        Tournament newT = new Tournament( TID);
        newT.addPet(PID1);
        newT.downVotePet(PID1);
        Assertions.assertEquals(newT.getPetTally(PID1), 0);
        newT.StartTournament();
        newT.downVotePet(PID1);
        newT.downVotePet(PID1);
        newT.downVotePet(PID1);
        Assertions.assertEquals(newT.getPetTally(PID1), -3);
        newT.upVotePet(PID1);
        newT.upVotePet(PID1);
        newT.upVotePet(PID1);
        newT.upVotePet(PID1);
        Assertions.assertEquals(newT.getPetTally(PID1), 1);
    }      
}