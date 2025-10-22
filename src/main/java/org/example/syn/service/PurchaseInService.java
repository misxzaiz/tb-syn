package org.example.syn.service;

import org.example.syn.model.entity.PurchaseIn;
import org.example.syn.mapper.PurchaseInMapper;
import org.example.syn.model.dto.TbPurchaseInDTO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class PurchaseInService {

    @Resource
    private PurchaseInMapper purchaseInMapper;

    /**
     * 保存采购入库数据
     */
    @Transactional
    public void savePurchaseIn(TbPurchaseInDTO dto, String cid) {
        // 检查是否已存在
        PurchaseIn existing = purchaseInMapper.selectByIoId(dto.getIoId(), cid);

        PurchaseIn purchaseIn = new PurchaseIn();
        BeanUtils.copyProperties(dto, purchaseIn);
        purchaseIn.setCid(cid);

        if (existing == null) {
            // 新增
            if (purchaseIn.getCreateTime() == null) {
                purchaseIn.setCreateTime(LocalDateTime.now());
            }
            purchaseIn.setUpdateTime(LocalDateTime.now());
            purchaseInMapper.insert(purchaseIn);
        } else {
            // 更新
            purchaseIn.setUpdateTime(LocalDateTime.now());
            purchaseInMapper.updateByIoId(purchaseIn);
        }
    }

    /**
     * 批量保存采购入库数据
     */
    @Transactional
    public void batchSavePurchaseIn(List<TbPurchaseInDTO> dtoList, String cid) {
        if (dtoList == null || dtoList.isEmpty()) {
            return;
        }

        List<PurchaseIn> insertList = new ArrayList<>();
        List<PurchaseIn> updateList = new ArrayList<>();

        for (TbPurchaseInDTO dto : dtoList) {
            PurchaseIn existing = purchaseInMapper.selectByIoId(dto.getIoId(), cid);
            PurchaseIn purchaseIn = new PurchaseIn();
            BeanUtils.copyProperties(dto, purchaseIn);
            purchaseIn.setCid(cid);

            if (existing == null) {
                if (purchaseIn.getCreateTime() == null) {
                    purchaseIn.setCreateTime(LocalDateTime.now());
                }
                purchaseIn.setUpdateTime(LocalDateTime.now());
                insertList.add(purchaseIn);
            } else {
                purchaseIn.setUpdateTime(LocalDateTime.now());
                updateList.add(purchaseIn);
            }
        }

        // 批量插入
        if (!insertList.isEmpty()) {
            purchaseInMapper.insertBatch(insertList);
        }

        // 批量更新
        for (PurchaseIn purchaseIn : updateList) {
            purchaseInMapper.updateByIoId(purchaseIn);
        }
    }

    /**
     * 根据ioId查询
     */
    public PurchaseIn findByIoId(String ioId, String cid) {
        return purchaseInMapper.selectByIoId(ioId, cid);
    }

    /**
     * 查询所有记录
     */
    public List<PurchaseIn> findAll(String cid) {
        return purchaseInMapper.selectAll(cid);
    }

    /**
     * 删除记录
     */
    @Transactional
    public void deleteByIoId(String ioId, String cid) {
        purchaseInMapper.deleteByIoId(ioId, cid);
    }
}