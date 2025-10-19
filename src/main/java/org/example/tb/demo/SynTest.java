package org.example.tb.demo;

import org.example.tb.model.TbPageDTO;
import org.example.tb.model.TbPageReqDTO;
import org.example.tb.model.TbTotalPageDTO;
import org.example.tb.util.TbPageUtil;

public class SynTest {

    private static RandleData randleData = new RandleData();

    public static void main(String[] args) {
        TbTotalPageDTO<PurchaseInDTO> total = TbPageUtil.totalPage(TbPageReqDTO.builder().build(), req -> randleData.page(req));

        System.out.println(total);
    }
}
