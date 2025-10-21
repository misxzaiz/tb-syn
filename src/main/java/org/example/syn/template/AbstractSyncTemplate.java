package org.example.syn.template;

import lombok.extern.slf4j.Slf4j;
import org.example.tb.model.TbPageReqDTO;
import org.example.tb.model.TbSynConfigDTO;
import org.example.tb.model.TbTotalPageDTO;
import org.example.tb.model.TbTotalPageReqDTO;
import org.example.syn.processor.DataProcessor;
import org.example.syn.service.SynConfigService;
import org.example.syn.service.SynQueueService;
import org.example.tb.util.TbPageUtil;

import javax.annotation.Resource;
import java.util.function.Consumer;

@Slf4j
public abstract class AbstractSyncTemplate<T> {
    
    @Resource
    protected SynConfigService synConfigService;
    
    @Resource
    protected SynQueueService<T> synQueueService;
    
    public final void syncData(String cid) {
        TbSynConfigDTO config = synConfigService.getTbSynConfigDTO(cid).orElse(TbSynConfigDTO.init(cid));
        
        if (TbSynConfigDTO.SYN_TWO.equals(config.getSynState())) {
            config.setSynState(TbSynConfigDTO.SYN_ONE);
            synConfigService.saveTbSynConfigDTO(config);
            log.info("初始化同步配置信息...");
        }
        
        TbPageReqDTO pageReq = buildPageRequest(config);
        updateSyncConfigForSyncing(config, pageReq);

        if (TbPageReqDTO.SYN_TWO.equals(pageReq.getIsSyn())) {
            log.info("同步时间未到达，等待中...");
        }
        
        TbTotalPageDTO<T> response = fetchData(pageReq);
        pushDataToQueue(response, cid);
        
        config.setSynState(TbSynConfigDTO.SYN_ONE);
        synConfigService.saveTbSynConfigDTO(config);
    }

    public void syn(String cid, Consumer<T> dataConsumer) {
        T data = synQueueService.popAndBak(SynQueueService.REDIS_QUEUE_PREFIX, cid);

        if (data == null) {
            syncData(cid);
            return;
        }

        processDataWithBackup(data, cid, dataConsumer);

        int count = 0;
        while (true) {
            if (++count > 100) {
                return;
            }
            data = synQueueService.popAndBak(SynQueueService.REDIS_QUEUE_PREFIX, cid);
            if (data == null) {
                return;
            }
            processDataWithBackup(data, cid, dataConsumer);
        }
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
        if (dataConsumer != null) {
            dataConsumer.accept(data);
        }

        synQueueService.removeBakData(SynQueueService.REDIS_BAK_QUEUE_PREFIX, cid);
    }
    
    private boolean isInitialState(Integer synState) {
        return TbSynConfigDTO.SYN_TWO.equals(synState) || TbSynConfigDTO.SYN_THREE.equals(synState);
    }
    
    protected abstract DataProcessor<T> getDataProcessor();
}