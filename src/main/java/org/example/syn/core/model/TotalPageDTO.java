package org.example.syn.core.model;

import lombok.Data;

import java.util.List;

@Data
public class TotalPageDTO<T> {

    private Integer dataCount;

    private Integer pageSize;

    private Integer pageCount;

    private List<T> datas;
}
