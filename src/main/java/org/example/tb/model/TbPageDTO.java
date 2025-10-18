package org.example.tb.model;

import lombok.Data;

import java.util.List;

@Data
public class TbPageDTO<T> {
    private List<T> datas;
    private Integer pageIndex;
    private Boolean hasNext;
    private Integer dataCount;
    private Integer pageCount;
    private Integer pageSize;
}
