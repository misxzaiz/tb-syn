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
        SimpleDistributedLock.LockHandle handle = lock.tryLock("tb:sync", 30000);
        if (handle == null) {
            return "同步任务正在执行中";
        }

        try {
            // 模拟耗时任务
            Thread.sleep(30000);
            return "同步成功";
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return "同步中断";
        } finally {
            lock.unlock(handle);
        }
    }

}
