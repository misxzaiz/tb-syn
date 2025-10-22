package org.example.syn.core;

import org.example.syn.core.model.PageReqDTO;
import org.example.syn.core.model.SynConfigDTO;
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
    public PageReqDTO build(SynConfigDTO config) {
        boolean isFirstSync = isInitialState(config.getSynState());
        String beginTime = isFirstSync ? config.getBeginSynTime() : config.getEndSynTime();
        String endTime = isFirstSync ? config.getEndSynTime() : null;

        return PageReqDTO.builder()
                .cid(config.getCid())
                .modifyBeginTime(beginTime)
                .modifyEndTime(endTime)
                .synIntervalSecond(config.getSynIntervalSecond())
                .build();
    }

    private boolean isInitialState(Integer synState) {
        return SynConfigDTO.SYN_TWO.equals(synState) ||
               SynConfigDTO.SYN_THREE.equals(synState);
    }
}