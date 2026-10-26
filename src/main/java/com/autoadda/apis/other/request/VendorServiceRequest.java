package com.autoadda.apis.other.request;
import com.autoadda.apis.other.util.PricingType;
import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VendorServiceRequest {

	@NotNull(message = "Vendor ID is required")
	private Integer vendorId;

	/** Service category id; "serviceId" is still accepted for older clients. */
	@NotNull(message = "Service category ID is required")
	@JsonAlias("serviceId")
	private Integer serviceCategoryId;

	private boolean isActive;

	// Optional vendor-level pricing for this category
	private PricingType pricingType;
	private BigDecimal price;
	private BigDecimal minPrice;
	private BigDecimal maxPrice;
	private String currency;
	private String description;
}
