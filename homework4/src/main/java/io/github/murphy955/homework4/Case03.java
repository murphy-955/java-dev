package io.github.murphy955.homework4;

import io.github.murphy955.homework4.config.RedisConfig;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 定时批量刷新统计数据、批量预热缓存、批量写入互相不依赖的key
 *
 * @author : 李泽聿
 * @since : 2026:08:13 09:48
 */
public class Case03 {
    public static final String BASE_COUNT_KEY = "fixed_time_task_count_:";
    public static final String CACHE_KEY = "news_:";
    public static final String BASE_DEMO_KEY = "demo_:";
    public static final Integer FIXED_TIME = 1;
    public static final String BASE_URL = "https://www.izuswimassociation3.top/activity/getNewsDetail?id=";

    public static void doTimingTask(ScheduledExecutorService scheduler, Runnable task) {
        scheduler.scheduleAtFixedRate(task, 0, FIXED_TIME, TimeUnit.SECONDS);
    }

    public static ScheduledExecutorService startTimingTask() {
        var scheduler = Executors.newScheduledThreadPool(
                1, Thread.ofVirtual().factory());
        scheduler.scheduleAtFixedRate(new TimingTask(), 0, FIXED_TIME, TimeUnit.SECONDS);
        return scheduler;
    }

    public static void doPreheatCache() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        Map<Integer, String> map = new HashMap<>();
        for (int i = 1; i < 3; i++) {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + i))
                    .GET()
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            map.put(i, response.body());
        }
        try (Jedis redis = RedisConfig.getJedis()) {
            Pipeline pipelined = redis.pipelined();
            for (Integer id : map.keySet()) {
                pipelined.set(CACHE_KEY + id, map.get(id));
            }
            pipelined.sync();
        }
    }

    public static void batchWrite() {
        try (Jedis redis = RedisConfig.getJedis()) {
            Pipeline pipelined = redis.pipelined();
            for (int i = 1; i <= 10; i++) {
                pipelined.set(BASE_DEMO_KEY + i, "demo_" + i);
            }
            pipelined.sync();
        }
    }

    public static void init() {
        try (Jedis redis = RedisConfig.getJedis()) {
            for (int i = 1; i <= 10; i++) {
                redis.set(BASE_COUNT_KEY + i, "0");
            }
        }
    }
}

class TimingTask implements Runnable {
    @Override
    public void run() {
        try (Jedis redis = RedisConfig.getJedis()) {
            Pipeline pipelined = redis.pipelined();
            for (int i = 1; i <= 10; i++) {
                pipelined.incr(Case03.BASE_COUNT_KEY + i);
            }
            pipelined.sync();
        }
    }
}
