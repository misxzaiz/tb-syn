package org.example.syn.web;

import org.example.syn.service.SynDataProcessor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/syn")
public class SynController {
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private SynDataProcessor synDataProcessor;


    @RequestMapping("/test")
    public Object test(@RequestParam String name) {
        stringRedisTemplate.opsForValue().set("test", name);
        String test = stringRedisTemplate.opsForValue().get("test");

        return "你好:" + test;
    }

    @RequestMapping("/synTest")
    public Object synTest() {
        return synDataProcessor.synTestData();
    }

    /**
     * 推送数据
     * @param cid 租户id
     * @return
     */
    @RequestMapping("/pushDate")
    public Object pushDate(@RequestParam String cid) {
        return synDataProcessor.pushDate(cid);
    }

    


    /**
     * 推送数据
     * @param cid 租户id
     * @return
     */
    @RequestMapping("/popDate")
    public Object popDate(@RequestParam String cid) {
        return synDataProcessor.popDate(cid, data -> {
            // 持久化
            System.out.println(data);
        });
    }


}
