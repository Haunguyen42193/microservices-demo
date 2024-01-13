package com.example.orderservices.service.impl;

import com.example.orderservices.service.RedisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class RedisServiceImpl implements RedisService {
    private final RedisTemplate<String, Object> redisTemplate;
    private static final String LOG_STRING1 = ", value: ";

    @Autowired
    public RedisServiceImpl(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void addToCache(String key, Object value) {
        redisTemplate.opsForValue().set(key, value);
        log.info("REDIS add to cache -> key: " + key + LOG_STRING1 + value);
    }

    public void addToCache(String key, Object value, long ttlInSeconds) {
        redisTemplate.opsForValue().set(key, value);
        redisTemplate.expire(key, ttlInSeconds, TimeUnit.SECONDS);
        log.info("REDIS add to cache with ttl -> key: " + key + LOG_STRING1 + value);
    }

    public Object getFromCache(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    public void updateCache(String key, Object value) {
        redisTemplate.opsForValue().set(key, value);
        log.info("REDIS update to cache -> key: " + key + LOG_STRING1 + value);
    }

    public void updateCache(String key, Object value, long ttlInSeconds) {
        redisTemplate.opsForValue().set(key, value);
        redisTemplate.expire(key, ttlInSeconds, TimeUnit.SECONDS);
        log.info("REDIS update to cache -> key: " + key + LOG_STRING1 + value);
    }

    public void deleteCache(String key) {
        redisTemplate.delete(key);
        log.info("REDIS delete to cache -> key: " + key);
    }
}
