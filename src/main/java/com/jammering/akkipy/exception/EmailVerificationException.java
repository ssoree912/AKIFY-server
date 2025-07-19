package com.jammering.akkipy.exception;

import com.jammering.akkipy.common.code.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class EmailVerificationException extends RuntimeException {
    private final ErrorCode errorCode;
}
