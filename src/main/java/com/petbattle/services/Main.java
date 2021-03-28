package com.petbattle.services;

import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;

import java.io.InputStream;
import java.util.Properties;

@QuarkusMain
public class Main {
    public static void main(String... args) {
        Main.printGitInfo();
        Quarkus.run(MyApp.class, args);
    }

    public static class MyApp implements QuarkusApplication {
        @Override
        public int run(String... args) throws Exception {
            Quarkus.waitForExit();
            return 0;
        }
    }

    public static void printGitInfo() {
        try {
            InputStream confFile = Main.class.getResourceAsStream("/git.properties");
            Properties prop = new Properties();
            prop.load(confFile);
            prop.forEach((k, v) -> {
                System.out.println("GITINFO -> " + k + ":" + v);
            });
        } catch (Exception ex) {
            System.out.println("GITINFO -> Unable to get git.properties file " + ex.getMessage());
        }
    }
}
