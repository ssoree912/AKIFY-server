package com.jammering.akkipy.domain.user;

public enum Role {
    USER("roleUser"),
    ADMIN("roleAdmin"),
    GUEST("roleGuest");

    private final String value;

    Role(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

}
