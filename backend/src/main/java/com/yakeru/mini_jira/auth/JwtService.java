package com.yakeru.mini_jira.auth;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.yakeru.mini_jira.user.User;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class JwtService {

    private final SecretKey signingKey;
    private final long expirationHours;

    public JwtService(
        @Value("${mini-jira.jwt.secret}") String secret, 
        @Value("${mini-jira.jwt.expiration-hours:24}") long expirationHours) {

        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationHours = expirationHours;
    }

    public String generateToken(User user) {

        Instant now = Instant.now();
        return Jwts.builder()
            .subject(user.getId().toString())
            .claim("username", user.getUsername())
            .claim("email",    user.getEmail())
            .issuedAt(Date.from(now))
            .expiration(Date.from(now.plus(expirationHours, ChronoUnit.HOURS)))
            .signWith(signingKey)
            .compact();
    }

    public UUID extractUserId(String token) {
        
        String subject = Jwts.parser()
            .verifyWith(signingKey)
            .build()
            .parseSignedClaims(token)
            .getPayload()
            .getSubject();

        return UUID.fromString(subject);
    }
}
