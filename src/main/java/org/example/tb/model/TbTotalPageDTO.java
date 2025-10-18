package org.example.tb.model;

import lombok.Data;

import java.util.List;

@Data
public class TbTotalPageDTO<T> {
    private List<T> datas;
}
