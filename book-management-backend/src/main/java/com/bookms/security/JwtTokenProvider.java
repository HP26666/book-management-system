package com.bookms.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {

    private static final String BASE64_PREFIX = "base64:";
    private static final String CLAIM_TYPE = "type";
    private static final String CLAIM_USER_ID = "userId";
    private static final String CLAIM_USERNAME = "username";
    private static final String CLAIM_ROLES = "roles";

    private final Key key;
    private final long accessTokenExpirationMs;
    private final long refreshTokenExpirationMs;

    public JwtTokenProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-token-expiration-ms}") long accessTokenExpirationMs,
            @Value("${jwt.refresh-token-expiration-ms}") long refreshTokenExpirationMs) {
        this.key = buildKey(secret);
        this.accessTokenExpirationMs = accessTokenExpirationMs;
        this.refreshTokenExpirationMs = refreshTokenExpirationMs;
    }

    public String createAccessToken(CustomUserPrincipal principal) {
        Instant now = Instant.now();
        Instant expiry = now.plusMillis(accessTokenExpirationMs);
        return Jwts.builder()
                .subject(principal.username())
                .claims(Map.of(
                        CLAIM_TYPE, "access",
                        CLAIM_USER_ID, principal.id(),
                        CLAIM_USERNAME, principal.username(),
                        CLAIM_ROLES, principal.roles()))
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(key)
                .compact();
    }

    public String createRefreshToken(CustomUserPrincipal principal) {
        Instant now = Instant.now();
        Instant expiry = now.plusMillis(refreshTokenExpirationMs);
        return Jwts.builder()
                .subject(principal.username())
                .claims(Map.of(
                        CLAIM_TYPE, "refresh",
                        CLAIM_USER_ID, principal.id(),
                        CLAIM_USERNAME, principal.username()))
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(key)
                .compact();
    }

    public boolean validateToken(String token) {
        parseClaims(token);
        return true;
    }

    public Claims parseClaims(String token) {
        return Jwts.parser().verifyWith((javax.crypto.SecretKey) key).build().parseSignedClaims(token).getPayload();
    }

    public Long getUserId(String token) {
        return parseClaims(token).get(CLAIM_USER_ID, Long.class);
    }

    public String getUsername(String token) {
        return parseClaims(token).get(CLAIM_USERNAME, String.class);
    }

    public boolean isRefreshToken(String token) {
        return "refresh".equals(parseClaims(token).get(CLAIM_TYPE, String.class));
    }

    public Instant getExpiration(String token) {
        return parseClaims(token).getExpiration().toInstant();
    }

    public long getAccessTokenExpirationSeconds() {
        return accessTokenExpirationMs / 1000;
    }

    private Key buildKey(String secret) {
        String normalizedSecret = secret == null ? "" : secret.trim();
        byte[] keyBytes;
        if (normalizedSecret.startsWith(BASE64_PREFIX)) {
            keyBytes = Decoders.BASE64.decode(normalizedSecret.substring(BASE64_PREFIX.length()));
        } else {
            keyBytes = normalizedSecret.getBytes(StandardCharsets.UTF_8);
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }
}