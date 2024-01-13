package com.example.userservices.service;

import com.example.userservices.dto.AuthenticationRequestDto;
import com.example.userservices.dto.AuthenticationResponseDto;
import com.example.userservices.dto.UserRequestDto;

public interface AuthenticateService {
    AuthenticationResponseDto authenticate(AuthenticationRequestDto request);

    AuthenticationResponseDto createUser(UserRequestDto userRequestDto);

    boolean verifyUser(String token);
}
