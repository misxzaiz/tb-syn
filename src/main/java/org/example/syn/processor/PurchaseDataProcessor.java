package org.example.syn.processor;

import org.example.syn.core.DataProcessor;
import org.example.tb.demo.PurchaseInDTO;
import org.example.tb.demo.RandleData;
import org.example.tb.model.TbPageDTO;
import org.example.tb.model.TbPageReqDTO;
import org.springframework.stereotype.Component;

@Component
public class PurchaseDataProcessor implements DataProcessor<PurchaseInDTO> {

    private static RandleData randleData = new RandleData();

    @Override
    public TbPageDTO<PurchaseInDTO> process(TbPageReqDTO req) {
        // 转换为TbPageReqDTO供RandleData使用
        TbPageReqDTO pageReq = TbPageReqDTO.builder()
                .modifyBeginTime(req.getParam("modifyBeginTime"))
                .modifyEndTime(req.getParam("modifyEndTime"))
                .pageIndex(req.getPageIndex())
                .pageSize(req.getPageSize())
                .build();

        return randleData.page(pageReq);
    }
}