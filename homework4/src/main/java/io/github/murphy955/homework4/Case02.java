package io.github.murphy955.homework4;

import io.github.murphy955.homework4.config.RedisConfig;
import lombok.AllArgsConstructor;
import lombok.Getter;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

import java.util.List;

/**
 * 同时更新订单状态和当天已支付统计计数
 *
 * @author : 李泽聿
 * @since : 2026:08:13 09:13
 */
public class Case02 {
    public static final String BASE_ORDER_STATUS_KEY = "order_status:";
    public static final String BASE_ORDER_COUNT_KEY = "order_count:";
    public static final String LUA_SCRIPT = """
            local currentStatus = redis.call('GET', KEYS[1])
            if currentStatus == false then
                return -1
            end
            if currentStatus == ARGV[1] then
                return -1
            end
            redis.call('SET', KEYS[1], ARGV[1])
            redis.call('INCR', KEYS[2])
            return 1
            """;

    public static Long updateOrderStatusAndCount(Long id) {
        try (Jedis redis = RedisConfig.getJedis()) {
            String orderStatusKey = BASE_ORDER_STATUS_KEY + id;
            String orderCountKey = BASE_ORDER_COUNT_KEY + id;
            Object result = redis.eval(LUA_SCRIPT, 2, orderStatusKey, orderCountKey, OrderStatus.PAID.getStatus());
            return (Long) result;
        }
    }

    public static Long updateOrderStatusAndCountUseTX(Long id) {
        String orderStatusKey = BASE_ORDER_STATUS_KEY + id;
        String orderCountKey = BASE_ORDER_COUNT_KEY + id;
        try (Jedis redis = RedisConfig.getJedis()) {
            redis.watch(orderStatusKey);
            String currentStatus = redis.get(orderStatusKey);
            if (currentStatus == null) {
                return -1L;
            }
            if (currentStatus.equals(OrderStatus.PAID.getStatus())) {
                return -1L;
            }
            Transaction tx = redis.multi();

            tx.set(orderStatusKey, OrderStatus.PAID.getStatus());
            tx.incr(orderCountKey);
            List<Object> exec = tx.exec();
            if (exec == null) {
                return -1L;
            }
            return (Long) exec.getFirst();
        }
    }

    public static void init(Long id) {
        String orderStatusKey = BASE_ORDER_STATUS_KEY + id;
        String orderCountKey = BASE_ORDER_COUNT_KEY + id;
        try (Jedis redis = RedisConfig.getJedis()) {
            redis.set(orderStatusKey, OrderStatus.PAYING.getStatus());
            redis.set(orderCountKey, String.valueOf(10086));
        }
    }
}

@AllArgsConstructor
@Getter
enum OrderStatus {
    PAYING("PAYING", 0L),
    PAID("PAID", 0L);

    private final String status;
    private final Long number;
}

