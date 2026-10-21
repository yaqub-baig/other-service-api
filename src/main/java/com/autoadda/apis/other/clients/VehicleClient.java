package com.autoadda.apis.other.clients;

import com.autoadda.apis.other.response.VehicleDetailResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "vehicle-api", url = "${clients.vehicle-api.url}")
public interface VehicleClient {

    @GetMapping("/vehicles/isc/{vehicleId}")
    VehicleDetailResponse getVehicleDetailsByIdResponse(
            @PathVariable("vehicleId") Integer vehicleId
    );
}
