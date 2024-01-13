package com.example.userservices.service;

import com.example.userservices.dto.UserEntityResponseDto;
import com.example.userservices.dto.UserRequestDto;
import com.example.userservices.dto.UserResponseDto;
import com.example.userservices.model.UserEntity;

import java.util.List;

public interface UserService {
    List<UserResponseDto> getAllUser();

    UserResponseDto mapToResponse(UserEntity user);

    UserEntity getUserByUserName(String userName);

    UserResponseDto getUserByUserNameIgnoreCase(String userName);

    List<UserResponseDto> getUserByNameContainIgnoreCase(String username, int page);

    UserResponseDto getUserById(long id);

    UserResponseDto updateUser(long id, UserRequestDto userRequestDto);

    UserResponseDto updatePassword(long id, UserRequestDto userRequestDto);

    List<UserEntityResponseDto> getUserByName(String name);
}
