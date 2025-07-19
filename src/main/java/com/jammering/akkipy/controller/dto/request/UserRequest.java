package com.jammering.akkipy.controller.dto.request;

import lombok.Getter;
import lombok.Setter;

public class UserRequest {
    @Getter
    @Setter
    public static class Auth {
        private String token;
        private String state;
        private String email;
        private String password;

    }
    @Setter
    @Getter
    public static class Firebase {
        private String email;
        private String password;
    }

    @Getter
    @Setter
    public static class SignUp {
        private String nickname;
    }

    @Getter
    @Setter
    public static class Reissue {
        private String refreshToken;
    }

    @Getter
    @Setter
    public static class SendEmailVerification {
        private String email;
    }

    @Getter
    @Setter
    public static class VerifyEmail {
        private String email;
        private String password;
        private String code;
    }
}