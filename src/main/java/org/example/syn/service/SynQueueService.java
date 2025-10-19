package org.example.syn.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class SynQueueService<T> {
    
    public static String REDIS_QUEUE_PREFIX = "tb:syn:queue:cid:";
    public static String REDIS_BAK_QUEUE_PREFIX = "tb:syn:bak:queue:cid:";
    
    @Resource
    private RedisTemplate<String, T> redisTemplate;
    
    public void leftPush(String prefix, String cid, T data) {
        String queueKey = prefix + cid;
        redisTemplate.opsForList().leftPush(queueKey, data);
    }
    
    public void leftPush(String prefix, String cid, List<T> dataList) {
        String queueKey = prefix + cid;
        redisTemplate.opsForList().leftPushAll(queueKey, dataList);
    }
    
    public T rightPop(String prefix, String cid) {
        String queueKey = prefix + cid;
        return redisTemplate.opsForList().rightPop(queueKey);
    }
    
    public void bakData(String prefix, String cid, String key, T data) {
        String queueKey = prefix + cid;
        redisTemplate.opsForHash().put(queueKey, key, data);
    }
    
    public void removeBakData(String prefix, String cid, String key) {
        String queueKey = prefix + cid;
        redisTemplate.opsForHash().delete(queueKey, key);
    }
}