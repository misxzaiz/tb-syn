package org.example.syn.core;

import java.util.function.Consumer;

/**
 * 同步引擎核心接口
 * 负责协调整个同步流程，与具体业务解耦
 */
public interface SyncEngine<T> {

    /**
     * 执行同步并消费数据
     * @param cid 租户ID
     * @param dataConsumer 数据消费者
     */
    void syncAndConsume(String cid, Consumer<T> dataConsumer);

    /**
     * 仅执行同步（将数据推送到队列）
     * @param cid 租户ID
     */
    void syncOnly(String cid);

    /**
     * 获取同步状态
     * @param cid 租户ID
     * @return 同步状态描述
     */
    String getSyncStatus(String cid);
}