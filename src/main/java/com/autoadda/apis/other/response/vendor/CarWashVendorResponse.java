package com.autoadda.apis.other.response.vendor;

import com.autoadda.apis.other.response.VendorProfileResponse;
import com.autoadda.apis.other.response.VendorServiceResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Car Wash view: the vendor company, the car-wash service, and its detail block.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CarWashVendorResponse {

    private VendorProfileResponse vendor;
    private VendorServiceResponse service;
    private CarWashDetailsResponse carWashDetails;
}
