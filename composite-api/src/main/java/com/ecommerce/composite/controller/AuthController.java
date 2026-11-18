package com.ecommerce.composite.controller;

import com.ecommerce.composite.service.RefreshTokenService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private static final String REFRESH_COOKIE = "refresh_token";

    private final AuthenticationManager authenticationManager;
    private final JwtEncoder jwtEncoder;
    private final RefreshTokenService refreshTokenService;
    private final long accessTokenLifetimeSeconds;
    private final long refreshTokenLifetimeSeconds;
    private final boolean secureCookie;

    public AuthController(
            AuthenticationManager authenticationManager,
            JwtEncoder jwtEncoder,
            RefreshTokenService refreshTokenService,
            @Value("${auth.access-token-ttl-seconds}") long accessTokenLifetimeSeconds,
            @Value("${auth.refresh-token-ttl-seconds}") long refreshTokenLifetimeSeconds,
            @Value("${auth.cookie-secure}") boolean secureCookie
    ) {
        this.authenticationManager = authenticationManager;
        this.jwtEncoder = jwtEncoder;
        this.refreshTokenService = refreshTokenService;
        this.accessTokenLifetimeSeconds = accessTokenLifetimeSeconds;
        this.refreshTokenLifetimeSeconds = refreshTokenLifetimeSeconds;
        this.secureCookie = secureCookie;
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@RequestBody LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );
        return tokenResponse(authentication.getName(), refreshTokenService.issue(authentication.getName()));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Void> invalidCredentials() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(
            @CookieValue(name = REFRESH_COOKIE, required = false) String refreshToken
    ) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token is required.");
        }
        String username = refreshTokenService.rotate(refreshToken);
        return tokenResponse(username, refreshTokenService.issue(username));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @CookieValue(name = REFRESH_COOKIE, required = false) String refreshToken
    ) {
        if (refreshToken != null && !refreshToken.isBlank()) {
            refreshTokenService.revoke(refreshToken);
        }
        return ResponseEntity.noContent()
                .header("Set-Cookie", refreshCookie("", 0).toString())
                .build();
    }

    private ResponseEntity<TokenResponse> tokenResponse(String username, String refreshToken) {
        Instant now = Instant.now();
        String accessToken = jwtEncoder.encode(JwtEncoderParameters.from(
                JwsHeader.with(MacAlgorithm.HS256).build(),
                JwtClaimsSet.builder()
                        .issuer("ecommerce-composite-api")
                        .subject(username)
                        .issuedAt(now)
                        .expiresAt(now.plusSeconds(accessTokenLifetimeSeconds))
                        .id(UUID.randomUUID().toString())
                        .build()
        )).getTokenValue();

        return ResponseEntity.ok()
                .header("Set-Cookie", refreshCookie(refreshToken, refreshTokenLifetimeSeconds).toString())
                .body(new TokenResponse(accessToken, "Bearer", accessTokenLifetimeSeconds));
    }

    private ResponseCookie refreshCookie(String value, long maxAgeSeconds) {
        return ResponseCookie.from(REFRESH_COOKIE, value)
                .httpOnly(true)
                .secure(secureCookie)
                .sameSite("Strict")
                .path("/auth")
                .maxAge(maxAgeSeconds)
                .build();
    }

    public record LoginRequest(String username, String password) {
    }

    public record TokenResponse(String accessToken, String tokenType, long expiresIn) {
    }
}
