package com.autoadda.apis.other.request.vendor;

import lombok.Getter;
import lombok.Setter;

/**
 * Car Wash service form: the shared per-service fields plus car-wash specifics
 * (persisted to {@code car_wash_details}).
 */
@Getter
@Setter
public class CarWashVendorServiceRequest extends BaseVendorServiceRequest {

    private Integer bayCount;
    private Integer averageWashDurationMinutes;
    private Boolean mobileService;
    private Boolean selfServiceAvailable;
    private Boolean expressWashAvailable;
    private Boolean interiorDetailingAvailable;
    private Boolean waterRecycling;
    private Boolean acceptsWalkIns;
}
