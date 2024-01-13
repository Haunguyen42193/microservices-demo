package com.example.userservices.service.impl;

import com.example.userservices.dto.UserEntityResponseDto;
import com.example.userservices.dto.UserRequestDto;
import com.example.userservices.dto.UserResponseDto;
import com.example.userservices.exception.UserNotFoundException;
import com.example.userservices.model.UserEntity;
import com.example.userservices.repository.UserRepository;
import com.example.userservices.service.RedisService;
import com.example.userservices.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;

@Service
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RedisService redisService;
    private static final String USERS = "users";
    private static final String USER = "user_";
    private static final String USER_NOT_FOUND = "Not found user";

    @Autowired
    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, RedisService redisService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.redisService = redisService;
    }

    @Transactional
    @SuppressWarnings("unchecked")
    public List<UserResponseDto> getAllUser() {
        List<UserResponseDto> list = (List<UserResponseDto>) redisService.getFromCache(USERS);
        if (list != null)
            return list;
        Pageable pageable = PageRequest
                .of(0,20, Sort
                        .by("name")
                        .descending()
                        .and(Sort.by("id")));
        Page<UserEntity> entityList = userRepository.findAll(pageable);
        log.info("page size " + entityList.getSize());
        log.info("Get all user -> " + entityList.toList().size());
        List<UserResponseDto> responseDtos = entityList.stream().map(this::mapToResponse).toList();
        redisService.addToCache(USERS, responseDtos);
        return responseDtos;
    }

    public UserEntity getUserByUserName(String userName) {
        UserEntity user = userRepository.findByUsername(userName);
        if (user != null)
            return user;
        throw new UserNotFoundException(USER_NOT_FOUND);
    }

    @Override
    public UserResponseDto getUserByUserNameIgnoreCase(String userName) {
        UserEntity user = userRepository.findByUsernameIgnoreCase(userName);
        if (user != null)
            return mapToResponse(user);
        throw new UserNotFoundException(USER_NOT_FOUND);
    }

    @Override
    public List<UserResponseDto> getUserByNameContainIgnoreCase(String userName, int page) {
        Pageable pageable = PageRequest
                .of(page,10, Sort
                        .by("name")
                        .descending());
        List<UserEntity> users = userRepository.findUserEntitiesByNameContainingIgnoreCase(userName, pageable);
        if (!users.isEmpty())
            return users.stream().map(this::mapToResponse).toList();
        throw new UserNotFoundException(USER_NOT_FOUND);
    }

    @Override
    public UserResponseDto getUserById(long id) {
        UserResponseDto userResponseDto = (UserResponseDto) redisService.getFromCache(USER + id);
        if (userResponseDto != null) return userResponseDto;
        UserEntity user = userRepository.findById(id).orElse(null);
        if (user != null) {
            userResponseDto = mapToResponse(user);
            log.info("Get user by id -> " + id);
            redisService.addToCache(USER + userResponseDto.getId(), userResponseDto, 600);
            redisService.deleteCache(USERS);
            return userResponseDto;
        }
        throw new UserNotFoundException(USER_NOT_FOUND);
    }

    @Override
    public UserResponseDto updateUser(long id, UserRequestDto userRequestDto) {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        log.info(request.getHeader("authorize"));
        UserEntity user = userRepository.findById(id).orElse(null);
        if (user != null) {
            user.setName(userRequestDto.getName());
            user.setEmail(userRequestDto.getEmail());
            userRepository.save(user);
            UserResponseDto userResponseDto = mapToResponse(user);
            redisService.addToCache(USER + userResponseDto.getId(), userResponseDto, 600);
            redisService.deleteCache(USERS);
            return userResponseDto;
        }
        throw new UserNotFoundException(USER_NOT_FOUND);
    }

    @Override
    public UserResponseDto updatePassword(long id, UserRequestDto userRequestDto) {
        UserEntity user = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND));
        String password = passwordEncoder.encode(userRequestDto.getPassword());
        user.setPassword(password);
        UserEntity person = new UserEntity();
        person.setName("Dave");

        ExampleMatcher matcher = ExampleMatcher.matching()
                .withIgnorePaths("email")
                .withIncludeNullValues()
                .withStringMatcher(ExampleMatcher.StringMatcher.ENDING);

        Example<UserEntity> example = Example.of(person, matcher);
        userRepository.findOne(example);

        userRepository.save(user);
        return mapToResponse(user);
    }

    @Override
    public List<UserEntityResponseDto> getUserByName(String name) {
        List<UserEntityResponseDto> user = userRepository.findAllByName(name);
        if (!user.isEmpty())
            return user;
        throw new UserNotFoundException(USER_NOT_FOUND);
    }

    public UserResponseDto mapToResponse (UserEntity user) {
        return UserResponseDto.builder()
                .id(user.getId())
                .role(user.getRole())
                .email(user.getEmail())
                .name(user.getName())
                .username(user.getUsername())
                .build();
    }
}
