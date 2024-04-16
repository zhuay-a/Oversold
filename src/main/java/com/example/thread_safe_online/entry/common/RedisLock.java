package com.example.thread_safe_online.entry.common;

import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.concurrent.TimeUnit;

public class RedisLock implements ILock {

    private final StringRedisTemplate stringRedisTemplate;

    public RedisLock(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public boolean tryLock(int id) {
        Boolean aBoolean = stringRedisTemplate.opsForValue().setIfAbsent(
                Context.REDIS_LOCK_HEAD + id,
                Thread.currentThread().toString(),
                10,
                TimeUnit.SECONDS
        );
        return Boolean.TRUE.equals(aBoolean);
    }

    @Override
    public void unLock(int id) {
        stringRedisTemplate.delete(Context.REDIS_LOCK_HEAD + id);
    }
}
