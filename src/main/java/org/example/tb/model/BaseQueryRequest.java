package org.example.tb.model;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * 基础查询请求类
 * 支持动态参数，便于扩展
 */
@Data
public class BaseQueryRequest {
    // 分页参数
    private Integer pageIndex = 1;
    private Integer pageSize = 50;

    // 租户ID
    private String cid;

    // 动态查询参数
    private Map<String, Object> queryParams = new HashMap<>();

    // 链式设置参数
    public BaseQueryRequest param(String key, Object value) {
        queryParams.put(key, value);
        return this;
    }

    // 获取参数
    @SuppressWarnings("unchecked")
    public <T> T getParam(String key, Class<T> clazz) {
        Object value = queryParams.get(key);
        return value != null ? (T) value : null;
    }

    // 设置时间范围（采购数据常用）
    public BaseQueryRequest timeRange(String beginTime, String endTime) {
        return param("modifyBeginTime", beginTime)
               .param("modifyEndTime", endTime);
    }

    // 计算偏移量
    public int getOffset() {
        return (pageIndex - 1) * pageSize;
    }
}