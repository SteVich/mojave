package com.mojave.authservice.security;

import com.mojave.authservice.util.properties.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class JwtTokenProvider {

    public static final String ROLE_TOKEN_CLAIM = "role";
    JwtProperties properties;

    public String extractUsername(String authToken) {
        String username = null;
        try {
            username = getClaimsFromToken(authToken).getSubject();
        } catch (Exception e) {
            // do nothing
        }

        return username;
    }

    public Claims getClaimsFromToken(String authToken) {
        String key = Base64.getEncoder().encodeToString(properties.getSecretKey().getBytes());
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(authToken)
                .getBody();
    }

    public String generateToken(Authentication authentication, Long expirationSeconds) {
        UserPrincipal user = (UserPrincipal) authentication.getPrincipal();

        HashMap<String, Object> claims = new HashMap<>();
        claims.put(ROLE_TOKEN_CLAIM, List.of(user.getAuthorities()).get(0));

        Date creationDate = new Date();
        Date expirationDate = new Date(creationDate.getTime() + expirationSeconds * 1000);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(user.getUsername())
                .setIssuedAt(creationDate)
                .setExpiration(expirationDate)
                .signWith(Keys.hmacShaKeyFor(properties.getSecretKey().getBytes()))
                .compact();
    }

    public String generateTokenFromUsername(String username, Long expiration) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(expiryDate)
                .signWith(Keys.hmacShaKeyFor(properties.getSecretKey().getBytes()))
                .compact();
    }

    public boolean validateAccessToken(String accessToken) {
        return getClaimsFromToken(accessToken)
                .getExpiration()
                .after(new Date());
    }

    public boolean validateRefreshToken(String refreshToken) {
        Long expirationRefreshToken = getClaimsFromToken(refreshToken).getExpiration().getTime();

        return expirationRefreshToken - getConstantExpirationRefreshToken() < 0;
    }

    public Long getConstantExpirationRefreshToken() {
        return new Date().getTime() + properties.getExpirationRefreshToken();
    }
}