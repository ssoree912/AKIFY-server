package com.jammering.akkipy.config.jwt;

import com.jammering.akkipy.domain.user.UserRepository;
import com.jammering.akkipy.domain.userLogin.CustomUserDetails;
import com.jammering.akkipy.domain.userLogin.CustomUserInfoDto;
import com.jammering.akkipy.service.auth.CustomUserDetailsService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static com.jammering.akkipy.config.PermitAllPaths.PATHS;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;
    private final CustomUserDetailsService customUserDetailsService;
    private final UserRepository userRepository;

    private static final String[] SIGNUP_PATH = {"/api/v1/auth/signup/**"};
    private static final List<AntPathRequestMatcher> PUBLIC_URL_MATCHERS =
            Stream.of(PATHS, SIGNUP_PATH)
                    .flatMap(Stream::of)
                    .map(AntPathRequestMatcher::new)
                    .toList();

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        boolean shouldNotFilter = PUBLIC_URL_MATCHERS.stream()
                .anyMatch(matcher -> matcher.matches(request));

        if (shouldNotFilter) {
            log.info("Skipping JWT filter for public path: {}", request.getRequestURI());
        } else {
            log.info("Applying JWT filter for protected path: {}", request.getRequestURI());
        }

        return shouldNotFilter;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String token = resolveToken(request);
        if (token != null && jwtProvider.validateToken(token)) {
            Claims claims = jwtProvider.getClaims(token);
            String subject = claims.getSubject();
            String role = claims.get("role", String.class);
            String provider = claims.get("provider", String.class);
            boolean signup = claims.get("signup", Boolean.class);

            CustomUserInfoDto userInfoDto;
            if (signup) {
                // 가입 완료된 유저라면 DB 조회
                UserDetails userDetails = customUserDetailsService.loadUserByUsername(subject);
                userInfoDto = ((CustomUserDetails) userDetails).getCustomUserInfoDto();
            } else {
                // GUEST 유저는 DB 조회 없이 생성
                userInfoDto = CustomUserInfoDto.toGuestDto(subject, provider,role);
            }
            log.info(role);
            CustomUserDetails customUserDetails = new CustomUserDetails(userInfoDto);
            List<GrantedAuthority> authorities = List.of(
                    new SimpleGrantedAuthority("ROLE_" + userInfoDto.getRole())
            );

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(customUserDetails, null, authorities);

            log.info("JWT Authentication for user: {}", authentication.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        return (bearer != null && bearer.startsWith("Bearer ")) ? bearer.substring(7) : null;
    }


}