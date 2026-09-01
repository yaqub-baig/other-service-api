package com.autoadda.apis.other.request.vendor;

import lombok.Getter;
import lombok.Setter;

/**
 * Onboarding form for a Car Wash vendor.
 *
 * <p>Currently identical to {@link BaseVendorRequest} (the basic garage-style
 * form). Car-wash-specific fields can be added here later.</p>
 */
@Getter
@Setter
public class CarWashVendorRequest extends BaseVendorRequest {
}
