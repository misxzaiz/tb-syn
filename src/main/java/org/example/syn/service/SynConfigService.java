package org.example.syn.service;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import org.example.tb.model.TbSynConfigDTO;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Optional;

@Service
public class SynConfigService {
    
    public static String REDIS_KEY_PREFIX = "tb:syn:config:cid:";
    
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    
    public void saveTbSynConfigDTO(TbSynConfigDTO tbSynConfigDTO) {
        stringRedisTemplate.opsForValue().set(REDIS_KEY_PREFIX + tbSynConfigDTO.getCid(), JSONUtil.toJsonStr(tbSynConfigDTO));
    }
    
    public Optional<TbSynConfigDTO> getTbSynConfigDTO(String cid) {
        String json = stringRedisTemplate.opsForValue().get(REDIS_KEY_PREFIX + cid);
        return Optional.ofNullable(StrUtil.isBlank(json) ? null : JSONUtil.toBean(json, TbSynConfigDTO.class));
    }
}