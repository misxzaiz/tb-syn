package org.example.syn.core.model;

import lombok.Data;

import java.util.List;

@Data
public class PageDTO<T> {
    private List<T> datas;
    private Integer pageIndex;
    private Boolean hasNext;
    private Integer dataCount;
    private Integer pageCount;
    private Integer pageSize;
}
