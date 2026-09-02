package com.autoadda.apis.other.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VendorServiceRequest {

	@NotNull(message = "Vendor ID is required")
	private Integer vendorId;

	@NotNull(message = "Service category ID is required")
	private Integer serviceId;

	private boolean isActive;
}
