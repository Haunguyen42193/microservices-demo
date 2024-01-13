package com.example.productservices.service;


public interface RedisService {
    void addToCache(String key, Object value);

    void addToCache(String key, Object value, long ttlInSeconds);

    Object getFromCache (String key);

    void updateCache(String key, Object value);

    void updateCache(String key, Object value, long ttlInSeconds);

    void deleteCache(String key);
}
