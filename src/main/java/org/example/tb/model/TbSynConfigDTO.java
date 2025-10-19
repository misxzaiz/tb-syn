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
    private String lastSynTime;
    private Integer synIntervalSecond;
    /**
     * 是否同步 0-不同步 1-同步 2-初始化
     */
    private Integer isSyn;

    public static TbSynConfigDTO init(String cid) {
        return TbSynConfigDTO.builder()
                .lastSynTime(TbDateUtil.dateTimeNowStr())
                .cid(cid)
                .synIntervalSecond(60 * 10)
                .isSyn(SYN_TWO)
                .build();
    }

    public static Integer SYN_ZERO = 0;
    public static Integer SYN_ONE = 1;
    public static Integer SYN_TWO = 2;
}
