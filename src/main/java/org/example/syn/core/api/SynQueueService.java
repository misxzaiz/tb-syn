package org.example.syn.core.api;

import java.util.List;

/**
 * 同步队列服务接口
 * 框架核心接口，定义队列操作的抽象契约
 */
public interface SynQueueService<T> {

    /**
     * 队列前缀常量
     */
    String REDIS_QUEUE_PREFIX = "tb:syn:queue:cid:";
    String REDIS_BAK_QUEUE_PREFIX = "tb:syn:bak:queue:cid:";

    /**
     * 批量推送数据到队列左侧
     * @param prefix 队列前缀
     * @param cid 租户ID
     * @param dataList 数据列表
     */
    void leftPush(String prefix, String cid, List<T> dataList);

    /**
     * 从队列右侧弹出数据并备份
     * 原子操作，确保数据不丢失
     * @param prefix 队列前缀
     * @param cid 租户ID
     * @return 弹出的数据，队列为空时返回null
     */
    T popAndBak(String prefix, String cid);

    /**
     * 获取数据
     * @param prefix 前缀
     * @param cid 租户ID
     * @return 数据
     */
    T get(String prefix, String cid);

    /**
     * 删除数据
     * @param prefix 前缀
     * @param cid 租户ID
     */
    void remove(String prefix, String cid);
}