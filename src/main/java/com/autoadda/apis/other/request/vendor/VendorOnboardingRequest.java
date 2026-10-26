package com.autoadda.apis.other.request.vendor;

import lombok.Getter;
import lombok.Setter;

/**
 * Body for {@code POST /vendors/onboard}: create the company and its first service in one call.
 */
@Getter
@Setter
public class VendorOnboardingRequest {

    private VendorCompanyRequest company;
    private VendorServiceOnboardingRequest service;
}
