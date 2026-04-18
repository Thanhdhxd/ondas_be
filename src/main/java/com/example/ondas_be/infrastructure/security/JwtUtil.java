package com.example.ondas_be.infrastructure.security;

import com.example.ondas_be.domain.entity.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class JwtUtil {

    private static final String TYPE_CLAIM = "type";
    private static final String ACCESS_TOKEN_TYPE = "access";
    private static final String REFRESH_TOKEN_TYPE = "refresh";

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    @Value("${jwt.refresh-expiration}")
    private long refreshExpiration;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(String email, Set<Role> roles) {
        return generateAccessToken(email, roles);
    }

    public String generateAccessToken(String email, Set<Role> roles) {
        return buildToken(email, roles, ACCESS_TOKEN_TYPE, expiration);
    }

    public String generateRefreshToken(String email, Set<Role> roles) {
        return buildToken(email, roles, REFRESH_TOKEN_TYPE, refreshExpiration);
    }

    private String buildToken(String email, Set<Role> roles, String tokenType, long tokenExpirationMs) {
        String rolesStr = roles.stream()
                .map(Role::name)
                .collect(Collectors.joining(","));

        return Jwts.builder()
                .subject(email)
                .claim("roles", rolesStr)
                .claim(TYPE_CLAIM, tokenType)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + tokenExpirationMs))
                .signWith(getSigningKey())
                .compact();
    }

    public String extractEmail(String token) {
        return getClaims(token).getSubject();
    }

    public Date extractExpiration(String token) {
        return getClaims(token).getExpiration();
    }

    public boolean isTokenValid(String token) {
        return isAccessTokenValid(token);
    }

    public boolean isAccessTokenValid(String token) {
        return isTokenValidByType(token, ACCESS_TOKEN_TYPE);
    }

    public boolean isRefreshTokenValid(String token) {
        return isTokenValidByType(token, REFRESH_TOKEN_TYPE);
    }

    private boolean isTokenValidByType(String token, String expectedTokenType) {
        try {
            Claims claims = getClaims(token);
            String tokenType = claims.get(TYPE_CLAIM, String.class);
            if (ACCESS_TOKEN_TYPE.equals(expectedTokenType) && tokenType == null) {
                return !claims.getExpiration().before(new Date());
            }
            if (!expectedTokenType.equals(tokenType)) {
                return false;
            }
            return !claims.getExpiration().before(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
