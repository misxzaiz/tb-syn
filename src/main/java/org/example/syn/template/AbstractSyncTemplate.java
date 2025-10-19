package org.example.syn.template;

import org.example.tb.model.TbPageReqDTO;
import org.example.tb.model.TbSynConfigDTO;
import org.example.tb.model.TbTotalPageDTO;
import org.example.tb.model.TbTotalPageReqDTO;
import org.example.syn.processor.DataProcessor;
import org.example.syn.service.SynConfigService;
import org.example.syn.service.SynQueueService;
import org.example.tb.util.TbPageUtil;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.function.Consumer;

public abstract class AbstractSyncTemplate<T> {
    
    @Resource
    protected SynConfigService synConfigService;
    
    @Resource
    protected SynQueueService<T> synQueueService;
    
    public final String syncData(String cid) {
        TbSynConfigDTO tbSynConfigDTO = synConfigService.getTbSynConfigDTO(cid).orElse(TbSynConfigDTO.init(cid));
        
        if (TbSynConfigDTO.SYN_TWO.equals(tbSynConfigDTO.getIsSyn())) {
            tbSynConfigDTO.setIsSyn(TbSynConfigDTO.SYN_ONE);
            synConfigService.saveTbSynConfigDTO(tbSynConfigDTO);
            return "初始化同步配置信息...";
        }
        
        TbPageReqDTO pageReq = buildPageRequest(tbSynConfigDTO);
        TbTotalPageDTO<T> resp = fetchData(pageReq);
        
        if (resp.getDatas() != null && !resp.getDatas().isEmpty()) {
            List<T> dataList = resp.getDatas();
            synQueueService.leftPush(SynQueueService.REDIS_QUEUE_PREFIX, cid, dataList);
        }
        
        updateSyncConfig(tbSynConfigDTO, pageReq);
        
        return resp.toString();
    }
    
    public final Object popData(String cid, Consumer<T> dataConsumer) {
        T data = synQueueService.rightPop(SynQueueService.REDIS_QUEUE_PREFIX, cid);
        
        if (data == null) {
            syncData(cid);
            return "数据同步";
        }
        
        String backupKey = generateBackupKey(data);
        synQueueService.bakData(SynQueueService.REDIS_BAK_QUEUE_PREFIX + getBackupPrefix(data) + ":", 
                cid, 
                backupKey, 
                data);
        
        if (dataConsumer != null) {
            dataConsumer.accept(data);
        }
        
        synQueueService.removeBakData(SynQueueService.REDIS_BAK_QUEUE_PREFIX + getBackupPrefix(data) + ":", 
                cid, 
                backupKey);
        
        return data;
    }
    
    protected TbPageReqDTO buildPageRequest(TbSynConfigDTO config) {
        return TbPageReqDTO.builder()
                .modifyBeginTime(config.getEndSynTime())
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
    
    protected void updateSyncConfig(TbSynConfigDTO config, TbPageReqDTO pageReq) {
        config.setIsSyn(TbSynConfigDTO.SYN_ONE);
        config.setBeginSynTime(pageReq.getModifyBeginTime());
        config.setEndSynTime(pageReq.getModifyEndTime());
        synConfigService.saveTbSynConfigDTO(config);
    }
    
    protected abstract DataProcessor<T> getDataProcessor();
    
    protected abstract String getBackupPrefix(T data);
    
    protected abstract String generateBackupKey(T data);
}