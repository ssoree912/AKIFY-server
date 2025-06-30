package com.jammering.akkipy.service.DTO;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

public class KakaoDTO {

        @Getter
        @Setter
        @Builder
        public static class KaKaoInfo {
            private String uid;
        }
}
