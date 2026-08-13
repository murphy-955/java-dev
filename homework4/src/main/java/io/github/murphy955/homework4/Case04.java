package io.github.murphy955.homework4;

import io.github.murphy955.homework4.config.RedisConfig;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;

import java.util.List;

/**
 * 模拟一个实时积分榜
 *
 * @author : 李泽聿
 * @since : 2026:08:13 10:56
 */
public class Case04 {
    public static final String USER_SET = "user_set";
    public static final String BASE_USER_KEY = "user_:";
    public static final String LUA_SCRIPT = """
            local currentScore = redis.call('ZSCORE', KEYS[1], ARGV[1])
            if currentScore == false then
                currentScore = 0
            else
                currentScore = tonumber(currentScore)
            end
            local newScore = currentScore + tonumber(ARGV[2])
            redis.call('ZADD', KEYS[1], newScore, ARGV[1])
            return tostring(newScore)
            """;

    public static void init() {
        try (Jedis redis = RedisConfig.getJedis()) {
            Pipeline pipelined = redis.pipelined();
            for (int i = 0; i < 100; i++) {
                pipelined.zadd(USER_SET, 0.00, BASE_USER_KEY + i);
            }
            pipelined.sync();
        }
    }

    public static Double addScore(Long id, Double score) {
        try (Jedis redis = RedisConfig.getJedis()) {
            String member = BASE_USER_KEY + id;
            Object result = redis.eval(LUA_SCRIPT, 1, USER_SET, member, String.valueOf(score));
            return Double.parseDouble(result.toString());
        }
    }

    /**
     * 获取指定积分范围内的用户排名
     *
     * @param min 开始排名
     * @param max 结束排名
     * @param set 集合名称
     * @return java.util.List<java.lang.String>
     * @author 李泽聿
     * @since 2026-08-13 11:12
     */
    public static List<String> getRankByScore(int min, int max, String set) {
        try (Jedis redis = RedisConfig.getJedis()) {
            return redis.zrangeByScore(set, min, max);
        }
    }

    /**
     * @param start 开始排名
     * @param end 结束排名
     * @param set 集合名称
     * @return java.util.List<java.lang.String>
     * @author 李泽聿
     * @since 2026-08-13 11:17
     */
    public static List<String> getRange(int start, int end, String set) {
        try (Jedis redis = RedisConfig.getJedis()) {
            return redis.zrange(set, start, end);
        }
    }

    public static void removeUser(Long id) {
        try (Jedis redis = RedisConfig.getJedis()) {
            redis.zrem(USER_SET, BASE_USER_KEY + id);
        }
    }
}
