package org.example.tb.demo;

import lombok.Data;
import org.example.tb.model.TbBusinessKey;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PurchaseInDTO implements TbBusinessKey<String> {
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

    public String getTbBusinessKey() {
        return ioId;
    }
}
