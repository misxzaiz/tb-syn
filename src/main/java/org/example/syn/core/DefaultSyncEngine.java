package org.example.syn.core;

import lombok.extern.slf4j.Slf4j;
// DataProcessor现在在同一个包中，不需要import
import org.example.syn.service.SynConfigService;
import org.example.syn.service.SynQueueService;
import org.example.tb.model.BaseQueryRequest;
import org.example.tb.model.TbPageDTO;
import org.example.tb.model.TbSynConfigDTO;

import javax.annotation.Resource;
import java.util.function.Consumer;

/**
 * 默认同步引擎实现
 * 将原AbstractSyncTemplate的核心逻辑抽取出来
 */
@Slf4j
public class DefaultSyncEngine<T> implements SyncEngine<T> {

    private SynConfigService synConfigService;
    private SynQueueService<T> synQueueService;
    private DataProcessor<T> dataProcessor;
    private QueryRequestBuilder queryRequestBuilder;

    // Setter方法用于依赖注入
    public void setSynConfigService(SynConfigService synConfigService) {
        this.synConfigService = synConfigService;
    }

    public void setSynQueueService(SynQueueService<T> synQueueService) {
        this.synQueueService = synQueueService;
    }

    public void setDataProcessor(DataProcessor<T> dataProcessor) {
        this.dataProcessor = dataProcessor;
    }

    public void setQueryRequestBuilder(QueryRequestBuilder queryRequestBuilder) {
        this.queryRequestBuilder = queryRequestBuilder;
    }

    @Override
    public void syncAndConsume(String cid, Consumer<T> dataConsumer) {
        log.debug("开始同步数据，cid: {}", cid);

        // 先处理备份数据
        T bakData = synQueueService.get(SynQueueService.REDIS_BAK_QUEUE_PREFIX, cid);
        if (bakData != null) {
            log.debug("处理备份数据，cid: {}", cid);
            processWithBackup(bakData, cid, dataConsumer);
        }

        // 尝试从队列获取数据
        T data = synQueueService.popAndBak(SynQueueService.REDIS_QUEUE_PREFIX, cid);
        if (data == null) {
            // 队列无数据，执行同步
            log.debug("队列为空，执行同步，cid: {}", cid);
            syncOnly(cid);
            data = synQueueService.popAndBak(SynQueueService.REDIS_QUEUE_PREFIX, cid);
        }

        // 处理数据
        if (data != null) {
            log.debug("开始处理数据，cid: {}", cid);
            processWithBackup(data, cid, dataConsumer);

            // 继续处理剩余数据
            int count = 0;
            while (count < 100) {
                data = synQueueService.popAndBak(SynQueueService.REDIS_QUEUE_PREFIX, cid);
                if (data == null) {
                    break;
                }
                processWithBackup(data, cid, dataConsumer);
                count++;
            }
        }

        log.debug("同步完成，cid: {}", cid);
    }

    @Override
    public void syncOnly(String cid) {
        TbSynConfigDTO config = synConfigService.getTbSynConfigDTO(cid)
                .orElse(TbSynConfigDTO.init(cid));

        BaseQueryRequest queryRequest = queryRequestBuilder.build(config);

        if (!shouldSync(queryRequest)) {
            log.info("同步时间未到达，cid: {}", cid);
            return;
        }

        // 执行数据查询
        TbPageDTO<T> pageResult = dataProcessor.process(queryRequest);

        // 推送到队列
        if (pageResult.getDatas() != null && !pageResult.getDatas().isEmpty()) {
            synQueueService.leftPush(SynQueueService.REDIS_QUEUE_PREFIX, cid, pageResult.getDatas());
            log.info("推送数据到队列，数量: {}, cid: {}", pageResult.getDatas().size(), cid);
        }

        // 更新同步状态
        config.setSynState(TbSynConfigDTO.SYN_ONE);
        synConfigService.saveTbSynConfigDTO(config);
    }

    @Override
    public String getSyncStatus(String cid) {
        return synConfigService.getTbSynConfigDTO(cid)
                .map(config -> "状态: " + config.getSynState() +
                             ", 开始时间: " + config.getBeginSynTime() +
                             ", 结束时间: " + config.getEndSynTime())
                .orElse("未找到配置");
    }

    private boolean shouldSync(BaseQueryRequest queryRequest) {
        // 这里可以添加更复杂的同步条件判断
        return queryRequest.getParam("modifyBeginTime", String.class) != null;
    }

    private void processWithBackup(T data, String cid, Consumer<T> dataConsumer) {
        try {
            if (dataConsumer != null) {
                dataConsumer.accept(data);
            }
            // 成功处理后删除备份
            synQueueService.remove(SynQueueService.REDIS_BAK_QUEUE_PREFIX, cid);
        } catch (Exception e) {
            log.error("处理数据失败，cid: {}, error: {}", cid, e.getMessage(), e);
            throw e;
        }
    }
}