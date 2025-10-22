package org.example.syn.entity;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PurchaseIn implements Serializable {
    private Long id;
    private String ioId;
    private String purchaseNo;
    private String supplierName;
    private String productName;
    private String productCode;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalAmount;
    private String purchaseType;
    private String status;
    private LocalDateTime purchaseDate;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private String remark;
    private String cid; // 租户ID
}