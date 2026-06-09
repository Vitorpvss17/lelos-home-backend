package com.leloshome.backend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.function.Function;

@Service
public class JwtService {

    private static final String TOKEN_TYPE_CLAIM = "type";
    private static final String TOKEN_VERSION_CLAIM = "ver";
    private static final String ACCESS_TYPE = "access";
    private static final String REFRESH_TYPE = "refresh";

    private final SecretKey key;
    private final long expirationMs;
    private final long refreshExpirationMs;

    public JwtService(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration-ms}") long expirationMs,
            @Value("${jwt.refresh-expiration-ms}") long refreshExpirationMs
    ) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
        this.refreshExpirationMs = refreshExpirationMs;
    }

    public String generateAccessToken(String email, int tokenVersion) {
        return buildToken(email, ACCESS_TYPE, tokenVersion, expirationMs);
    }

    public String generateRefreshToken(String email, int tokenVersion) {
        return buildToken(email, REFRESH_TYPE, tokenVersion, refreshExpirationMs);
    }

    public long getAccessExpirationMs() {
        return expirationMs;
    }

    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Integer extractTokenVersion(String token) {
        return extractClaim(token, c -> c.get(TOKEN_VERSION_CLAIM, Integer.class));
    }

    public boolean isAccessToken(String token) {
        return ACCESS_TYPE.equals(extractClaim(token, c -> c.get(TOKEN_TYPE_CLAIM, String.class)));
    }

    public boolean isRefreshToken(String token) {
        return REFRESH_TYPE.equals(extractClaim(token, c -> c.get(TOKEN_TYPE_CLAIM, String.class)));
    }

    public boolean isValid(String token, String email) {
        return email.equals(extractEmail(token)) && !isExpired(token);
    }

    private String buildToken(String email, String type, int tokenVersion, long ttlMs) {
        Date now = new Date();
        return Jwts.builder()
                .subject(email)
                .claim(TOKEN_TYPE_CLAIM, type)
                .claim(TOKEN_VERSION_CLAIM, tokenVersion)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + ttlMs))
                .signWith(key)
                .compact();
    }

    private boolean isExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    private <T> T extractClaim(String token, Function<Claims, T> resolver) {
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return resolver.apply(claims);
    }
}
