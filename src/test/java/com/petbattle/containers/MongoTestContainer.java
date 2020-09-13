package com.petbattle.containers;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import org.testcontainers.containers.MongoDBContainer;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class MongoTestContainer implements QuarkusTestResourceLifecycleManager {
    private static final MongoDBContainer DATABASE = new MongoDBContainer("mongo:4.2").withExposedPorts(27017);

    @Override
    public Map<String, String> start() {
        DATABASE.start();
        Map<String, String> res = new HashMap();
        res.put("quarkus.mongodb.connection-string", "mongodb://" + DATABASE.getContainerIpAddress() + ":" + DATABASE.getFirstMappedPort());
        res.put("quarkus.mongodb.credentials.username","");
        res.put("quarkus.mongodb.credentials.password","");
        res.put("quarkus.mongodb.credentials.auth-source","");
        return res;
    }

    @Override
    public void stop() {
        DATABASE.stop();
    }
}
