package org.example.syn.lock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 不可重入的分布式锁（带自动续期）
 */
@Component
public class SimpleDistributedLock {

    private static final Logger log = LoggerFactory.getLogger(SimpleDistributedLock.class);
    private static final int MAX_RENEW_FAILURES = 3; // 最大续期失败次数

    private final StringRedisTemplate redisTemplate;
    private final ScheduledExecutorService renewExecutor;

    public SimpleDistributedLock(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.renewExecutor = Executors.newScheduledThreadPool(1);
    }

    /**
     * 尝试获取锁并执行任务
     * @param key 锁的键名
     * @param run 任务执行器
     * @return true 表示执行成功，false 表示获取锁失败
     */
    public boolean tryLockAndRun(String key, Runnable run) {
        LockHandle handle = tryLock(key);
        if (handle == null) {
            log.warn("获取锁失败，跳过任务执行: key={}", key);
            return false;
        }

        try {
            run.run();
            return true;
        } catch (Exception e) {
            log.error("执行任务异常: key={}", key, e);
            throw e; // 重新抛出，让调用者处理
        } finally {
            unlock(handle);
        }
    }

    /**
     * 尝试获取锁（不可重入）
     *
     * @param key 锁的键名
     * @return LockHandle 如果加锁成功则返回，否则为 null
     */
    public LockHandle tryLock(String key) {
        return tryLock(key, 30000);
    }

    /**
     * 尝试获取锁（不可重入）
     *
     * @param key 锁的键名
     * @param expireMillis 锁过期时间（毫秒）
     * @return LockHandle 如果加锁成功则返回，否则为 null
     */
    public LockHandle tryLock(String key, long expireMillis) {
        String value = UUID.randomUUID().toString();

        Boolean success = redisTemplate.opsForValue()
                .setIfAbsent(key, value, expireMillis, TimeUnit.MILLISECONDS);

        if (Boolean.TRUE.equals(success)) {
            LockHandle handle = new LockHandle(key, value, expireMillis);
            handle.startRenewal();
            return handle;
        }

        return null;
    }

    /**
     * 解锁逻辑（Lua 脚本防止误删）
     * @param handle 锁句柄
     * @return true 表示解锁成功，false 表示解锁失败
     */
    public boolean unlock(LockHandle handle) {
        if (handle == null || !handle.isLocked.get()) {
            return true;
        }

        // 先停止看门狗，防止解锁失败时继续续期
        handle.stopRenewal();

        try {
            String lua = "if redis.call('get', KEYS[1]) == ARGV[1] then " +
                    "return redis.call('del', KEYS[1]) " +
                    "else return 0 end";

            DefaultRedisScript<Long> script = new DefaultRedisScript<>();
            script.setResultType(Long.class);
            script.setScriptText(lua);

            Long result = redisTemplate.execute(script,
                Collections.singletonList(handle.key), handle.value);

            boolean success = result != null && result == 1;
            if (!success) {
                log.warn("解锁失败，锁可能已过期或被删除: key={}", handle.key);
            }
            return success;
        } catch (Exception e) {
            log.error("解锁异常: key={}", handle.key, e);
            return false;
        }
    }

    @PreDestroy
    public void shutdown() {
        renewExecutor.shutdown();
    }

    // =============================
    // 内部类：锁句柄
    // =============================
    public class LockHandle {
        private final String key;
        private final String value;
        private final long expireMillis;
        private ScheduledFuture<?> renewTask;
        private final AtomicBoolean isLocked = new AtomicBoolean(true);
        private final AtomicInteger failureCount = new AtomicInteger(0);

        LockHandle(String key, String value, long expireMillis) {
            this.key = key;
            this.value = value;
            this.expireMillis = expireMillis;
        }

        /**
         * 启动自动续期（看门狗）
         */
        void startRenewal() {
            long interval = expireMillis / 3;
            renewTask = renewExecutor.scheduleAtFixedRate(() -> {
                if (!isLocked.get()) return;

                try {
                    String lua = "if redis.call('get', KEYS[1]) == ARGV[1] then " +
                            "return redis.call('pexpire', KEYS[1], ARGV[2]) " +
                            "else return 0 end";
                    DefaultRedisScript<Long> script = new DefaultRedisScript<>();
                    script.setResultType(Long.class);
                    script.setScriptText(lua);

                    Long result = redisTemplate.execute(script,
                        Collections.singletonList(key), value, String.valueOf(expireMillis));

                    if (result == null || result != 1) {
                        // 续期失败，锁可能已不存在
                        int failures = failureCount.incrementAndGet();
                        log.warn("看门狗续期失败: key={}, failures={}", key, failures);

                        if (failures >= MAX_RENEW_FAILURES) {
                            log.error("看门狗续期失败次数过多，自动停止: key={}", key);
                            stopRenewal();
                        }
                    } else {
                        // 续期成功，重置失败计数
                        failureCount.set(0);
                    }
                } catch (Exception e) {
                    int failures = failureCount.incrementAndGet();
                    log.error("看门狗续期异常: key={}, failures={}", key, failures, e);

                    if (failures >= MAX_RENEW_FAILURES) {
                        log.error("看门狗续期异常次数过多，自动停止: key={}", key);
                        stopRenewal();
                    }
                }
            }, interval, interval, TimeUnit.MILLISECONDS);
        }

        /**
         * 停止自动续期
         */
        void stopRenewal() {
            if (isLocked.compareAndSet(true, false)) {
                if (renewTask != null) {
                    renewTask.cancel(true);
                }
            }
        }
    }
}
