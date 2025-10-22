package org.example.syn.core;

import org.example.syn.processor.DataProcessor;
import org.example.syn.service.SynConfigService;
import org.example.syn.service.SynQueueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * 同步引擎工厂
 * 用于创建和管理不同类型的同步引擎
 */
@Component
public class SyncEngineFactory {

    @Resource
    private SynConfigService synConfigService;

    @Resource
    private SynQueueService<?> synQueueService;

    @Resource
    private QueryRequestBuilder queryRequestBuilder;

    private final Map<Class<?>, SyncEngine<?>> engineCache = new HashMap<>();

    /**
     * 获取指定数据类型的同步引擎
     */
    @SuppressWarnings("unchecked")
    public <T> SyncEngine<T> getEngine(Class<T> dataType, DataProcessor<T> processor) {
        return (SyncEngine<T>) engineCache.computeIfAbsent(dataType,
            type -> createEngine(processor));
    }

    private <T> SyncEngine<T> createEngine(DataProcessor<T> processor) {
        DefaultSyncEngine<T> engine = new DefaultSyncEngine<>();
        engine.setSynConfigService(synConfigService);
        engine.setSynQueueService((SynQueueService<T>) synQueueService);
        engine.setDataProcessor(processor);
        engine.setQueryRequestBuilder(queryRequestBuilder);
        return engine;
    }
}