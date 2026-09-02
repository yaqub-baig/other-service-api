package com.autoadda.apis.other.request.vendor;

import lombok.Getter;
import lombok.Setter;

/**
 * Combined convenience body for {@code POST /vendors/car-wash}: create the
 * company and its first car-wash service in one call.
 */
@Getter
@Setter
public class CarWashVendorRequest {

    private VendorCompanyRequest company;
    private CarWashVendorServiceRequest service;
}
