package com.example.userservices.dto;

import com.example.userservices.model.Role;

public interface UserEntityResponseDto {
    Long getId();
    String getName();
    String getUsername();
    String getEmail();
    Role getRole();
}
