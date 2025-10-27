package org.example.syn.lock;


import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 不可重入的分布式锁（带自动续期）
 */
@Component
public class SimpleDistributedLock {

    private final StringRedisTemplate redisTemplate;
    private final ScheduledExecutorService renewExecutor;

    public SimpleDistributedLock(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.renewExecutor = Executors.newScheduledThreadPool(1);
    }

    public void tryLockAndRun(String key, Runnable run) {
        LockHandle handle = tryLock(key);
        if (handle == null) {
            return;
        }

        try {
            run.run();
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
     */
    public void unlock(LockHandle handle) {
        if (handle == null || !handle.isLocked.get()) {
            return;
        }

        // 先停止看门狗，防止解锁失败时继续续期
        handle.stopRenewal();

        String lua = "if redis.call('get', KEYS[1]) == ARGV[1] then " +
                "return redis.call('del', KEYS[1]) " +
                "else return 0 end";

        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setResultType(Long.class);
        script.setScriptText(lua);

        redisTemplate.execute(script, Collections.singletonList(handle.key), handle.value);
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
                    redisTemplate.execute(script, Collections.singletonList(key),
                            value, String.valueOf(expireMillis));
                } catch (Exception ignored) {
                }
            }, interval, interval, TimeUnit.MILLISECONDS);
        }

        /**
         * 停止自动续期
         */
        void stopRenewal() {
            isLocked.set(false);
            if (renewTask != null) renewTask.cancel(true);
        }
    }
}
