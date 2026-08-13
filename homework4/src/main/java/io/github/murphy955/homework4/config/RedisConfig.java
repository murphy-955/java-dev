package io.github.murphy955.homework4.config;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisConfig {

    private static final String HOST = "106.15.90.163";
    private static final int PORT = 6379;
    private static final int DATABASE = 5;
    private static final int TIMEOUT = 2000;

    private static final JedisPool JEDIS_POOL;

    static {
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(128);
        config.setMaxIdle(64);
        config.setMinIdle(16);
        JEDIS_POOL = new JedisPool(config, HOST, PORT, TIMEOUT, null, DATABASE);
    }

    private RedisConfig() {
    }

    public static Jedis getJedis() {
        return JEDIS_POOL.getResource();
    }

    public static void close() {
        if (JEDIS_POOL != null && !JEDIS_POOL.isClosed()) {
            JEDIS_POOL.close();
        }
    }
}
