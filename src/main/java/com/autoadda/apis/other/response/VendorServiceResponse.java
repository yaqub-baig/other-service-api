package com.autoadda.apis.other.response;

import com.autoadda.apis.other.util.PricingType;
import com.autoadda.apis.other.util.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Full view of one service a vendor offers.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VendorServiceResponse {

    private Integer id;
    private Boolean isActive;
    private Integer vendorId;
    private String vendorName;
    private Integer serviceCategoryId;
    private String serviceCategoryName;

    private String serviceType;
    private String openingTime;
    private String closingTime;
    private String workingDays;
    private Integer concurrentAppointmentCapacity;
    private String tradeLicenceNumber;
    private Status accountStatus;

    private LocationResponse serviceLocation;
    private List<MediaFileResponse> serviceImages;
    private List<MediaFileResponse> tradeLicenseAttachment;
    private PricingType pricingType;
    private BigDecimal price;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private String currency;
    private String description;

    /** Values for the category's VENDOR attributes. */
    private Map<String, Object> attributes;
}
