package org.example.tb.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.tb.util.TbDateUtil;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TbPageReqDTO {
    private String modifyBeginTime;
    private String modifyEndTime;
    private Integer pageIndex;
    private Integer pageSize;
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
        public TbPageReqDTO build() {
            String dateTimeNowStr = TbDateUtil.dateTimeNowStr();
            if (modifyBeginTime == null) {
                modifyBeginTime = TbDateUtil.dateTimeBeforeDayStr(1);
            }
            if (synIntervalSecond != null) {
                modifyEndTime = TbDateUtil.dateTimeAfterSecond(modifyBeginTime, synIntervalSecond);
            }
            if (modifyEndTime == null) {
                modifyEndTime = dateTimeNowStr;
            } else if (TbDateUtil.isAfter(modifyEndTime, dateTimeNowStr)) {
                modifyEndTime = dateTimeNowStr;
            }
            if (pageIndex == null) {
                pageIndex = 1;
            }
            if (pageSize == null) {
                pageSize = 50;
            }
            return new TbPageReqDTO(modifyBeginTime, modifyEndTime, pageIndex, pageSize, TbDateUtil.isAfter(modifyBeginTime, dateTimeNowStr) ? SYN_TWO : SYN_ONE);
        }

    }
}
