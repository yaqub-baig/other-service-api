package com.autoadda.apis.other.request;
import com.autoadda.apis.other.util.PricingType;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GarageServiceRequest {

	@NotNull(message = "Garage ID is required")
	private Integer garageId;

	@NotNull(message = "Service ID is required")
	private Integer serviceId;

	private boolean isActive;

	// Optional vendor-level pricing for this category
	private PricingType pricingType;
	private BigDecimal price;
	private BigDecimal minPrice;
	private BigDecimal maxPrice;
	private String currency;
	private String description;
}
