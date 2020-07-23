package com.petbattle.config;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

public class InfinispanAuthCredsList {
    @Getter
    @Setter
    private List<InfinspanCreds> credentials;

    public InfinispanAuthCredsList() {
    }

    public InfinispanAuthCredsList(List<InfinspanCreds> credentials) {
        this.credentials = credentials;
    }

}
