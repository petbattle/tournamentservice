package com.petbattle.config;

import lombok.Getter;
import lombok.Setter;

public class InfinspanCreds {
    @Getter
    @Setter
    private String username;

    @Getter
    @Setter
    private String password;

    public InfinspanCreds() {
    }

    public InfinspanCreds(String username, String password) {
        this.username = username;
        this.password = password;
    }

}
