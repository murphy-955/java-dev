package io.github.murphy955.homework4.lock;

import io.github.murphy955.homework4.config.RedisConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import redis.clients.jedis.Jedis;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class RedisLockTest {

    private String keyPrefix;

    @BeforeEach
    void setUp() {
        keyPrefix = "test:lock:" + UUID.randomUUID() + ":";
    }

    @AfterEach
    void tearDown() {
        try (Jedis jedis = RedisConfig.getJedis()) {
            jedis.del(keyPrefix + "basic");
            jedis.del(keyPrefix + "expired");
            jedis.del(keyPrefix + "work");
            jedis.del(keyPrefix + "conflict");
            jedis.del(keyPrefix + "wrongToken");
        }
    }

    @Test
    void testTryLockAndUnlock() {
        String key = keyPrefix + "basic";
        String token = RedisLock.createRequestId();

        assertTrue(RedisLock.tryLock(key, token, 5));
        assertTrue(RedisLock.unlock(key, token));
    }

    /**
     * 场景 a：线程 a 获取锁，TTL 5s；等待 7s 后锁已过期；
     * 此时 a 再用自己的 token 去 del lock，由于锁已经不存在（即使存在也是别人的新 token），
     * 解锁必须返回 false，避免误释放他人的锁。
     */
    @Test
    void testExpiredLockCannotBeUnlockedByOldToken() throws InterruptedException {
        String key = keyPrefix + "expired";
        String tokenA = RedisLock.createRequestId();

        assertTrue(RedisLock.tryLock(key, tokenA, 5));

        // 等待 7s，让锁自然过期
        Thread.sleep(7000);

        // 锁已过期，a 的旧 token 不能再解锁成功
        assertFalse(RedisLock.unlock(key, tokenA));
    }

    /**
     * 场景 b：线程 b 获取锁（new token），TTL 10s；
     * b 干活需要 3s；在 b 干活期间锁仍然有效；
     * b 干完活后可以正常用自己的 token 释放锁。
     */
    @Test
    void testLockHeldDuringWork() throws InterruptedException {
        String key = keyPrefix + "work";
        String tokenB = RedisLock.createRequestId();

        assertTrue(RedisLock.tryLock(key, tokenB, 10));

        // 模拟 b 干活 3s
        Thread.sleep(3000);

        // 干活期间锁仍有效，b 可以正常解锁
        assertTrue(RedisLock.unlock(key, tokenB));
    }

    @Test
    void testCannotLockSameKeyWhileHeld() {
        String key = keyPrefix + "conflict";
        String tokenA = RedisLock.createRequestId();
        String tokenB = RedisLock.createRequestId();

        assertTrue(RedisLock.tryLock(key, tokenA, 5));
        assertFalse(RedisLock.tryLock(key, tokenB, 5));

        RedisLock.unlock(key, tokenA);
    }

    @Test
    void testUnlockWithWrongTokenFails() {
        String key = keyPrefix + "wrongToken";
        String tokenA = RedisLock.createRequestId();
        String tokenB = RedisLock.createRequestId();

        assertTrue(RedisLock.tryLock(key, tokenA, 5));
        assertFalse(RedisLock.unlock(key, tokenB));

        RedisLock.unlock(key, tokenA);
    }
}
