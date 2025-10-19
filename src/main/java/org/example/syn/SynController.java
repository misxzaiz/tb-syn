package org.example.syn;

import org.example.tb.demo.PurchaseInDTO;
import org.example.tb.demo.RandleData;
import org.example.tb.model.TbPageReqDTO;
import org.example.tb.model.TbTotalPageDTO;
import org.example.tb.util.TbPageUtil;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/syn")
public class SynController {

    private static RandleData randleData = new RandleData();

    @RequestMapping("/test")
    public Object test() {
        return "hello world";
    }

    @RequestMapping("/synTest")
    public Object synTest() {
        TbTotalPageDTO<PurchaseInDTO> total = TbPageUtil.totalPage(TbPageReqDTO.builder().build(), req -> randleData.page(req));

        return total;
    }
}
