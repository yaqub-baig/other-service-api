package com.autoadda.apis.other.mapper;

import com.autoadda.apis.other.entity.OsAvailableService;
import com.autoadda.apis.other.response.AvailableServiceResponse;
import org.springframework.stereotype.Component;

@Component
public class OsAvailableServiceMapper {
    public AvailableServiceResponse toResponse(OsAvailableService availableService) {
        AvailableServiceResponse availableServiceResponse = new AvailableServiceResponse ( );
        availableServiceResponse.setId ( availableService.getId ( ) );
        availableServiceResponse.setName ( availableService.getName ( ) );
        availableServiceResponse.setDescription ( availableService.getDescription ( ) );
        return availableServiceResponse;
    }
}
