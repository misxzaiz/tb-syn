package org.example.syn.web;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/demo")
public class DemoController {
    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    private static Integer ct = 1;
    @RequestMapping("/pt")
    public Object pt() {
        List<Integer> list = new ArrayList<>();
        list.add(ct++);
        list.add(ct++);

        // 将数据推送到Redis队列
        redisTemplate.opsForList().leftPushAll("ctp", list.toArray());
//
//        List<String> list1 = new ArrayList<>();
//        list1.add("ct++");
//        list1.add("ct+1+");
//        Collections.reverse(list1);
//        // 将数据推送到Redis队列
//        redisTemplate.opsForList().leftPushAll("ctps", list1.toArray());

        return "你好";
    }
}
