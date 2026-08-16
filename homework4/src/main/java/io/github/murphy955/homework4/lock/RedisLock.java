package io.github.murphy955.homework4.lock;

import io.github.murphy955.homework4.config.RedisConfig;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.params.SetParams;

import java.util.UUID;

/**
 * 基于redis实现简单的分布式锁
 *
 * @author : 李泽聿
 * @since : 2026:08:16 11:17
 */
public class RedisLock {
    private static final String UNLOCK_LUA_SCRIPT = """
            if redis.call("get", KEYS[1]) == ARGV[1] then
                return redis.call("del", KEYS[1])
            else
                return 0
            end
            """;

    /**
     * @param key 锁的key
     * @param requestId 唯一标识
     * @param ttl 锁的过期时间
     * @return boolean 获取锁成功返回true
     * @author 李泽聿
     * @since 2026-08-16 11:34
     */
    public static boolean tryLock(String key, String requestId, long ttl) {
        try (Jedis redis = RedisConfig.getJedis()) {
            SetParams set = new SetParams();
            set.nx().ex(ttl);
            String res = redis.set(key, requestId, set);
            return "OK".equals(res);
        }
    }

    /**
     * @param key 锁的key
     * @param requestId 唯一标识
     * @return boolean 解锁成功返回true
     * @author 李泽聿
     * @since 2026-08-16 11:34
     */
    public static boolean unlock(String key, String requestId) {
        try (Jedis redis = RedisConfig.getJedis()) {
            Object res = redis.eval(UNLOCK_LUA_SCRIPT, 1, key, requestId);
            return Long.valueOf(1L).equals(res);
        }
    }

    public static String createRequestId() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
