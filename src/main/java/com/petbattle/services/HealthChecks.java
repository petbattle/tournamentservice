package com.petbattle.services;

import com.petbattle.core.PetVote;
import com.petbattle.core.Tournament;
import com.petbattle.repository.TournamentRepository;
import io.quarkus.infinispan.client.Remote;
import io.quarkus.mongodb.panache.PanacheMongoRepository;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Liveness;
import org.eclipse.microprofile.health.Readiness;
import org.infinispan.client.hotrod.RemoteCache;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@Liveness
@Readiness
@ApplicationScoped
public class HealthChecks implements HealthCheck {

    @Inject
    @Remote("VotesCache")
    RemoteCache<String, PetVote> voteCache;

    @Inject
    PanacheMongoRepository<Tournament> TournamentRepository;

    @Override
    public HealthCheckResponse call() {
        try {
            TournamentRepository.mongoDatabase().runCommand(new Document("dbStats", 1));
            voteCache.serverStatistics().getStatsMap();
            return HealthCheckResponse.up("Cache health check passed");
        } catch (Exception ex) {
            return HealthCheckResponse.down("Cache health check failed");
        }
    }
}
