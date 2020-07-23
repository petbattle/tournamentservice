package com.petbattle.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.List;

@Slf4j
public class ProcessInfinispanAuth {
    @Getter
    private String authUserName;

    @Getter
    private String authPassWord;

    public ProcessInfinispanAuth(String InfinispanAuthFileContents) {
        if (InfinispanAuthFileContents.isEmpty()) {
            log.error("Empty authFile location, using default values which will result in errors. Set location using Infinispan.CredFileLocn ");
            this.authPassWord = "testpwd";
            this.authUserName = "testuser";
        } else
            ParseAuthFile(InfinispanAuthFileContents);
    }

    public void ParseAuthFile(String authFileContents) {
        try {
            ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
            InfinispanAuthCredsList credList = mapper.readValue(authFileContents, InfinispanAuthCredsList.class);
            this.authUserName = credList.getCredentials().get(0).getUsername();
            this.authPassWord = credList.getCredentials().get(0).getPassword();
        } catch (Exception e) {
            log.error("Unable to parse authfile, using default values which will result in errors. ", e);
            this.authPassWord = "testpwd";
            this.authUserName = "testuser";
        }
    }






}
