package com.example.userservices.service;

import com.example.userservices.model.UserEntity;
import io.jsonwebtoken.Claims;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Map;
import java.util.function.Function;

public interface JwtService {
    boolean isTokenValid(String token, UserDetails userDetails);

    String extractUsername(String token);

    String generateToken(UserEntity userDetails);

    String generateToken(Map<String, Object> extractClaims, UserEntity userDetails);

    <T> T extractClaims(String token, Function<Claims, T> claimsTFunction);

}
