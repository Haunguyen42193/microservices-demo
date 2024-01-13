package com.example.userservices.service.impl;

import com.example.userservices.model.UserEntity;
import com.example.userservices.service.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
@Slf4j
public class JwtServiceImpl implements JwtService {
    private static final String SECRET_KEY = "dsRrAqnv6uwjw4BT6XZAiRb5c3ecXRCjX8gk7uq/t2NhvWrM+BeQLu3mCNN1pQ30/LE++n4OOwKAj2bEJiqT1mzP+5fZw4OAi55WJEfMlIZQzzmPHFXl/zPZ/p+OEWa+JFrs5J3WZYLajv9QAr775yRc21WKR1dmgrLl41RMND0A+wpsUHxcsCtlQDGGExtBJWs60h8uRwRZ8wZd3MeLarE9J/OGQ5exZYWSv3lUOf/u3vjKe7995NsXOVAYL8yNOWpF+z2zD6X4wxHBbjcl0ZKsbwjhNxrmsAesEVQL7arhmVtD9vedus7BVTjXS3erOHAKjVBlnYZFfYVjrzW/da8kNssPVNBKB4HnMZLmbAU=";

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaims(token, Claims::getExpiration);
    }

    public String extractUsername(String token) {
        return extractClaims(token, Claims::getSubject);
    }

    public String generateToken(UserEntity userDetails) {
        Map<String, Object> map = new HashMap<>();
        Collection<String> authority;
        authority = userDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();
        String role = authority.stream().toList().toString();
        map.put("role", role);
        return generateToken(map, userDetails);
    }

    public String generateToken(Map<String, Object> extractClaims, UserEntity userDetails) {
        return Jwts
                .builder()
                .setClaims(extractClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24))
                .signWith(getSignInKey()).compact();
    }

    public <T> T extractClaims(String token, Function<Claims, T> claimsTFunction) {
        final Claims claims = extractAllClaims(token);
        return claimsTFunction.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
