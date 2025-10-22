package org.example.syn.schedule;

import lombok.extern.slf4j.Slf4j;
import org.example.syn.core.engine.SyncEngine;
import org.example.syn.core.engine.SyncEngineFactory;
import org.example.syn.processor.PurchaseDataProcessor;
import org.example.syn.service.PurchaseInService;
import org.example.syn.model.dto.TbPurchaseInDTO;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
public class SynSchedule {

    @Resource
    private PurchaseInService purchaseInService;

    @Resource
    private PurchaseDataProcessor purchaseDataProcessor;

    @Resource
    private SyncEngineFactory syncEngineFactory;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Scheduled(fixedDelay = 5000) // 5000 毫秒 = 5 秒
    public void syn() {
        String cid = "1";
        String lockKey = "sync_lock:cid:" + cid;
        // 尝试获取锁，设置一个合理的超时时间（如30秒）
        if (redisTemplate.opsForValue().setIfAbsent(lockKey, "1", 30, TimeUnit.SECONDS)) {
            long startTime = System.currentTimeMillis();
            AtomicInteger syncCount = new AtomicInteger();
            try {
                // 获取锁成功，执行同步逻辑
                // 直接使用SyncEngine，减少抽象层
                SyncEngine<TbPurchaseInDTO> syncEngine = syncEngineFactory.getEngine(
                    TbPurchaseInDTO.class,
                    purchaseDataProcessor
                );

                syncEngine.syncAndConsume(cid, data -> {
                    // 持久化到MySQL
                    try {
                        purchaseInService.savePurchaseIn(data, cid);
                        syncCount.getAndIncrement();
                        log.info("已保存采购入库数据到MySQL：{}", data.getIoId());
                    } catch (Exception e) {
                        log.error("保存采购入库数据失败，ioId: {}, error: {}", data.getIoId(), e.getMessage(), e);
                        throw e;
                    }
                });

                long endTime = System.currentTimeMillis();
                log.info("同步任务执行完成，cid: {}, 同步数量: {}, 耗时: {}ms", cid, syncCount, endTime - startTime);
            } catch (Exception e) {
                log.error("同步任务执行失败，cid: {}, error: {}", cid, e.getMessage(), e);
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
