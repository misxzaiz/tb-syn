package org.example.syn.core.util;

import org.example.syn.core.model.PageDTO;
import org.example.syn.core.model.PageReqDTO;
import org.example.syn.core.model.TotalPageDTO;

import java.util.function.Function;

public class PageUtil {

    public static <T> TotalPageDTO<T> totalPage(PageReqDTO reqDTO, Function<PageReqDTO, PageDTO<T>> pageReqFunc) {
        // 保存原始页码和页大小
        Integer originalPageIndex = reqDTO.getPageIndex();
        Integer originalPageSize = reqDTO.getPageSize();
        
        try {
            // 设置查询第一页获取总页数信息
            reqDTO.setPageIndex(1);
            if (reqDTO.getPageSize() == null) {
                reqDTO.setPageSize(10);
            }
            
            // 查询第一页获取总页数
            PageDTO<T> firstPage = pageReqFunc.apply(reqDTO);
            if (firstPage == null || firstPage.getPageCount() == null || firstPage.getPageCount() <= 0) {
                return new TotalPageDTO<>();
            }
            
            // 准备结果对象
            TotalPageDTO<T> result = new TotalPageDTO<>();
            
            // 合并所有页面的数据
            java.util.List<T> allData = new java.util.ArrayList<>();
            
            // 添加第一页数据
            if (firstPage.getDatas() != null) {
                allData.addAll(firstPage.getDatas());
            }
            
            // 查询剩余页面
            Integer totalPages = firstPage.getPageCount();
            for (int pageIndex = 2; pageIndex <= totalPages; pageIndex++) {
                reqDTO.setPageIndex(pageIndex);
                PageDTO<T> pageData = pageReqFunc.apply(reqDTO);
                if (pageData != null && pageData.getDatas() != null) {
                    allData.addAll(pageData.getDatas());
                }
            }
            
            // 设置结果
            result.setDatas(allData);
            result.setDataCount(allData.size());
            result.setPageSize(reqDTO.getPageSize());
            result.setPageCount(totalPages);
            
            return result;
            
        } finally {
            // 恢复原始请求参数
            reqDTO.setPageIndex(originalPageIndex);
            reqDTO.setPageSize(originalPageSize);
        }
    }
}
