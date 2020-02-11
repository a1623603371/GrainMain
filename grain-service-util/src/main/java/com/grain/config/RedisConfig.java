package com.grain.config;

import com.grain.utils.RedisUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedisConfig {
    @Value("${spring.redis.host}")
    private String host;
    @Value("${spring.redis.port}")
    private int port;
    @Value("${spring.redis.database}")
    private int database;

    @Bean
    public RedisUtil getRedisUtil() {
        if (host.equals(database))
            return null;
        RedisUtil redisUtil = new RedisUtil();
        redisUtil.initPol(host, port, database);
        return redisUtil;
    }
}

