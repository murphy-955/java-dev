package io.github.murphy955.util.concurrent;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.concurrent.BlockingDeque;
import java.util.concurrent.TimeUnit;

/**
 * 手写线程池
 *
 * @author : 李泽聿
 * @since : 2026:07:14 15:58
 */
public class MyThreadPoll {
    // 任务队列
    private BlockingDeque<Runnable> queue;
    // 核心线程数
    private int corePoolSize;
    // 最大线程数
    private int maximumPoolSize;
    // 线程空闲时间
    private long keepAliveTime;
    // 时间单位
    private TimeUnit timeUnit;
    // 拒绝策略
    private DenyPolicy denyPolicy;

    public MyThreadPoll(BlockingDeque<Runnable> queue, int corePoolSize, int maximumPoolSize,
                        long keepAliveTime, TimeUnit timeUnit, DenyPolicy denyPolicy) {
        this.queue = queue;
        this.corePoolSize = corePoolSize;
        this.maximumPoolSize = maximumPoolSize;
        this.keepAliveTime = keepAliveTime;
        this.timeUnit = timeUnit;
        this.denyPolicy = denyPolicy;
    }

    /**
     * 拒绝策略
     *
     * <ul>
     *     <li>AbortPolicy: 抛出 `RejectedExecutionException`</li>
     *     <li>CallerRunsPolicy: 由调用线程（提交任务的线程）自己执行</li>
     *     <li>DiscardPolicy: 静默丢弃任务</li>
     *     <li>DiscardOldestPolicy: 丢弃队列最老任务，然后重试提交</li>
     * </ul>
     *
     * @author 李泽聿
     * @since 2026-07-14 16:03
     */
    @Getter
    @AllArgsConstructor
    public enum DenyPolicy {
        ABORT("AbortPolicy: 抛出 RejectedExecutionException"),
        CALLER_RUNS("CallerRunsPolicy: 由调用线程（提交任务的线程）自己执行"),
        DISCARD("DiscardPolicy: 静默丢弃任务"),
        DISCARD_OLDEST("DiscardOldestPolicy: 丢弃队列最老任务，然后重试提交");

        private final String description;
    }
}
