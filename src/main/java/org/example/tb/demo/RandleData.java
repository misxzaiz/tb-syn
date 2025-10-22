package org.example.tb.demo;

import org.example.tb.model.TbPageDTO;
import org.example.tb.model.TbPageReqDTO;
import org.example.tb.model.TbTotalPageDTO;
import org.example.tb.util.SimpleRateLimiter;
import org.example.tb.util.TbPageUtil;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RandleData {


    private static final SimpleRateLimiter limiter = new SimpleRateLimiter(1000, 3);
    
    // 默认最大数据条数
    private static final int DEFAULT_MAX_COUNT = 10;
    // 可配置的最大数据条数
    private int maxDataCount = DEFAULT_MAX_COUNT;

    public static void main(String[] args) {
        RandleData randleData = new RandleData();
        TbPageReqDTO tbPageReqDTO = new TbPageReqDTO();
        tbPageReqDTO.setPageIndex(1);
        tbPageReqDTO.setPageSize(10);
        tbPageReqDTO.setModifyEndTime("2023-09-01");
        tbPageReqDTO.setModifyBeginTime("2023-08-01");

        TbTotalPageDTO<PurchaseInDTO> purchaseInDTOTbTotalPageDTO = TbPageUtil.totalPage(tbPageReqDTO, req -> randleData.page(req));

        System.out.println(purchaseInDTOTbTotalPageDTO);
    }



    public TbPageDTO<PurchaseInDTO> page(TbPageReqDTO req) {
        TbPageDTO<PurchaseInDTO> result = new TbPageDTO<>();
        
        // 根据时间参数计算总数据量，确保相同参数返回相同数据量
        int totalCount = calculateTotalCount(req);
        
        // 设置分页参数
        int pageIndex = req.getPageIndex() != null ? req.getPageIndex() : 1;
        int pageSize = req.getPageSize() != null ? req.getPageSize() : 10;
        
        // 计算分页信息
        int startIndex = (pageIndex - 1) * pageSize;
        int endIndex = Math.min(startIndex + pageSize, totalCount);
        int pageCount = (totalCount + pageSize - 1) / pageSize;
        boolean hasNext = pageIndex < pageCount;
        
        // 生成模拟数据
        List<PurchaseInDTO> dataList = generateMockData(startIndex, endIndex, req);
        
        // 设置返回结果
        result.setDatas(dataList);
        result.setPageIndex(pageIndex);
        result.setPageSize(pageSize);
        result.setDataCount(totalCount);
        result.setPageCount(pageCount);
        result.setHasNext(hasNext);
        
        return result;
    }
    
    /**
     * 根据查询参数计算总数据量
     * 使用ModifyBeginTime和ModifyEndTime的hash值确保相同参数返回相同数据量
     */
    private int calculateTotalCount(TbPageReqDTO req) {
        String key = (req.getModifyBeginTime() != null ? req.getModifyBeginTime() : "") + 
                     (req.getModifyEndTime() != null ? req.getModifyEndTime() : "");
        
        // 使用hash值计算数据量，确保相同参数返回相同结果
        int hash = key.hashCode();
        // 取绝对值并对最大数据量取模，确保在合理范围内
        int count = Math.abs(hash % maxDataCount) ;
        
        return count;
    }
    
    /**
     * 设置最大数据条数
     */
    public void setMaxDataCount(int maxDataCount) {
        this.maxDataCount = Math.max(20, maxDataCount); // 最小20条
    }
    
    /**
     * 获取当前最大数据条数
     */
    public int getMaxDataCount() {
        return maxDataCount;
    }
    
    private List<PurchaseInDTO> generateMockData(int startIndex, int endIndex, TbPageReqDTO req) {
//        if (!limiter.allowRequest()) {
//            throw new RuntimeException("调用太频繁");
//        }
        List<PurchaseInDTO> dataList = new ArrayList<>();

        // 使用查询参数作为随机种子，确保相同参数生成相同数据
        String seedKey = (req.getModifyBeginTime() != null ? req.getModifyBeginTime() : "") +
                        (req.getModifyEndTime() != null ? req.getModifyEndTime() : "") +
                        (req.getPageIndex() != null ? req.getPageIndex() : 1);
        Random random = new Random(seedKey.hashCode());

        String[] suppliers = {"供应商A", "供应商B", "供应商C", "供应商D", "供应商E"};
        String[] products = {"产品X", "产品Y", "产品Z", "产品M", "产品N"};
        String[] statuses = {"待审核", "已审核", "已驳回", "已完成"};
        String[] types = {"普通采购", "紧急采购", "计划采购"};

        // 用于记录已生成的随机数，避免单次请求内重复
        java.util.Set<Integer> usedRandomNumbers = new java.util.HashSet<>();

        for (int i = startIndex; i < endIndex; i++) {
            PurchaseInDTO dto = new PurchaseInDTO();

            // 生成1-100000的随机后缀，确保不重复
            int randomSuffix;
            int attempts = 0;
            do {
                randomSuffix = random.nextInt(100000) + 1;
                attempts++;
                // 如果尝试次数过多，说明可用数字快用完了，使用线性递增
                if (attempts > 1000) {
                    randomSuffix = (i + 1) % 100000 + 1;
                    break;
                }
            } while (usedRandomNumbers.contains(randomSuffix));
            usedRandomNumbers.add(randomSuffix);

            dto.setIoId("IO" + String.format("%06d", randomSuffix));
            dto.setPurchaseNo("P" + Math.abs(seedKey.hashCode()) + String.format("%05d", randomSuffix));
            dto.setSupplierName(suppliers[random.nextInt(suppliers.length)]);
            dto.setProductName(products[random.nextInt(products.length)]);
            dto.setProductCode("PC" + String.format("%05d", random.nextInt(100000)));
            dto.setQuantity(random.nextInt(100) + 1);
            dto.setUnitPrice(BigDecimal.valueOf(random.nextDouble() * 1000 + 10));
            dto.setTotalAmount(dto.getUnitPrice().multiply(BigDecimal.valueOf(dto.getQuantity())));
            dto.setPurchaseType(types[random.nextInt(types.length)]);
            dto.setStatus(statuses[random.nextInt(statuses.length)]);
            dto.setPurchaseDate(LocalDateTime.now().minusDays(random.nextInt(30)));
            dto.setCreateTime(LocalDateTime.now().minusDays(random.nextInt(60)));
            dto.setUpdateTime(LocalDateTime.now().minusHours(random.nextInt(24)));
            dto.setRemark("备注信息" + randomSuffix);

            dataList.add(dto);
        }

        return dataList;
    }
}
