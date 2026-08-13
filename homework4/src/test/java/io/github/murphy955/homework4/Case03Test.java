package io.github.murphy955.homework4;

import io.github.murphy955.homework4.config.RedisConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import redis.clients.jedis.Jedis;

import java.io.IOException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class Case03Test {

    @BeforeEach
    void setUp() {
        cleanup();
        Case03.init();
    }

    @AfterEach
    void tearDown() {
        cleanup();
    }

    private void cleanup() {
        try (Jedis jedis = RedisConfig.getJedis()) {
            for (int i = 1; i <= 10; i++) {
                jedis.del(Case03.BASE_COUNT_KEY + i);
                jedis.del(Case03.BASE_DEMO_KEY + i);
            }
            for (int i = 1; i < 3; i++) {
                jedis.del(Case03.CACHE_KEY + i);
            }
        }
    }

    @Test
    void testInit() {
        try (Jedis jedis = RedisConfig.getJedis()) {
            for (int i = 1; i <= 10; i++) {
                assertEquals("0", jedis.get(Case03.BASE_COUNT_KEY + i));
            }
        }
    }

    @Test
    void testTimingTaskIncrement() {
        new TimingTask().run();

        try (Jedis jedis = RedisConfig.getJedis()) {
            for (int i = 1; i <= 10; i++) {
                assertEquals("1", jedis.get(Case03.BASE_COUNT_KEY + i));
            }
        }
    }

    @Test
    void testBatchWrite() {
        Case03.batchWrite();

        try (Jedis jedis = RedisConfig.getJedis()) {
            for (int i = 1; i <= 10; i++) {
                assertEquals("demo_" + i, jedis.get(Case03.BASE_DEMO_KEY + i));
            }
        }
    }

    @Test
    void testStartTimingTask() throws InterruptedException {
        ScheduledExecutorService scheduler = Case03.startTimingTask();
        try {
            TimeUnit.SECONDS.sleep(10);
        } finally {
            scheduler.shutdown();
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        }

        try (Jedis jedis = RedisConfig.getJedis()) {
            for (int i = 1; i <= 10; i++) {
                String value = jedis.get(Case03.BASE_COUNT_KEY + i);
                assertNotNull(value);
                assertTrue(Long.parseLong(value) >= 10,
                        "计数器 " + Case03.BASE_COUNT_KEY + i + " 应至少增加到 10，实际为 " + value);
                System.out.println("计数器 " + Case03.BASE_COUNT_KEY + i + " 应至少增加到 10，实际为 :"  + value);
            }
        }
    }

    @Test
    void testPreheatCache() {
        try {
            Case03.doPreheatCache();
        } catch (IOException | InterruptedException e) {
            assumeTrue(false, "外部接口暂不可用，跳过该测试: " + e.getMessage());
        }

        try (Jedis jedis = RedisConfig.getJedis()) {
            for (int i = 1; i < 3; i++) {
                assertNotNull(jedis.get(Case03.CACHE_KEY + i),
                        "缓存 key " + Case03.CACHE_KEY + i + " 应存在");
            }
        }
    }
}
