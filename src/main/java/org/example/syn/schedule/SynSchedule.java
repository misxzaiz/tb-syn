package org.example.syn.schedule;

import org.example.syn.template.PurchaseSyncTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class SynSchedule {

    @Resource
    private PurchaseSyncTemplate purchaseSyncTemplate;

    @Scheduled(fixedRate = 5000) // 5000 毫秒 = 5 秒
    public void scheduleTaskWithFixedRate() {
        String cid = "1";
        purchaseSyncTemplate.syn(cid, data -> {
            // 持久化
            System.out.println(data);
        });
    }

}
