package com.payv.contracts.common.dto;

public class IdNamePublicDto {

    private final String id;
    private final String name;

    public IdNamePublicDto(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
