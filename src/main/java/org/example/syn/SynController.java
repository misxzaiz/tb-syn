package org.example.syn;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import org.example.tb.demo.PurchaseInDTO;
import org.example.tb.demo.RandleData;
import org.example.tb.model.TbPageReqDTO;
import org.example.tb.model.TbSynConfigDTO;
import org.example.tb.model.TbTotalPageDTO;
import org.example.tb.model.TbTotalPageReqDTO;
import org.example.tb.util.TbDateUtil;
import org.example.tb.util.TbPageUtil;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.*;

@RestController
@RequestMapping("/syn")
public class SynController {
    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    public static String REDIS_KEY_PREFIX = "tb:syn:config:cid:";
    public static String REDIS_QUEUE_PREFIX = "tb:syn:queue:cid:";

    private static RandleData randleData = new RandleData();

    private static Integer ct = 1;
    @RequestMapping("/pt")
    public Object pt() {
        List<Integer> list = new ArrayList<>();
        list.add(ct++);
        list.add(ct++);
        Collections.reverse(list);

        // 将数据推送到Redis队列
        redisTemplate.opsForList().leftPushAll("ctp", list.toArray());

        List<String> list1 = new ArrayList<>();
        list1.add("ct++");
        list1.add("ct+1+");
        Collections.reverse(list1);
        // 将数据推送到Redis队列
        redisTemplate.opsForList().leftPushAll("ctps", list1.toArray());

        return "你好";
    }

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
    public Object pushDate(@RequestParam String cid) {
        // 1. 查询配置（redis）
        TbSynConfigDTO tbSynConfigDTO = getTbSynConfigDTO(cid).orElse(TbSynConfigDTO.init(cid));

        if (TbSynConfigDTO.SYN_TWO.equals(tbSynConfigDTO.getIsSyn())) {
            tbSynConfigDTO.setIsSyn(TbSynConfigDTO.SYN_ONE);
            saveTbSynConfigDTO(tbSynConfigDTO);
            return "初始化同步配置信息...";
        }

        TbPageReqDTO pageReq = TbPageReqDTO.builder()
                .modifyBeginTime(tbSynConfigDTO.getLastSynTime())
                .synIntervalSecond(tbSynConfigDTO.getSynIntervalSecond())
                .build();
        TbTotalPageReqDTO<PurchaseInDTO> req = TbTotalPageReqDTO.<PurchaseInDTO>builder()
                .pageReq(pageReq)
                .pageReqFunc(randleData::page)
                .build();



        // 2. 查询数据
        TbTotalPageDTO<PurchaseInDTO> resp = TbPageUtil.totalPage(req);

        // 3. push数据
        // 3. push数据到Redis队列
        if (resp.getDatas() != null && !resp.getDatas().isEmpty()) {
            String queueKey = REDIS_QUEUE_PREFIX + cid;
            List<PurchaseInDTO> dataList = resp.getDatas();

            // 将数据推送到Redis队列
            Collections.reverse(dataList);
            redisTemplate.opsForList().leftPushAll(queueKey, dataList);
        }

        // 修改最后更新时间
        tbSynConfigDTO.setIsSyn(TbSynConfigDTO.SYN_ONE);
        tbSynConfigDTO.setLastSynTime(pageReq.getModifyEndTime());
        saveTbSynConfigDTO(tbSynConfigDTO);

        return resp;
    }

    private void saveTbSynConfigDTO(TbSynConfigDTO tbSynConfigDTO) {
        stringRedisTemplate.opsForValue().set(REDIS_KEY_PREFIX + tbSynConfigDTO.getCid(), JSONUtil.toJsonStr(tbSynConfigDTO));
    }

    private Optional<TbSynConfigDTO> getTbSynConfigDTO(String cid) {
        // TODO 并发问题？
        String json = stringRedisTemplate.opsForValue().get(REDIS_KEY_PREFIX + cid);

        return Optional.ofNullable(StrUtil.isBlank(json) ? null : JSONUtil.toBean(json, TbSynConfigDTO.class));
    }


}
