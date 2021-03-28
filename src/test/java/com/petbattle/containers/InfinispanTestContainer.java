package com.petbattle.containers;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import org.testcontainers.containers.GenericContainer;

import java.util.HashMap;
import java.util.Map;

public class InfinispanTestContainer implements QuarkusTestResourceLifecycleManager {
    public static GenericContainer infinispanContainer = new GenericContainer("infinispan/server:latest")
            .withExposedPorts(11222)
            .withEnv("USER", "user")
            .withEnv("PASS", "pass");

    @Override
    public Map<String, String> start() {
        infinispanContainer.start();
        Map<String, String> res = new HashMap();
        res.put("quarkus.infinispan-client.server-list",
                infinispanContainer.getHost() + ":" + infinispanContainer.getFirstMappedPort());
        res.put("quarkus.infinispan-client.auth-username", "user");
        res.put("quarkus.infinispan-client.auth-password", "pass");
        res.put("quarkus.infinispan-client.client-intelligence", "BASIC");
        res.put("quarkus.infinispan-client.trust-store", "");
        res.put("quarkus.infinispan-client.trust-store-password", "");
        return res;
    }

    @Override
    public void stop() {
        infinispanContainer.stop();
    }
}
