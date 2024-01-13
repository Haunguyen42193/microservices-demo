package com.example.userservices.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@EnableWebSecurity
@Configuration
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {
    private final AuthenticationProvider authenticationProvider;
    private final JWTAthFilterConfig jwtAthFilterConfig;

    @Autowired
    public SecurityConfig(AuthenticationProvider authenticationProvider, JWTAthFilterConfig jwtAthFilterConfig) {
        this.authenticationProvider = authenticationProvider;
        this.jwtAthFilterConfig = jwtAthFilterConfig;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(requests -> (
                (AuthorizeHttpRequestsConfigurer.AuthorizedUrl) requests
                .requestMatchers("/api/user/register", "/api/user/login", "/api/user/verify")
                .permitAll()
                .anyRequest())
                .authenticated())
                .formLogin(Customizer.withDefaults())
                .httpBasic(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAthFilterConfig, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
