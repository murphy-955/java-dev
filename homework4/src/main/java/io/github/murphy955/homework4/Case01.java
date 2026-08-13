package io.github.murphy955.homework4;

import io.github.murphy955.homework4.config.RedisConfig;
import redis.clients.jedis.Jedis;

public class Case01 {

    /**
     * Lua 脚本：根据商品 id 原子地查询并扣减库存
     *
     * 参数说明：
     *   KEYS[1]：商品库存对应的 Redis key
     *
     * 返回值：
     *   -1：商品库存 key 不存在
     *    0：库存不足，扣减失败
     *   >0：扣减成功，返回扣减后的剩余库存
     */
    private static final String LUA_SCRIPT =
            "local stock = tonumber(redis.call('GET', KEYS[1])); " +
            "if stock == nil then " +
            "    return -1; " +
            "end; " +
            "if stock <= 0 then " +
            "    return 0; " +
            "end; " +
            "redis.call('DECR', KEYS[1]); " +
            "return stock - 1;";

    public static long deductStock(String productId) {
        String stockKey = "stock:" + productId;
        try (Jedis jedis = RedisConfig.getJedis()) {
            Object result = jedis.eval(LUA_SCRIPT, 1, stockKey);
            System.out.println("扣减库存结果：" + result);
            return ((Long) result);
        }
    }

    public static void initStock(String productId, long stock) {
        String stockKey = "stock:" + productId;
        try (Jedis jedis = RedisConfig.getJedis()) {
            jedis.set(stockKey, String.valueOf(stock));
        }
    }

    /**
     * 查询指定商品的当前库存
     *
     * @param productId 商品 id
     * @return 当前库存，若商品不存在则返回 null
     */
    public static String queryStock(String productId) {
        String stockKey = "stock:" + productId;
        try (Jedis jedis = RedisConfig.getJedis()) {
            return jedis.get(stockKey);
        }
    }

}
