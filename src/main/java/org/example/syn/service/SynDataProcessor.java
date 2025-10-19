package org.example.syn.service;

import org.example.tb.demo.PurchaseInDTO;
import org.example.syn.template.PurchaseSyncTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.function.Consumer;

@Service
public class SynDataProcessor {
    
    @Resource
    private PurchaseSyncTemplate purchaseSyncTemplate;

    public void syn(String cid, Consumer<PurchaseInDTO> consumer) {
        purchaseSyncTemplate.syn(cid, consumer);
    }
}