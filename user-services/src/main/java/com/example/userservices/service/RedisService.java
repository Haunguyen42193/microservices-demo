package com.example.userservices.service;

public interface RedisService {
    void addToCache(String key, Object values);
    void addToCache(String key, Object value, long ttl);
    void updateCache(String key, Object value);
    void updateCache(String key, Object value, long ttl);
    void deleteCache(String key);
    Object getFromCache(String key);
}
