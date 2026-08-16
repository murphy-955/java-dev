package io.github.murphy955.homework4;

import io.github.murphy955.homework4.config.RedisConfig;
import io.github.murphy955.homework4.lock.RedisLock;
import redis.clients.jedis.Jedis;

import java.util.Map;

/**
 * 缓存击穿
 *
 * @author : 李泽聿
 * @since : 2026:08:16 11:03
 */
public class RedisBreakdown {
    public Map<String, String> map = Map.of("key1", "value1", "key2", "value2");
    public static final String BASE_LOCK_KEY = "lock:";
    public static final int MAX_TRY_COUNT = 10;
    public static final String NULL_VALUE = "NULL_VALUE";

    public String getMsg(String key) {
        String res = null;
        try (Jedis redis = RedisConfig.getJedis()) {
            res = redis.get(key);
            if (res != null) {
                return NULL_VALUE.equals(res) ? null : res;
            }
            for (int i = 0; i < MAX_TRY_COUNT; i++) {
                String requestId = RedisLock.createRequestId();
                String lockKey = BASE_LOCK_KEY + key;
                try {
                    boolean isLock = RedisLock.tryLock(lockKey, requestId, 10);
                    if (isLock) {
                        res = redis.get(key);
                        if (res != null) {
                            return NULL_VALUE.equals(res) ? null : res;
                        }
                        res = map.get(key);
                        if (res == null) {
                            res = NULL_VALUE;
                            redis.setex(key, 10, res);
                            RedisLock.unlock(lockKey, requestId);
                            return null;
                        } else {
                            redis.setex(key, 100, res);
                            RedisLock.unlock(lockKey, requestId);
                            return res;
                        }
                    } else {
                        Thread.sleep(100);
                    }
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return null;
    }
}
