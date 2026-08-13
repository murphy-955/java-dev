package io.github.murphy955.homework4;

import io.github.murphy955.homework4.config.RedisConfig;
import lombok.AllArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class Case01Test {

    private String productId;

    @BeforeEach
    void setUp() {
        productId = "product:" + UUID.randomUUID();
    }

    @AfterEach
    void tearDown() {
        try (Jedis jedis = RedisConfig.getJedis()) {
            jedis.del("stock:" + productId);
        }
    }

    @Test
    void testDeductStockSuccess() {
        Case01.initStock(productId, 10L);

        long result = Case01.deductStock(productId);

        assertEquals(9L, result);
        assertEquals("9", Case01.queryStock(productId));
    }

    @Test
    void testDeductStockUntilEmpty() {
        Case01.initStock(productId, 3L);

        assertEquals(2L, Case01.deductStock(productId));
        assertEquals(1L, Case01.deductStock(productId));
        assertEquals(0L, Case01.deductStock(productId));
        assertEquals(0L, Case01.deductStock(productId));
        assertEquals("0", Case01.queryStock(productId));
    }

    @Test
    void testDeductStockNotExists() {
        long result = Case01.deductStock(productId);

        assertEquals(-1L, result);
    }

    @Test
    void testDeductStockAtomicity() {
        Case01.initStock(productId, 100L);

        List<Thread> threads = new ArrayList<>();

        for (int i = 0; i < 500; i++) {
            Task task = new Task(productId);
            Thread vt = Thread.ofVirtual()
                    .name("task-" + i)
                    .unstarted(task);
            threads.add(vt);  // 加入列表
            vt.start();
        }

        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e){
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            }
        }
        System.out.println(Case01.queryStock(productId));
    }
}
@AllArgsConstructor
class Task implements Runnable {
    private String productId;

    @Override
    public void run() {
        Case01.deductStock(this.productId);
    }
}