package org.example.syn.schedule;

import lombok.extern.slf4j.Slf4j;
import org.example.syn.template.PurchaseSyncTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Slf4j
@Component
public class SynSchedule {

    @Resource
    private PurchaseSyncTemplate purchaseSyncTemplate;

    @Scheduled(fixedRate = 5000) // 5000 毫秒 = 5 秒
    public void syn() {
        String cid = "1";
        purchaseSyncTemplate.syn(cid, data -> {
            // 持久化
            log.debug("已保存：{}", data.getIoId());
        });
    }

}
