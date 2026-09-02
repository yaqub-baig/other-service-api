package com.autoadda.apis.other.request.vendor;

import lombok.Getter;
import lombok.Setter;

/**
 * Onboarding / edit form for a Car Wash vendor.
 *
 * <p>Inherits the shared basic form from {@link BaseVendorRequest} and adds the
 * car-wash-specific fields that are persisted to {@code car_wash_details}.
 * On a 2-step wizard, step 1 sends the base fields and step 2 sends these.</p>
 */
@Getter
@Setter
public class CarWashVendorRequest extends BaseVendorRequest {

    private Integer bayCount;
    private Integer averageWashDurationMinutes;
    private Boolean mobileService;
    private Boolean selfServiceAvailable;
    private Boolean expressWashAvailable;
    private Boolean interiorDetailingAvailable;
    private Boolean waterRecycling;
    private Boolean acceptsWalkIns;
}
