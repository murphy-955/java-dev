package io.github.murphy955.util.concurrent;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Set;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
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
    // 工作线程
    private Set<Worker> workerSet;

    public MyThreadPoll(BlockingDeque<Runnable> queue, int corePoolSize, int maximumPoolSize,
                        long keepAliveTime, TimeUnit timeUnit, DenyPolicy denyPolicy) {
        this.queue = queue;
        this.corePoolSize = corePoolSize;
        this.maximumPoolSize = maximumPoolSize;
        this.keepAliveTime = keepAliveTime;
        this.timeUnit = timeUnit;
        this.denyPolicy = denyPolicy;
    }

    public MyThreadPoll(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit timeUnit, DenyPolicy denyPolicy) {
        this.corePoolSize = corePoolSize;
        this.maximumPoolSize = maximumPoolSize;
        this.keepAliveTime = keepAliveTime;
        this.timeUnit = timeUnit;
        this.denyPolicy = denyPolicy;
        this.queue = new LinkedBlockingDeque<>(1000);
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

    /**
     * <ol>
     *     <li>workerSet.size() < corePoolSize&&queue.offer(task) == true，创建新线程执行任务</li>
     *     <li>workerSet.size() >= corePoolSize&&queue.offer(task) == true，将任务加入队列</li>
     *     <li>workerSet.size() >= maximumPoolSize&&queue.offer(task)==false，执行拒绝策略</li>
     * </ol>
    * @author 李泽聿
    * @since 2026-07-14 16:30
    */
    public void execute(Runnable task) {
    }
}

final class Worker implements Runnable{
    private Runnable firstTask;

    Worker(Runnable firstTask) {
        this.firstTask = firstTask;
    }

    void start() {
        Thread thread = new Thread(this);
        thread.start();
    }

    @Override
    public void run() {
        while (firstTask != null || (firstTask = getTask()) != null) {
            try {
                firstTask.run();
            } finally {
                firstTask = null;
            }
        }
        // 退出时从 workers 中移除
        workers.remove(this);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }
}
