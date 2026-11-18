package com.ecommerce.composite.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RefreshTokenService {

    private final Map<String, RefreshToken> tokens = new ConcurrentHashMap<>();
    private final SecureRandom secureRandom = new SecureRandom();
    private final Duration tokenLifetime;

    public RefreshTokenService(
            @Value("${auth.refresh-token-ttl-seconds}") long tokenLifetimeSeconds
    ) {
        this.tokenLifetime = Duration.ofSeconds(tokenLifetimeSeconds);
    }

    public String issue(String username) {
        byte[] tokenBytes = new byte[32];
        secureRandom.nextBytes(tokenBytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
        tokens.put(hash(token), new RefreshToken(username, Instant.now().plus(tokenLifetime)));
        return token;
    }

    public String rotate(String token) {
        RefreshToken storedToken = tokens.remove(hash(token));
        if (storedToken == null || !storedToken.expiresAt().isAfter(Instant.now())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token.");
        }
        return storedToken.username();
    }

    public void revoke(String token) {
        tokens.remove(hash(token));
    }

    private String hash(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(token.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is not available.", exception);
        }
    }

    private record RefreshToken(String username, Instant expiresAt) {
    }
}
