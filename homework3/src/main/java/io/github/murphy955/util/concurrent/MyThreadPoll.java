package io.github.murphy955.util.concurrent;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.Set;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.RejectedExecutionException;
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
     *     <li> 当前线程数 < corePoolSize → addWorker(task, true)  // 创建核心线程，直接执行</li>
     *     <li> 否则，queue.offer(task) == true → 入队成功，结束</li>
     *     <li> 否则，当前线程数 < maximumPoolSize → addWorker(task, false) // 创建救急线程，直接执行</li>
     *     <li> 否则 → reject(task) // 拒绝策略</li>
     * </ol>
     *
     * @author 李泽聿
     * @since 2026-07-14 16:30
     */
    public void execute(Runnable task) {
        if (task == null){
            throw new IllegalArgumentException("Task cannot be null");
        }
        if (workerSet.size() < corePoolSize) {
            addWorker(task, true);
        } else if (queue.offer(task)) {
            return;
        } else if (workerSet.size() < maximumPoolSize) {
            addWorker(task, false);
        } else {
            rejectedExecution(task, denyPolicy);
        }
    }

    /**
     * @param task 任务
     * @param core 是否核心线程
     * @author 李泽聿
     * @since 2026-07-14 16:48
     */
    private void addWorker(Runnable task, boolean core) {
        int limit;
        if (core) {
            limit = corePoolSize;
        } else {
            limit = maximumPoolSize;
        }
        if (workerSet.size() >= limit) {
            return;
        }
        Worker worker = new Worker(task);
        workerSet.add(worker);
        worker.start();
    }

    /**
     * @param task 任务
     * @param denyPolicy 拒绝策略
     * @author 李泽聿
     * @since 2026-07-14 17:02
     */
    private void rejectedExecution(Runnable task, DenyPolicy denyPolicy) {
        if (DenyPolicy.ABORT.equals(denyPolicy)){
            throw new RejectedExecutionException("Task " + task + " rejected");
        }
        else if (DenyPolicy.CALLER_RUNS.equals(denyPolicy)){
            task.run();
        }
        else if (DenyPolicy.DISCARD.equals(denyPolicy)){
            // 静默丢弃任务
            return;
        }
        else if (DenyPolicy.DISCARD_OLDEST.equals(denyPolicy)){
            // 丢弃队列最老任务，然后重试提交
            queue.poll();
            queue.offer(task);
        }
    }


    @EqualsAndHashCode
    private final class Worker implements Runnable {
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
            Runnable task = firstTask;
            firstTask = null;

            while (task != null || (task = getTask()) != null) {
                try {
                    task.run();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    task = null;
                }
            }

            // 线程退出时从集合移除
            workerSet.remove(this);
        }

        private Runnable getTask() {
            if (workerSet.size() < corePoolSize) {
                try {
                    return queue.take();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return null;
                }
            }
            try {
                return queue.poll(keepAliveTime, timeUnit);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return null;
            }
        }
    }
}
