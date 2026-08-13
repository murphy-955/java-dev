package io.github.murphy955.homework4;

import io.github.murphy955.homework4.config.RedisConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class Case04Test {

    @BeforeEach
    void setUp() {
        cleanup();
        Case04.init();
    }

    @AfterEach
    void tearDown() {
        cleanup();
    }

    private void cleanup() {
        try (Jedis jedis = RedisConfig.getJedis()) {
            jedis.del(Case04.USER_SET);
        }
    }

    @Test
    void testInit() {
        try (Jedis jedis = RedisConfig.getJedis()) {
            assertEquals(100L, jedis.zcard(Case04.USER_SET));
            for (int i = 0; i < 100; i++) {
                assertEquals(0.0, jedis.zscore(Case04.USER_SET, Case04.BASE_USER_KEY + i), 0.0001);
            }
        }
    }

    @Test
    void testAddScore() {
        Double newScore = Case04.addScore(1L, 10.5);

        assertEquals(10.5, newScore, 0.0001);
        try (Jedis jedis = RedisConfig.getJedis()) {
            assertEquals(10.5, jedis.zscore(Case04.USER_SET, Case04.BASE_USER_KEY + 1), 0.0001);
        }
    }

    @Test
    void testAddScoreAccumulate() {
        assertEquals(10.0, Case04.addScore(2L, 10.0), 0.0001);
        assertEquals(25.0, Case04.addScore(2L, 15.0), 0.0001);

        try (Jedis jedis = RedisConfig.getJedis()) {
            assertEquals(25.0, jedis.zscore(Case04.USER_SET, Case04.BASE_USER_KEY + 2), 0.0001);
        }
    }

    @Test
    void testAddScoreToNonExistentUser() {
        Double newScore = Case04.addScore(200L, 5.5);

        assertEquals(5.5, newScore, 0.0001);
        try (Jedis jedis = RedisConfig.getJedis()) {
            assertEquals(5.5, jedis.zscore(Case04.USER_SET, Case04.BASE_USER_KEY + 200), 0.0001);
        }
    }

    @Test
    void testAddScoreAtomicity() throws InterruptedException {
        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            Thread thread = Thread.ofVirtual().unstarted(() -> Case04.addScore(50L, 1.0));
            threads.add(thread);
            thread.start();
        }
        for (Thread thread : threads) {
            thread.join();
        }

        try (Jedis jedis = RedisConfig.getJedis()) {
            assertEquals(100.0, jedis.zscore(Case04.USER_SET, Case04.BASE_USER_KEY + 50), 0.0001);
        }
    }

    @Test
    void testGetRankByScore() {
        Case04.addScore(1L, 10.0);
        Case04.addScore(2L, 20.0);
        Case04.addScore(3L, 30.0);

        List<String> rank15To25 = Case04.getRankByScore(15, 25, Case04.USER_SET);
        List<String> rank25To35 = Case04.getRankByScore(25, 35, Case04.USER_SET);
        List<String> rank5To15 = Case04.getRankByScore(5, 15, Case04.USER_SET);

        assertEquals(List.of(Case04.BASE_USER_KEY + 2), rank15To25);
        assertEquals(List.of(Case04.BASE_USER_KEY + 3), rank25To35);
        assertEquals(List.of(Case04.BASE_USER_KEY + 1), rank5To15);
    }

    @Test
    void testGetRange() {
        Case04.addScore(1L, 10.0);
        Case04.addScore(2L, 20.0);
        Case04.addScore(3L, 30.0);

        List<String> top3 = Case04.getRange(97, 99, Case04.USER_SET);

        assertEquals(List.of(
                Case04.BASE_USER_KEY + 1,
                Case04.BASE_USER_KEY + 2,
                Case04.BASE_USER_KEY + 3
        ), top3);
    }
}
