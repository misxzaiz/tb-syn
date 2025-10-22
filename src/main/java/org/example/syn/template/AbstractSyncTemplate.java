package org.example.syn.template;

import lombok.extern.slf4j.Slf4j;
import org.example.syn.core.SyncEngine;
import org.example.syn.core.SyncEngineFactory;
import org.example.syn.processor.DataProcessor;

import javax.annotation.Resource;
import java.util.function.Consumer;

/**
 * 简化后的同步模板
 * 作为适配器，保留原有接口，内部使用SyncEngine
 */
@Slf4j
public abstract class AbstractSyncTemplate<T> {

    @Resource
    private SyncEngineFactory syncEngineFactory;

    private SyncEngine<T> engine;

    /**
     * 获取同步引擎（懒加载）
     */
    private SyncEngine<T> getEngine() {
        if (engine == null) {
            engine = syncEngineFactory.getEngine(getDataType(), getDataProcessor());
        }
        return engine;
    }

    /**
     * 保留原有接口，内部委托给SyncEngine
     */
    public void syn(String cid, Consumer<T> dataConsumer) {
        getEngine().syncAndConsume(cid, dataConsumer);
    }

    /**
     * 保留原有接口
     */
    public final void syncData(String cid) {
        getEngine().syncOnly(cid);
    }

    /**
     * 子类需要实现：返回数据类型
     */
    protected abstract Class<T> getDataType();

    /**
     * 子类需要实现：返回数据处理器
     */
    protected abstract DataProcessor<T> getDataProcessor();
}