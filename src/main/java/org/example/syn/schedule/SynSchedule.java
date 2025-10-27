package org.example.syn.schedule;

import lombok.extern.slf4j.Slf4j;
import org.example.syn.core.engine.SyncEngine;
import org.example.syn.core.engine.SyncEngineFactory;
import org.example.syn.lock.SimpleDistributedLock;
import org.example.syn.processor.PurchaseDataProcessor;
import org.example.syn.service.PurchaseInService;
import org.example.syn.model.dto.TbPurchaseInDTO;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private SimpleDistributedLock lock;

    @Scheduled(fixedDelay = 5000) // 5000 毫秒 = 5 秒
    public void syn() {
        String cid = "1";
        String lockKey = "sync_lock:cid:" + cid;
        lock.tryLockAndRun(lockKey, () -> {
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
                    log.info("已保存采购入库数据到MySQL：{}", data.getIoId());
                } catch (Exception e) {
                    log.error("保存采购入库数据失败，ioId: {}, error: {}", data.getIoId(), e.getMessage(), e);
                    throw e;
                }
            });

        });
    }

}
