package org.example.syn.template;

import org.example.tb.demo.PurchaseInDTO;
import org.example.syn.processor.DataProcessor;
import org.example.syn.processor.PurchaseDataProcessor;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class PurchaseSyncTemplate extends AbstractSyncTemplate<PurchaseInDTO> {
    
    @Resource
    private PurchaseDataProcessor purchaseDataProcessor;
    
    @Override
    protected DataProcessor<PurchaseInDTO> getDataProcessor() {
        return purchaseDataProcessor;
    }

    @Override
    protected Class<PurchaseInDTO> getDataType() {
        return PurchaseInDTO.class;
    }

}