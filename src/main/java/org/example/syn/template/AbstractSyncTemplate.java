package org.example.syn.template;

import org.example.tb.model.TbPageReqDTO;
import org.example.tb.model.TbSynConfigDTO;
import org.example.tb.model.TbTotalPageDTO;
import org.example.tb.model.TbTotalPageReqDTO;
import org.example.syn.processor.DataProcessor;
import org.example.syn.service.SynConfigService;
import org.example.syn.service.SynQueueService;
import org.example.tb.util.TbPageUtil;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public abstract class AbstractSyncTemplate<T> {
    
    @Resource
    protected SynConfigService synConfigService;
    
    @Resource
    protected SynQueueService<T> synQueueService;
    
    public final String syncData(String cid) {
        TbSynConfigDTO config = synConfigService.getTbSynConfigDTO(cid).orElse(TbSynConfigDTO.init(cid));
        
        if (TbSynConfigDTO.SYN_TWO.equals(config.getSynState())) {
            config.setSynState(TbSynConfigDTO.SYN_ONE);
            synConfigService.saveTbSynConfigDTO(config);
            return "初始化同步配置信息...";
        }
        
        TbPageReqDTO pageReq = buildPageRequest(config);
        updateSyncConfigForSyncing(config, pageReq);

        if (TbPageReqDTO.SYN_TWO.equals(pageReq.getIsSyn())) {
            return "同步时间未到达，等待中...";
        }
        
        TbTotalPageDTO<T> response = fetchData(pageReq);
        pushDataToQueue(response, cid);
        
        config.setSynState(TbSynConfigDTO.SYN_ONE);
        synConfigService.saveTbSynConfigDTO(config);

        return response.toString();
    }

    public void syn(String cid, Consumer<T> dataConsumer) {
        T data = synQueueService.rightPop(SynQueueService.REDIS_QUEUE_PREFIX, cid);

        if (data == null) {
            // TODO 异步发起然后退出
            syncData(cid);
            return;
        }

        processDataWithBackup(data, cid, dataConsumer);

        int count = 0;
        while (true) {
            if (++count > 100) {
                return;
            }
            data = synQueueService.rightPop(SynQueueService.REDIS_QUEUE_PREFIX, cid);
            if (data == null) {
                return;
            }
            processDataWithBackup(data, cid, dataConsumer);
        }
    }

    public final Object popData(String cid, Consumer<T> dataConsumer) {
        T data = synQueueService.rightPop(SynQueueService.REDIS_QUEUE_PREFIX, cid);
        
        if (data == null) {
            syncData(cid);
            return "数据同步";
        }
        
        processDataWithBackup(data, cid, dataConsumer);
        
        return data;
    }
    
    protected TbPageReqDTO buildPageRequest(TbSynConfigDTO config) {
        boolean isFirstSync = isInitialState(config.getSynState());
        return TbPageReqDTO.builder()
                .modifyBeginTime(isFirstSync ? config.getBeginSynTime() : config.getEndSynTime())
                .modifyEndTime(isFirstSync ? config.getEndSynTime() : null)
                .synIntervalSecond(config.getSynIntervalSecond())
                .build();
    }
    
    protected TbTotalPageDTO<T> fetchData(TbPageReqDTO pageReq) {
        DataProcessor<T> processor = getDataProcessor();
        TbTotalPageReqDTO<T> req = TbTotalPageReqDTO.<T>builder()
                .pageReq(pageReq)
                .pageReqFunc(processor::process)
                .build();
        
        return TbPageUtil.totalPage(req);
    }
    
    private void updateSyncConfigForSyncing(TbSynConfigDTO config, TbPageReqDTO pageReq) {
        config.setBeginSynTime(pageReq.getModifyBeginTime());
        config.setEndSynTime(pageReq.getModifyEndTime());
        config.setSynState(TbSynConfigDTO.SYN_THREE);
        synConfigService.saveTbSynConfigDTO(config);
    }
    
    private void pushDataToQueue(TbTotalPageDTO<T> response, String cid) {
        if (response.getDatas() != null && !response.getDatas().isEmpty()) {
            synQueueService.leftPush(SynQueueService.REDIS_QUEUE_PREFIX, cid, response.getDatas());
        }
    }
    
    private void processDataWithBackup(T data, String cid, Consumer<T> dataConsumer) {
        String backupPrefix = SynQueueService.REDIS_BAK_QUEUE_PREFIX + getBackupPrefix(data) + ":";
        String backupKey = generateBackupKey(data);
        
        synQueueService.bakData(backupPrefix, cid, backupKey, data);
        
        if (dataConsumer != null) {
            dataConsumer.accept(data);
        }
        
        synQueueService.removeBakData(backupPrefix, cid, backupKey);
    }
    
    private boolean isInitialState(Integer synState) {
        return TbSynConfigDTO.SYN_TWO.equals(synState) || TbSynConfigDTO.SYN_THREE.equals(synState);
    }
    
    protected abstract DataProcessor<T> getDataProcessor();
    
    protected abstract String getBackupPrefix(T data);
    
    protected abstract String generateBackupKey(T data);
}