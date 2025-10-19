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

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String modifyBeginTime;
        private String modifyEndTime;
        private Integer pageIndex;
        private Integer pageSize;
        public Builder modifyBeginTime(String modifyBeginTime) {
            this.modifyBeginTime = modifyBeginTime;
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
            if (modifyBeginTime == null) {
                modifyBeginTime = TbDateUtil.dateTimeBeforeDayStr(1);
            }
            if (modifyEndTime == null) {
                modifyEndTime = TbDateUtil.dateTimeNowStr();
            }
            if (pageIndex == null) {
                pageIndex = 1;
            }
            if (pageSize == null) {
                pageSize = 50;
            }
            return new TbPageReqDTO(modifyBeginTime, modifyEndTime, pageIndex, pageSize);
        }

    }
}
