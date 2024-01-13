package com.example.userservices.service.impl;

import com.example.userservices.dto.AuthenticationRequestDto;
import com.example.userservices.dto.AuthenticationResponseDto;
import com.example.userservices.dto.UserRequestDto;
import com.example.userservices.exception.UserNotFoundException;
import com.example.userservices.exception.UsernameExistedException;
import com.example.userservices.model.Role;
import com.example.userservices.model.UserEntity;
import com.example.userservices.repository.UserRepository;
import com.example.userservices.service.AuthenticateService;
import com.example.userservices.service.JwtService;
import com.example.userservices.service.RedisService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@Slf4j
public class AuthenticateServiceImpl implements AuthenticateService {
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final RedisService redisService;

    @Autowired
    public AuthenticateServiceImpl(UserRepository userRepository, JwtService jwtService, AuthenticationManager authenticationManager, PasswordEncoder passwordEncoder, RedisService redisService) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.passwordEncoder = passwordEncoder;
        this.redisService = redisService;
    }

    public AuthenticationResponseDto authenticate(AuthenticationRequestDto request) {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                    request.getUsername(),
                    request.getPassword()
            ));
        } catch (AuthenticationException ex) {
            throw new UserNotFoundException("Username or password are incorrect");
        }

        UserEntity user = userRepository.findByUsername(request.getUsername());
        if (user != null) {
            String token = jwtService.generateToken(user);
            return AuthenticationResponseDto.builder().token(token).build();
        } else {
            throw new UserNotFoundException("User not found");
        }
    }

    public AuthenticationResponseDto createUser(UserRequestDto userRequestDto) {
        UserEntity user = userRepository.findByUsername(userRequestDto.getUsername());
        if (Objects.nonNull(user)) {
            log.info("Username already existed! -> " + userRequestDto.getUsername());
            throw new UsernameExistedException("Username already existed");
        }
        user = userRepository.findByEmail(userRequestDto.getEmail());
        if (Objects.nonNull(user)) {
            log.info("Email already existed! -> " + userRequestDto.getUsername());
            throw new UsernameExistedException("Email already existed");
        }
        user = UserEntity.builder()
                .role(Role.ROLE_USER)
                .email(userRequestDto.getEmail())
                .name(userRequestDto.getName())
                .username(userRequestDto.getUsername())
                .password(passwordEncoder.encode(userRequestDto.getPassword()))
                .build();
        log.info("Create user (register) -> " + user.getEmail());
        userRepository.save(user);
        String jwtToken = jwtService.generateToken(user);
        redisService.deleteCache("users");
        return AuthenticationResponseDto.builder().token(jwtToken).build();
    }

    public boolean verifyUser(String token) {
        if (token.startsWith("Bearer "))
            token = token.substring(7);
        try {
            String username = jwtService.extractUsername(token);
            if (username != null && SecurityContextHolder.getContext().getAuthentication() != null) {
                UserDetails userDetails = userRepository.findByUsername(username);
                return jwtService.isTokenValid(token, userDetails);
            }
            return false;
        } catch (ExpiredJwtException ex) {
            log.info("Expired token JWT!");
            return false;
        } catch (SignatureException ex) {
            log.info("JWT signature does not match");
            return false;
        } catch (UnsupportedJwtException ex) {
            log.info("UnsupportedJwtException");
            return false;
        } catch (MalformedJwtException ex) {
            log.info("JWT strings does not exactly form");
            return false;
        } catch (IllegalArgumentException ex) {
            log.info("Caught an IllegalArgumentException: " + ex.getMessage());
            return false;
        }
    }
}
