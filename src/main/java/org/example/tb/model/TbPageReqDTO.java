package org.example.tb.model;

import lombok.Data;

@Data
public class TbPageReqDTO {
    private String modifyBeginTime;
    private String modifyEndTime;
    private Integer pageIndex;
    private Integer pageSize;
}
