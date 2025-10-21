package org.example.syn.schedule;

import lombok.extern.slf4j.Slf4j;
import org.example.syn.template.PurchaseSyncTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class SynSchedule {

    @Resource
    private PurchaseSyncTemplate purchaseSyncTemplate;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Scheduled(fixedDelay = 5000) // 5000 毫秒 = 5 秒
    public void syn() {
        String cid = "1";
        String lockKey = "sync_lock:cid:" + cid;
        // 尝试获取锁，设置一个合理的超时时间（如30秒）
        if (redisTemplate.opsForValue().setIfAbsent(lockKey, "1", 30, TimeUnit.SECONDS)) {
            try {
                // 获取锁成功，执行同步逻辑
                purchaseSyncTemplate.syn(cid, data -> {
                    // 持久化
                    log.debug("已保存：{}", data.getIoId());
                });
            } finally {
                // 确保在finally块中释放锁
                redisTemplate.delete(lockKey);
            }
        } else {
            // 获取锁失败，说明其他实例正在执行，直接返回
            log.debug("获取分布式锁失败，任务正在由其他实例执行, cid: {}", cid);
        }

    }

}
