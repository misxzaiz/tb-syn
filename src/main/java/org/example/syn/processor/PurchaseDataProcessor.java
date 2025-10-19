package org.example.syn.processor;

import org.example.tb.demo.PurchaseInDTO;
import org.example.tb.demo.RandleData;
import org.example.tb.model.TbPageReqDTO;
import org.example.tb.model.TbTotalPageDTO;
import org.example.tb.util.TbPageUtil;
import org.springframework.stereotype.Component;

@Component
public class PurchaseDataProcessor implements DataProcessor<PurchaseInDTO> {
    
    private static RandleData randleData = new RandleData();
    
    @Override
    public TbTotalPageDTO<PurchaseInDTO> process(TbPageReqDTO req) {
        return TbPageUtil.totalPage(req, request -> randleData.page(request));
    }
}