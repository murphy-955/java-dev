package io.github.murphy955.homework4;

import io.github.murphy955.homework4.config.RedisConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import redis.clients.jedis.Jedis;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class Case02Test {

    private Long orderId;

    @BeforeEach
    void setUp() {
        orderId = Math.abs(UUID.randomUUID().getLeastSignificantBits());
    }

    @AfterEach
    void tearDown() {
        try (Jedis jedis = RedisConfig.getJedis()) {
            jedis.del(Case02.BASE_ORDER_STATUS_KEY + orderId);
            jedis.del(Case02.BASE_ORDER_COUNT_KEY + orderId);
        }
    }

    @Test
    void testUpdateStatusAndCountSuccess() {
        Case02.init(orderId);

        Long result = Case02.updateOrderStatusAndCount(orderId);

        assertEquals(1L, result);
        try (Jedis jedis = RedisConfig.getJedis()) {
            assertEquals(OrderStatus.PAID.getStatus(), jedis.get(Case02.BASE_ORDER_STATUS_KEY + orderId));
            assertEquals("10087", jedis.get(Case02.BASE_ORDER_COUNT_KEY + orderId));
        }
    }

    @Test
    void testUpdateWithoutInit() {
        Long result = Case02.updateOrderStatusAndCount(orderId);

        assertEquals(-1L, result);
    }

    @Test
    void testUpdateAlreadyPaid() {
        Case02.init(orderId);
        assertEquals(1L, Case02.updateOrderStatusAndCount(orderId));

        Long result = Case02.updateOrderStatusAndCount(orderId);

        assertEquals(-1L, result);
        try (Jedis jedis = RedisConfig.getJedis()) {
            assertEquals(OrderStatus.PAID.getStatus(), jedis.get(Case02.BASE_ORDER_STATUS_KEY + orderId));
            assertEquals("10087", jedis.get(Case02.BASE_ORDER_COUNT_KEY + orderId));
        }
    }
}
