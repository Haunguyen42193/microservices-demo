package com.example.userservices.controller;

import com.example.userservices.dto.*;
import com.example.userservices.exception.PasswordNullException;
import com.example.userservices.exception.UsernameNullException;
import com.example.userservices.service.AuthenticateService;
import com.example.userservices.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/user")
@Slf4j
public class UserController {
    private final UserService userService;
    private final AuthenticateService authenticate;

    @Autowired
    public UserController(UserService userService, AuthenticateService authenticate) {
        this.userService = userService;
        this.authenticate = authenticate;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponseDto> registerUser(@RequestBody UserRequestDto user) {
        if (Objects.isNull(user.getUsername()))
            throw new UsernameNullException("Username must be not null");
        if (Objects.isNull(user.getPassword()))
            throw new PasswordNullException("Password must be not null");
        return ResponseEntity.status(HttpStatus.OK).body(authenticate.createUser(user));
    }

    @GetMapping("")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponseDto>> getAllUser() {
        return ResponseEntity.status(HttpStatus.OK).body(userService.getAllUser());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<UserResponseDto> getUserById(@PathVariable long id) {
        return ResponseEntity.status(HttpStatus.OK).body(userService.getUserById(id));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponseDto> authenticate(@RequestBody AuthenticationRequestDto request) {
        if(Objects.isNull(request.getUsername())) return ResponseEntity.badRequest().body(null);
        return ResponseEntity.ok().body(authenticate.authenticate(request));
    }

    @PostMapping("/verify")
    public boolean verifyUser(@RequestBody String token) {
        log.info("Get token -> " + token);
        return authenticate.verifyUser(token);
    }

    @PutMapping("/update-user/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<UserResponseDto> updateUser(@PathVariable long id, @RequestBody UserRequestDto userRequestDto) {
        return ResponseEntity.status(HttpStatus.OK).body(userService.updateUser(id, userRequestDto));
    }

    @PutMapping("/update-password/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<UserResponseDto> updatePassword(@PathVariable long id, @RequestBody UserRequestDto userRequestDto) {
        return ResponseEntity.status(HttpStatus.OK).body(userService.updatePassword(id, userRequestDto));
    }

    @GetMapping("/get-by-username")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponseDto> getUserByUserName(@RequestParam String username) {
        return ResponseEntity.status(HttpStatus.OK).body(userService.getUserByUserNameIgnoreCase(username));
    }

    @GetMapping("/get-by-name")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<UserResponseDto>> getUserByName(@RequestParam String name, @RequestParam int page) {
        return ResponseEntity.status(HttpStatus.OK).body(userService.getUserByNameContainIgnoreCase(name, page));
    }

    @GetMapping("/get-all-by-name")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<UserEntityResponseDto>> getUserByName(@RequestParam String name) {
        return ResponseEntity.status(HttpStatus.OK).body(userService.getUserByName(name));
    }
}
