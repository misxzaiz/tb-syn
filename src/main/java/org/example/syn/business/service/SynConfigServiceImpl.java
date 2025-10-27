package org.example.syn.business.service;

import org.example.syn.core.api.SynConfigService;
import org.example.syn.core.model.SynConfigDTO;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Optional;

@Service
public class SynConfigServiceImpl implements SynConfigService {
    
    public static String REDIS_KEY_PREFIX = "tb:syn:config:cid:";
    
    @Resource
    private RedisTemplate<String, SynConfigDTO> redisTemplate;
    
    public void saveSynConfigDTO(SynConfigDTO tbSynConfigDTO) {
        redisTemplate.opsForValue().set(REDIS_KEY_PREFIX + tbSynConfigDTO.getCid(), tbSynConfigDTO);
    }
    
    public Optional<SynConfigDTO> getSynConfigDTO(String cid) {
        SynConfigDTO json = redisTemplate.opsForValue().get(REDIS_KEY_PREFIX + cid);
        return Optional.ofNullable(json);
    }
}