package org.example.syn.core.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.syn.core.util.DateUtil;

import java.util.HashMap;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageReqDTO {
    private String modifyBeginTime;
    private String modifyEndTime;
    private Integer pageIndex;
    private Integer pageSize;
    // 租户ID
    private String cid;

    // 动态查询参数
    private Map<String, Object> queryParams = new HashMap<>();

    // 获取参数
    @SuppressWarnings("unchecked")
    public <T> T getParam(String key) {
        Object value = queryParams.get(key);
        return value != null ? (T) value : null;
    }

    /**
     * 1-同步 2-不同步（如开始时间大于当前时间）
     */
    private Integer isSyn;

    public static final Integer SYN_ONE = 1;
    public static final Integer SYN_TWO = 2;

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String modifyBeginTime;
        private String modifyEndTime;
        private Integer pageIndex;
        private Integer pageSize;
        private Integer synIntervalSecond;
        private String cid;
        private Map<String, Object> queryParams = new HashMap<>();
        public Builder modifyBeginTime(String modifyBeginTime) {
            this.modifyBeginTime = modifyBeginTime;
            return this;
        }
        public Builder synIntervalSecond(Integer synIntervalSecond) {
            this.synIntervalSecond = synIntervalSecond;
            return this;
        }
        public Builder modifyEndTime(String modifyEndTime) {
            this.modifyEndTime = modifyEndTime;
            return this;
        }
        public Builder pageIndex(Integer pageIndex) {
            this.pageIndex = pageIndex;
            return this;
        }
        public Builder pageSize(Integer pageSize) {
            this.pageSize = pageSize;
            return this;
        }
        public Builder cid(String cid) {
            this.cid = cid;
            return this;
        }
        public Builder param(String key, Object value) {
            queryParams.put(key, value);
            return this;
        }
        public PageReqDTO build() {
            String dateTimeNowStr = DateUtil.dateTimeNowStr();
            if (modifyBeginTime == null) {
                modifyBeginTime = DateUtil.dateTimeBeforeDayStr(1);
            }
            if (synIntervalSecond != null) {
                modifyEndTime = DateUtil.dateTimeAfterSecond(modifyBeginTime, synIntervalSecond);
            }
            if (modifyEndTime == null) {
                modifyEndTime = dateTimeNowStr;
            } else if (DateUtil.isAfter(modifyEndTime, dateTimeNowStr)) {
                modifyEndTime = dateTimeNowStr;
            }
            if (pageIndex == null) {
                pageIndex = 1;
            }
            if (pageSize == null) {
                pageSize = 50;
            }
            PageReqDTO tbPageReqDTO = new PageReqDTO();
            tbPageReqDTO.modifyBeginTime = modifyBeginTime;
            tbPageReqDTO.modifyEndTime = modifyEndTime;
            tbPageReqDTO.pageIndex = pageIndex;
            tbPageReqDTO.pageSize = pageSize;
            tbPageReqDTO.cid = cid;
            tbPageReqDTO.queryParams = queryParams;
            tbPageReqDTO.isSyn = DateUtil.isAfter(modifyBeginTime, dateTimeNowStr) ? SYN_TWO : SYN_ONE;
            return tbPageReqDTO;
        }

    }


    // 计算偏移量
    public int getOffset() {
        return (pageIndex - 1) * pageSize;
    }

    private static boolean isInitialState(Integer synState) {
        return SynConfigDTO.SYN_TWO.equals(synState) ||
                SynConfigDTO.SYN_THREE.equals(synState);
    }

    /**
     * 根据同步配置构建查询请求
     */
    public static PageReqDTO build(SynConfigDTO config) {
        boolean isFirstSync = isInitialState(config.getSynState());
        String beginTime = isFirstSync ? config.getBeginSynTime() : config.getEndSynTime();
        String endTime = isFirstSync ? config.getEndSynTime() : null;

        return PageReqDTO.builder()
                .cid(config.getCid())
                .modifyBeginTime(beginTime)
                .modifyEndTime(endTime)
                .synIntervalSecond(config.getSynIntervalSecond())
                .build();
    }
}
