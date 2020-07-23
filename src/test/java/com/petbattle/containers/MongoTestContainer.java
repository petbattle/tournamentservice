package com.petbattle.containers;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import org.testcontainers.containers.MongoDBContainer;

import java.util.Collections;
import java.util.Map;

public class MongoTestContainer implements QuarkusTestResourceLifecycleManager {
    private static final MongoDBContainer DATABASE = new MongoDBContainer("mongo:4.2").withExposedPorts(27017);

    @Override
    public Map<String, String> start() {
        DATABASE.start();
        return Collections.singletonMap("quarkus.mongodb.connection-string", "mongodb://" + DATABASE.getContainerIpAddress() + ":" + DATABASE.getFirstMappedPort());
    }

    @Override
    public void stop() {
        DATABASE.stop();
    }
}
