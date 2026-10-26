package com.autoadda.apis.other.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The company and first service created by {@code POST /vendors/onboard}.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VendorOnboardingResponse {

    private VendorProfileResponse vendor;
    private VendorServiceResponse service;
}
