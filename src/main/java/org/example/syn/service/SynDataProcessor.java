package org.example.syn.service;

import org.example.tb.demo.PurchaseInDTO;
import org.example.tb.model.TbPageReqDTO;
import org.example.tb.model.TbTotalPageDTO;
import org.example.tb.util.TbPageUtil;
import org.example.syn.processor.PurchaseDataProcessor;
import org.example.syn.template.PurchaseSyncTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.function.Consumer;

@Service
public class SynDataProcessor {
    
    @Resource
    private PurchaseSyncTemplate purchaseSyncTemplate;
    
    @Resource
    private PurchaseDataProcessor purchaseDataProcessor;
    
    public TbTotalPageDTO<PurchaseInDTO> synTestData() {
        return TbPageUtil.totalPage(TbPageReqDTO.builder().build(), purchaseDataProcessor::process);
    }
    
    public String pushDate(String cid) {
        return purchaseSyncTemplate.syncData(cid);
    }
    
    public Object popDate(String cid, Consumer<PurchaseInDTO> consumer) {
        return purchaseSyncTemplate.popData(cid, consumer);
    }
}