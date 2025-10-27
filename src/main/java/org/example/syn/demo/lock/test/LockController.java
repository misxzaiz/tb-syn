package org.example.syn.demo.lock.test;

import org.example.syn.demo.lock.SimpleDistributedLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/syn/demo/lock/test")
public class LockController {

    @Autowired
    private SimpleDistributedLock lock;

    @GetMapping("/tb")
    public String syncTbData() {
        lock.tryLockAndRun("tb:sync", () -> {
            // 模拟耗时任务
            try {
                Thread.sleep(30000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        return "同步完成";
    }

}
