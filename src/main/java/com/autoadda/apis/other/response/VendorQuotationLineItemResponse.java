package com.autoadda.apis.other.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VendorQuotationLineItemResponse {
    private Integer id;
    private String itemType;
    private String title;
    private String description;
    private Integer quantity;
    private String unit;
    private BigDecimal unitPrice;
    private BigDecimal discount;
    private BigDecimal taxRate;
    private BigDecimal totalPrice;
    private Integer displayOrder;
    private Boolean isOptional;
    private LocalDateTime createdAt;
}
