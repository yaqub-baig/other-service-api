package com.autoadda.apis.other.response.vendor;

import com.autoadda.apis.other.response.VendorProfileResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Car Wash vendor view: the shared vendor profile plus the car-wash detail block.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CarWashVendorResponse {

    private VendorProfileResponse vendor;
    private CarWashDetailsResponse carWashDetails;
}
