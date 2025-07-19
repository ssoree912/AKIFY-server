package com.jammering.akkipy.controller;

import com.jammering.akkipy.common.code.SuccessCode;
import com.jammering.akkipy.common.response.APIResponse;
import com.jammering.akkipy.common.response.ErrorResponse;
import com.jammering.akkipy.controller.dto.request.UserRequest;
import com.jammering.akkipy.controller.dto.response.TokenResponse;
import com.jammering.akkipy.controller.dto.response.UserResponse;
import com.jammering.akkipy.domain.userLogin.CustomUserDetails;
import com.jammering.akkipy.domain.userLogin.Provider;
import com.jammering.akkipy.common.code.ErrorCode;
import com.jammering.akkipy.exception.EmailVerificationException;
import com.jammering.akkipy.service.EmailService;
import com.jammering.akkipy.service.RedisService;
import com.jammering.akkipy.service.UserService;
import com.jammering.akkipy.service.auth.AuthService;
import com.jammering.akkipy.service.auth.AuthStrategyManager;
import com.jammering.akkipy.service.auth.strategy.FirebaseAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

@Tag(name = "Auth", description = "인증 및 사용자 관련 API")
@RequiredArgsConstructor
@RequestMapping("api/v1/auth")
@Slf4j
@RestController
public class AuthController {
    private final AuthStrategyManager authStrategyManager;
    private final FirebaseAuthService firebaseAuthService;
    private final UserService userService;
    private final AuthService authService;
    private final EmailService emailService;
    private final RedisService redisService;
    private final PasswordEncoder passwordEncoder;

    private static final String VERIFICATION_CODE_KEY_PREFIX = "verificationCode:";

    @Operation(summary = "로그인", description = "소셜 로그인 또는 로컬 로그인을 통해 토큰을 발급합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "로그인 성공 및 토큰 발급",
                    content = @Content(schema = @Schema(implementation = TokenResponse.ToKenInfo.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (예: 유효하지 않은 토큰, 잘못된 이메일/비밀번호)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "서버 오류",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("signin/{provider}")
    public ResponseEntity<APIResponse<TokenResponse.ToKenInfo>> login(@PathVariable Provider provider, @RequestBody UserRequest.Auth auth) throws Exception {
        APIResponse response =  APIResponse.of(SuccessCode.SELECT_SUCCESS,authStrategyManager.signIn(provider, auth));
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Operation(summary = "이메일 인증 코드 전송", description = "회원가입을 위한 이메일 인증 코드를 전송합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "이메일 전송 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (예: 유효하지 않은 이메일 형식)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "서버 오류",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/email/send")
    public ResponseEntity<APIResponse<Void>> sendVerificationEmail(@RequestBody UserRequest.SendEmailVerification request) {
        String verificationCode = emailService.sendVerificationEmail(request.getEmail());
        // 인증 코드만 Redis에 저장
        redisService.setValues(VERIFICATION_CODE_KEY_PREFIX + request.getEmail(), verificationCode, Duration.ofMinutes(5));

        APIResponse response = APIResponse.of(SuccessCode.INSERT_SUCCESS, null);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Operation(summary = "이메일 인증 및 회원가입", description = "이메일 인증 코드를 확인하고 회원가입을 완료합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "회원가입 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (예: 유효하지 않거나 만료된 인증 코드, 이미 존재하는 이메일)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "서버 오류",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/email/verify")
    public ResponseEntity<APIResponse<Void>> verifyEmailAndSignUp(@RequestBody UserRequest.VerifyEmail request) throws Exception {
        String storedCode = redisService.getValues(VERIFICATION_CODE_KEY_PREFIX + request.getEmail());

        if (storedCode == null || !storedCode.equals(request.getCode())) {
            throw new EmailVerificationException(ErrorCode.INVALID_VERIFICATION_CODE);
        }

        firebaseAuthService.createAccountInFirebase(request.getEmail(), request.getPassword());

        redisService.deleteValues(VERIFICATION_CODE_KEY_PREFIX + request.getEmail());

        APIResponse response = APIResponse.of(SuccessCode.INSERT_SUCCESS, null);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(summary = "회원가입", description = "임시 토큰을 가진 사용자가 닉네임을 설정하여 정식 회원으로 가입합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "회원가입 성공 및 정식 토큰 발급",
                    content = @Content(schema = @Schema(implementation = UserResponse.UserTokenInfo.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (예: 이미 사용 중인 닉네임)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자 (임시 토큰 없음)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "서버 오류",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("signup")
    public ResponseEntity<APIResponse<UserResponse.UserTokenInfo>> signUp(@RequestBody UserRequest.SignUp signUp , @AuthenticationPrincipal CustomUserDetails userDetails)  {
        UserResponse.UserTokenInfo userTokenInfo =userService.registerUser(signUp.getNickname(), userDetails.getCustomUserInfoDto().getUid(), userDetails.getCustomUserInfoDto().getProvider());
        log.info(userDetails.getCustomUserInfoDto().getProvider().toString());
        APIResponse response = APIResponse.of(SuccessCode.INSERT_SUCCESS, userTokenInfo);
        return new ResponseEntity<>(response, HttpStatus.CREATED);

    }

    @Operation(summary = "토큰 재발급", description = "만료된 Access Token과 Refresh Token을 사용하여 새로운 토큰을 발급합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "토큰 재발급 성공",
                    content = @Content(schema = @Schema(implementation = TokenResponse.ToKenInfo.class))),
            @ApiResponse(responseCode = "401", description = "유효하지 않거나 만료된 Refresh Token",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "서버 오류",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/reissue")
    public ResponseEntity<APIResponse<TokenResponse.ToKenInfo>> reissue(@RequestBody UserRequest.Reissue reissue) {
        TokenResponse.ToKenInfo tokenInfo = authService.reissueToken(reissue.getRefreshToken());
        APIResponse response = APIResponse.of(SuccessCode.SELECT_SUCCESS, tokenInfo);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

}

