package org.example.tb.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.tb.util.TbDateUtil;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TbSynConfigDTO {
    private String cid;
    /**
     * 同步开始时间
     */
    private String beginSynTime;
    /**
     * 同步结束时间
     */
    private String endSynTime;
    private Integer synIntervalSecond;
    /**
     * 是否同步 0-不同步 1-同步 2-初始化 3-push数据中（如果下次还是这个状态，则代表上次push数据失败，push数据，和修改状态使用lua保证原子性）
     */
    private Integer synState;

    public static TbSynConfigDTO init(String cid) {
        String dateTimeNowStr = TbDateUtil.dateTimeNowStr();
        // FIXME 10分钟
//        int synIntervalSecond = 60 * 10;
        int synIntervalSecond = 5;
        return TbSynConfigDTO.builder()
                .beginSynTime(dateTimeNowStr)
                .endSynTime(TbDateUtil.dateTimeAfterSecond(dateTimeNowStr, synIntervalSecond))
                .cid(cid)
                .synIntervalSecond(synIntervalSecond)
                .synState(SYN_TWO)
                .build();
    }

    public static Integer SYN_ZERO = 0;
    public static Integer SYN_ONE = 1;
    public static Integer SYN_TWO = 2;
    public static Integer SYN_THREE = 3;
}
