package org.example.tb.model;

import lombok.Data;

import java.util.List;

@Data
public class TbTotalPageDTO<T> {

    private Integer dataCount;

    private Integer pageSize;

    private Integer pageCount;

    private List<T> datas;
}
