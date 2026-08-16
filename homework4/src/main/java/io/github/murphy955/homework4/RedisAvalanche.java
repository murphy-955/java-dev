package io.github.murphy955.homework4;

import io.github.murphy955.homework4.config.RedisConfig;
import redis.clients.jedis.Jedis;

import java.util.Map;

/**
 * 缓存雪崩
 *
 * @author : 李泽聿
 * @since : 2026:08:16 10:57
 */
public class RedisAvalanche {
    private static final Map<String,String> map = Map.of("key1", "value1", "key2", "value2");
    private static final Long RANDOM_TTL = 500L;
    private static final Long BASE_TTL = 1000L;

    public static void setCache(){
        try(Jedis redis = RedisConfig.getJedis()) {
            for(String key : map.keySet()){
                redis.setex(key,BASE_TTL + (long)(Math.random() * RANDOM_TTL),map.get(key));
            }
        }
    }

}
