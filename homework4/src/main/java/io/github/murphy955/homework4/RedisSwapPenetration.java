package io.github.murphy955.homework4;

import io.github.murphy955.homework4.config.RedisConfig;
import redis.clients.jedis.Jedis;

import java.util.Map;

/**
 * 缓存穿透
 *
 * @author : 李泽聿
 * @since : 2026:08:16 09:35
 */
public class RedisSwapPenetration {
    private static Map<String,String> map = Map.of("key1", "value1", "key2", "value2");
    private static final String NULL_VALUE = "NULL_VALUE";


    public static String getMsg(String key){
        try(Jedis redis = RedisConfig.getJedis()){
            String res;
            res = redis.get(key);
            if (res == null || NULL_VALUE.equals(res)){
                // 查数据库
                res = map.get(key);
                if (res == null){
                    res = NULL_VALUE;
                }
                redis.set(key,res);
                return res;
            } else {
                return res;
            }
        }
    }
}
