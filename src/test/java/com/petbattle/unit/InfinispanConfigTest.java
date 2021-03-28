package com.petbattle.unit;

import com.petbattle.config.ProcessInfinispanAuth;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

class InfinispanConfigTest {

    @Test
    void parseNullAuthFile() {
        ProcessInfinispanAuth testCFg = new ProcessInfinispanAuth("");
        testCFg.ParseAuthFile("");
        Assertions.assertEquals(testCFg.getAuthUserName(), "testuser");
        Assertions.assertEquals(testCFg.getAuthPassWord(), "testpwd");
    }

    @Test
    void parseMissingAuthFile() {
        ProcessInfinispanAuth testCFg = new ProcessInfinispanAuth("/tmp/n/a/b/auth.txt");
        testCFg.ParseAuthFile("");
        Assertions.assertEquals(testCFg.getAuthUserName(), "testuser");
        Assertions.assertEquals(testCFg.getAuthPassWord(), "testpwd");
    }

    @Test
    void parseValidAuthFile() throws IOException {
        File authFileContents = new File(this.getClass().getResource("/infin-creds.yml").getFile());
        String data = new String(Files.readAllBytes(authFileContents.toPath()));
        ProcessInfinispanAuth testCFg = new ProcessInfinispanAuth(data);
        testCFg.ParseAuthFile(data);
        Assertions.assertEquals(testCFg.getAuthUserName(), "developer");
        Assertions.assertEquals(testCFg.getAuthPassWord(), "Q3amUxpHFberq8iA");
    }

}
