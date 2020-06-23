package com.petbattle;

import java.util.List;
import java.util.Map;
import java.util.Random;

import com.petbattle.core.PetVote;
import com.petbattle.core.Tournament;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class TournamentTest {

    @Test
    public void testTournamentInit() {
        Random r = new Random();
        int TID = r.nextInt();
        String PID1 = String.valueOf(r.nextInt());
        String PID2 = String.valueOf(r.nextInt());
        Tournament newT = new Tournament(TID);
        Assertions.assertTrue(!newT.isStarted());
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
        String PID1 = String.valueOf(r.nextInt());
        Tournament newT = new Tournament(TID);
        newT.addPet(PID1);
        newT.downVotePet(PID1);
        Assertions.assertEquals(newT.getPetTally(PID1), 0);
        Assertions.assertTrue(!newT.isStarted());
        newT.StartTournament();
        Assertions.assertTrue(newT.isStarted());
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

    @Test
    public void testTournamentLeaderBoard() {
        Random r = new Random();
        int TID = r.nextInt();
        String PID1 = String.valueOf(r.nextInt());
        String PID2 = String.valueOf(r.nextInt());
        String PID3 = String.valueOf(r.nextInt());
        String PID4 = String.valueOf(r.nextInt());
        String PID5 = String.valueOf(r.nextInt());
        String PID6 = String.valueOf(r.nextInt());
        String PID7 = String.valueOf(r.nextInt());
        Tournament newT = new Tournament(TID);
        newT.addPet(PID1);
        newT.addPet(PID2);
        newT.addPet(PID3);
        newT.addPet(PID4);
        newT.addPet(PID5);
        newT.addPet(PID6);
        newT.addPet(PID7);
        newT.StartTournament();
        newT.downVotePet(PID1);
        newT.downVotePet(PID2);
        newT.downVotePet(PID2);
        newT.downVotePet(PID3);
        newT.downVotePet(PID3);
        newT.downVotePet(PID3);
        newT.upVotePet(PID4);
        newT.upVotePet(PID5);
        newT.upVotePet(PID5);
        newT.upVotePet(PID6);
        newT.upVotePet(PID6);
        newT.upVotePet(PID6);
        List<PetVote> lb2 = newT.getLeaderboard();
        lb2.forEach(System.out::println);
        Assertions.assertTrue(lb2.get(0).getPetID().equalsIgnoreCase(PID6));
        Assertions.assertTrue(lb2.get(1).getPetID().equalsIgnoreCase(PID5));
        Assertions.assertTrue(lb2.get(2).getPetID().equalsIgnoreCase(PID4));
        Assertions.assertTrue(lb2.get(3).getPetID().equalsIgnoreCase(PID7));
        Assertions.assertTrue(lb2.get(4).getPetID().equalsIgnoreCase(PID1));
        Assertions.assertTrue(lb2.get(5).getPetID().equalsIgnoreCase(PID2));
        Assertions.assertTrue(lb2.get(6).getPetID().equalsIgnoreCase(PID3));
    }
}