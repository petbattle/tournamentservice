package com.petbattle.services;

import com.petbattle.core.PetVote;
import io.quarkus.infinispan.client.Remote;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Liveness;
import org.infinispan.client.hotrod.RemoteCache;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@Liveness
@ApplicationScoped
public class HealthChecks implements HealthCheck {

    @Inject
    @Remote("VotesCache")
    RemoteCache<String, PetVote> voteCache;

    @Override
    public HealthCheckResponse call() {
        try {
            voteCache.serverStatistics().getStatsMap();
            return HealthCheckResponse.up("Cache health check passed");
        } catch (Exception ex) {
            return HealthCheckResponse.down("Cache health check failed");
        }
    }
}