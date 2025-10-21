package org.example.syn.web;

import org.example.syn.template.PurchaseSyncTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/syn")
public class SynController {

    @Resource
    private PurchaseSyncTemplate purchaseSyncTemplate;


    /**
     * 推送数据
     * @param cid 租户id
     * @return
     */
    @RequestMapping("/syn")
    public Object syn(@RequestParam String cid) {
        purchaseSyncTemplate.syn(cid, data -> {
            // 持久化
            System.out.println(data);
        });
        return "OK";
    }


}
