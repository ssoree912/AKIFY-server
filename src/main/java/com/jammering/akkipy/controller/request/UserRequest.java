package com.jammering.akkipy.controller.request;

import lombok.Getter;
import lombok.Setter;

public class UserRequest {
    @Getter
    @Setter
    public static class Token{
        private String token;

    }
}
