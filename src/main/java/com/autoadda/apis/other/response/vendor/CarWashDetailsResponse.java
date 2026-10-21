package com.autoadda.apis.other.response.vendor;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CarWashDetailsResponse {

    private Integer bayCount;
    private Integer averageWashDurationMinutes;
    private Boolean mobileService;
    private Boolean selfServiceAvailable;
    private Boolean expressWashAvailable;
    private Boolean interiorDetailingAvailable;
    private Boolean waterRecycling;
    private Boolean acceptsWalkIns;
}
