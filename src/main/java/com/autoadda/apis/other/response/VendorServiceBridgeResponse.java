package com.autoadda.apis.other.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for VendorServiceBridge entity.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VendorServiceBridgeResponse {
    private Integer id;
    private Boolean isActive;
    private Integer vendorId;
    private String vendorName;
    private Integer serviceCategoryId;
    private String serviceCategoryName;
}
