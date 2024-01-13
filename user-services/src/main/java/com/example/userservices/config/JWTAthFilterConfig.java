package com.example.userservices.config;

import com.example.userservices.model.UserEntity;
import com.example.userservices.service.JwtService;
import com.example.userservices.service.UserService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@Component
@Slf4j
public class JWTAthFilterConfig extends OncePerRequestFilter {

    private final UserService userService;
    private final JwtService jwtService;

    @Autowired
    public JWTAthFilterConfig(UserService userService, JwtService jwtService) {
        this.userService = userService;
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        final String authHeader = request.getHeader(AUTHORIZATION);
        final String username;
        final String token;
        if(authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }
        try {
            request.getMethod();
            token = authHeader.substring(7);
            username = jwtService.extractUsername(token);
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserEntity userDetails = userService.getUserByUserName(username);
                boolean isValidToken = jwtService.isTokenValid(token, userDetails);
                if (isValidToken) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
                    log.info("User get authorized " + authToken.getAuthorities().stream().map(GrantedAuthority::getAuthority).toString());
                    log.info("User get authorized " + authToken.getName());
                    log.info("UserDetail get authorized " + userDetails.getAuthorities().toString());
                    log.info("UserDetail get role " + userDetails.getRole());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
            filterChain.doFilter(request, response);
        } catch (ExpiredJwtException ex) {
            log.info("Expired token JWT!");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        } catch (SignatureException ex) {
            log.info("JWT signature does not match");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        } catch (UnsupportedJwtException ex) {
            log.info("UnsupportedJwtException");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        } catch (MalformedJwtException ex) {
            log.info("JWT strings does not exactly");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        } catch (IllegalArgumentException ex) {
            log.info("Caught an IllegalArgumentException: " + ex.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }
    }
}
