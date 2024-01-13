package com.example.userservices.service.impl;

import com.example.userservices.service.RedisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class RedisServiceImpl implements RedisService {
    private final RedisTemplate<String, Object> redisTemplate;

    @Autowired
    public RedisServiceImpl(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void addToCache(String key, Object values) {
        redisTemplate.opsForValue().set(key, values);
        log.info("Add to cache with key: " + key);
    }

    @Override
    public void addToCache(String key, Object values, long ttl) {
        redisTemplate.opsForValue().set(key, values);
        redisTemplate.expire(key, ttl, TimeUnit.SECONDS);
        log.info("Add to cache with key: " + key + ", TTL: " + ttl);
    }

    @Override
    public void updateCache(String key, Object values) {
        redisTemplate.opsForValue().set(key, values);
        log.info("Update cache with key: " + key);
    }

    @Override
    public void updateCache(String key, Object values, long ttl) {
        redisTemplate.opsForValue().set(key, values);
        redisTemplate.expire(key, ttl, TimeUnit.SECONDS);
        log.info("Update cache with key: " + key + ", TTL: " + ttl);
    }

    @Override
    public void deleteCache(String key) {
        redisTemplate.delete(key);
        log.info("Delete cache with key: " + key);
    }

    @Override
    public Object getFromCache(String key) {
        return redisTemplate.opsForValue().get(key);
    }
}
