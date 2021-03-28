package com.petbattle.repository;

import com.petbattle.core.PetVote;
import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.MeterRegistry;
import io.quarkus.cache.CacheInvalidate;
import io.quarkus.cache.CacheResult;
import io.quarkus.infinispan.client.Remote;
import org.infinispan.client.hotrod.RemoteCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.microprofile.opentracing.Traced;

@Singleton
public class TournamentTemporalRepository {
    private final Logger log = LoggerFactory.getLogger(TournamentTemporalRepository.class);
    private final MeterRegistry registry;

    @Inject
    @Remote("VotesCache")
    RemoteCache<String, PetVote> voteCache;

    public TournamentTemporalRepository(MeterRegistry registry) {
        log.info("TournamentTemporalRepository init");
        this.registry = registry;
    }

    @Timed
    @Traced
    public void addPet(String petID) {
        voteCache.put(petID, new PetVote(petID, 0, 0));
    }

    @Timed
    @Traced
    public void upVotePet(String petID) {
        PetVote currPetVote = voteCache.get(petID);
        if (currPetVote != null) {
            currPetVote.upVote();
            voteCache.put(petID, currPetVote);
            log.info("UpVote {}", currPetVote.toString());
        }
    }

    @Timed
    @Traced
    public void downVotePet(String petID) {
        PetVote currPetVote = voteCache.get(petID);
        if (currPetVote != null) {
            currPetVote.downVote();
            voteCache.put(petID, currPetVote);
            log.info("DownVote {}", currPetVote.toString());
        }
    }

    @Traced
    @Timed
    public PetVote getPetVote(String petID) {
        return voteCache.get(petID);
    }

    @Traced
    @Timed
    public int getPetTally(String petID) {
        PetVote currPetVote = voteCache.get(petID);
        if (currPetVote != null) {
            return currPetVote.getVoteTally();
        } else {
            return 0;
        }
    }

    @CacheInvalidate(cacheName = "leaderboard-cache")
    @Timed
    @Traced
    public List<PetVote> getVotes() {
        return voteCache.values().stream()
                .sorted(Comparator.comparingInt(PetVote::getVoteTally).reversed()).collect(Collectors.toList());
    }

    public void clearRepo() {
        voteCache.clearAsync();
    }

    @CacheResult(cacheName = "leaderboard-cache")
    @Timed
    @Traced
    public List<PetVote> getLeaderboard() {
        return this.getVotes();
    }
}
