package io.github.murphy955.homework4;

import io.github.murphy955.homework4.config.RedisConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import redis.clients.jedis.Jedis;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class RedisSwapPenetrationTest {

    private String keyPrefix;

    @BeforeEach
    void setUp() {
        keyPrefix = "test:penetration:" + UUID.randomUUID() + ":";
        try (Jedis jedis = RedisConfig.getJedis()) {
            jedis.del("key1");
            jedis.del("key2");
        }
    }

    @AfterEach
    void tearDown() {
        try (Jedis jedis = RedisConfig.getJedis()) {
            jedis.del(keyPrefix + "cachedKey");
            jedis.del(keyPrefix + "missingKey");
            jedis.del("key1");
            jedis.del("key2");
        }
    }

    @Test
    void testCacheHit() {
        String key = keyPrefix + "cachedKey";
        try (Jedis jedis = RedisConfig.getJedis()) {
            jedis.set(key, "cachedValue");
        }

        String result = RedisSwapPenetration.getMsg(key);

        assertEquals("cachedValue", result);
    }

    @Test
    void testDatabaseHitAndCacheWriteBack() {
        String key = "key1";

        String result = RedisSwapPenetration.getMsg(key);

        assertEquals("value1", result);
        try (Jedis jedis = RedisConfig.getJedis()) {
            assertEquals("value1", jedis.get(key));
        }
    }

    @Test
    void testCachePenetrationWithNullValue() {
        String key = keyPrefix + "missingKey";

        String result = RedisSwapPenetration.getMsg(key);

        assertEquals("NULL_VALUE", result);
        try (Jedis jedis = RedisConfig.getJedis()) {
            assertEquals("NULL_VALUE", jedis.get(key));
        }
    }

    @Test
    void testNullValueReturnsDirectlyOnSecondCall() {
        String key = keyPrefix + "missingKey";

        RedisSwapPenetration.getMsg(key);

        String secondResult = RedisSwapPenetration.getMsg(key);

        assertEquals("NULL_VALUE", secondResult);
    }
}
