package org.example.syn.core.api;

import org.example.syn.core.model.SynConfigDTO;
import java.util.Optional;

/**
 * 同步配置存储服务接口
 * 框架核心接口，定义配置存储的抽象契约
 */
public interface SynConfigService {

    /**
     * 保存同步配置
     * @param config 同步配置
     */
    void saveSynConfigDTO(SynConfigDTO config);

    /**
     * 获取同步配置
     * @param cid 租户ID
     * @return 同步配置，如果不存在返回Optional.empty()
     */
    Optional<SynConfigDTO> getSynConfigDTO(String cid);
}