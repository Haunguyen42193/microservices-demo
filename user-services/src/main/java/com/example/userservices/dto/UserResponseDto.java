package com.example.userservices.dto;

import com.example.userservices.model.Role;
import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserResponseDto implements Serializable {
    private Long id;
    private String name;
    private String username;
    private String email;
    private Role role;
}
