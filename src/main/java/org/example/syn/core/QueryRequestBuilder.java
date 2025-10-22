package org.example.syn.core;

import org.example.tb.model.BaseQueryRequest;
import org.example.tb.model.TbSynConfigDTO;
import org.springframework.stereotype.Component;

/**
 * 查询请求构建器
 * 负责根据同步配置构建查询请求
 */
@Component
public class QueryRequestBuilder {

    /**
     * 根据同步配置构建查询请求
     */
    public BaseQueryRequest build(TbSynConfigDTO config) {
        boolean isFirstSync = isInitialState(config.getSynState());
        String beginTime = isFirstSync ? config.getBeginSynTime() : config.getEndSynTime();
        String endTime = isFirstSync ? config.getEndSynTime() : null;

        return new BaseQueryRequest()
                .setCid(config.getCid())
                .timeRange(beginTime, endTime)
                .param("synIntervalSecond", config.getSynIntervalSecond());
    }

    private boolean isInitialState(Integer synState) {
        return TbSynConfigDTO.SYN_TWO.equals(synState) ||
               TbSynConfigDTO.SYN_THREE.equals(synState);
    }
}