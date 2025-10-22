package org.example.syn.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.syn.entity.PurchaseIn;

import java.util.List;

@Mapper
public interface PurchaseInMapper {

    /**
     * 插入采购入库记录
     */
    int insert(PurchaseIn purchaseIn);

    /**
     * 批量插入采购入库记录
     */
    int insertBatch(@Param("list") List<PurchaseIn> list);

    /**
     * 根据ioId查询记录
     */
    PurchaseIn selectByIoId(@Param("ioId") String ioId, @Param("cid") String cid);

    /**
     * 更新记录
     */
    int updateByIoId(PurchaseIn purchaseIn);

    /**
     * 根据ioId删除记录
     */
    int deleteByIoId(@Param("ioId") String ioId, @Param("cid") String cid);

    /**
     * 查询所有记录
     */
    List<PurchaseIn> selectAll(@Param("cid") String cid);
}