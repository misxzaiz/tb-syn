package org.example.syn.core;

import org.example.tb.model.TbPageReqDTO;
import org.example.tb.model.TbTotalPageDTO;

/**
 * 数据处理器接口
 * 核心抽象：定义如何处理特定类型的数据查询
 */
public interface DataProcessor<T> {
    /**
     * 处理查询请求，返回分页数据
     * @param req 查询请求
     * @return 分页数据
     */
    TbTotalPageDTO<T> process(TbPageReqDTO req);
}