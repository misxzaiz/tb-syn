package org.example.syn.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;

@Service
public class SynQueueService<T> {
    
    private static final Logger log = LoggerFactory.getLogger(SynQueueService.class);
    
    public static String REDIS_QUEUE_PREFIX = "tb:syn:queue:cid:";
    public static String REDIS_BAK_QUEUE_PREFIX = "tb:syn:bak:queue:cid:";
    
    @Resource
    private RedisTemplate<String, T> redisTemplate;
    
    public void leftPush(String prefix, String cid, List<T> dataList) {
        String queueKey = prefix + cid;
        redisTemplate.opsForList().leftPushAll(queueKey, dataList);
    }

    /**
     * pop 并备份；原子操作
     * 使用 Lua 脚本确保操作的原子性（推荐方式）
     *
     * @param key 备份键
     * @param cid 租户ID
     * @return 弹出的数据，如果队列为空则返回null
     */
    public T popAndBak(String key, String cid) {
        String queueKey = REDIS_QUEUE_PREFIX + cid;
        String backupKey = REDIS_BAK_QUEUE_PREFIX + cid;
        
        // Lua脚本：原子性pop+备份操作
        String luaScript = 
                "local data = redis.call('RPOP', KEYS[1])\n" +
                "if data then\n" +
                "    -- 备份数据\n" +
                "    redis.call('SET', KEYS[2], data)\n" +
                "    -- 返回数据\n" +
                "    return data\n" +
                "else\n" +
                "    -- 队列为空返回nil\n" +
                "    return nil\n" +
                "end";
        
        try {
            DefaultRedisScript<Object> script = new DefaultRedisScript<>(luaScript, Object.class);
            
            // KEYS[1] = 队列key, KEYS[2] = 备份key
            List<String> keys = Arrays.asList(queueKey, backupKey);
            
            // 执行Lua脚本
            Object result = redisTemplate.execute(script, keys);
            T data = result != null ? (T) result : null;
            
            if (data != null) {
                log.debug("popAndBak成功: cid={}, key={}, 数据大小={}", cid, key, data.toString().length());
            } else {
                log.debug("popAndBak队列为空: cid={}, key={}", cid, key);
            }
            
            return data;
            
        } catch (Exception e) {
            log.error("popAndBak操作失败: cid={}, key={}", cid, key, e);
            throw new RuntimeException("popAndBak操作失败", e);
        }
    }
    
    public T rightPop(String prefix, String cid) {
        String queueKey = prefix + cid;
        return redisTemplate.opsForList().rightPop(queueKey);
    }
    
    public void bakData(String prefix, String cid, String key, T data) {
        String queueKey = prefix + cid;
        redisTemplate.opsForHash().put(queueKey, key, data);
    }
    
    public void removeBakData(String prefix, String cid) {
        String queueKey = prefix + cid;
        redisTemplate.opsForHash().delete(queueKey);
    }
}