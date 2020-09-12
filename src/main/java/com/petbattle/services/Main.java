package com.petbattle.services;

import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;
import io.quarkus.runtime.Quarkus;

@QuarkusMain  
public class Main {
    public static void main(String... args) {
        System.out.println("Starting....");
        Quarkus.run(MyApp.class, args);
    }

    public static class MyApp implements QuarkusApplication {
        @Override
        public int run(String... args) throws Exception {
            Quarkus.waitForExit();
            return 0;
        }
    }
}