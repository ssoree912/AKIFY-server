package com.jammering.akkipy.domain.userLogin;

public enum Provider {
    LOCAL("local"),
    GOOGLE("google"),
    KAKAO("kakao"),
    NAVER("naver");

    private final String value;

    Provider(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
