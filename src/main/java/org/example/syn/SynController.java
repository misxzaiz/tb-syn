package org.example.syn;

import org.example.tb.demo.PurchaseInDTO;
import org.example.tb.demo.RandleData;
import org.example.tb.model.TbPageReqDTO;
import org.example.tb.model.TbTotalPageDTO;
import org.example.tb.model.TbTotalPageReqDTO;
import org.example.tb.util.TbDateUtil;
import org.example.tb.util.TbPageUtil;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/syn")
public class SynController {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    private static RandleData randleData = new RandleData();

    @RequestMapping("/test")
    public Object test(@RequestParam String name) {
        stringRedisTemplate.opsForValue().set("test", name);
        String test = stringRedisTemplate.opsForValue().get("test");

        return "你好:" + test;
    }

    @RequestMapping("/synTest")
    public Object synTest() {
        TbTotalPageDTO<PurchaseInDTO> total = TbPageUtil.totalPage(TbPageReqDTO.builder().build(), req -> randleData.page(req));

        return total;
    }

    /**
     * 推送数据
     * @param cid 租户id
     * @return
     */
    @RequestMapping("/pushDate")
    public Object pushDate(@RequestParam Integer cid) {
        // 1. 查询配置（redis）
        // 查询最后更新时间？
        String lastSynTime = TbDateUtil.dateTimeBeforeDayStr(7);
        TbTotalPageReqDTO<PurchaseInDTO> req = TbTotalPageReqDTO.<PurchaseInDTO>builder()
                .pageReq(TbPageReqDTO.builder()
                        .modifyBeginTime(lastSynTime)
                        .modifyEndTime(TbDateUtil.dateTimeNowStr())
                        .build())
                .pageReqFunc(randleData::page)
                .build();



        // 2. 查询数据
        TbTotalPageDTO<PurchaseInDTO> dates = TbPageUtil.totalPage(req);

        // 3. push数据

        return dates;
    }


}
