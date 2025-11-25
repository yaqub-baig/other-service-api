package com.autoadda.apis.other.mapper;


import com.autoadda.apis.other.entity.AvailableService;
import com.autoadda.apis.other.entity.os.OsAvailableService;
import com.autoadda.apis.other.response.AvailableServiceResponse;
import org.springframework.stereotype.Component;

@Component
public class AvailableServiceMapper {
    public AvailableServiceResponse toResponse(OsAvailableService availableService) {
        AvailableServiceResponse availableServiceResponse = new AvailableServiceResponse ( );
        availableServiceResponse.setId ( availableService.getId ( ) );
        availableServiceResponse.setName ( availableService.getName ( ) );
        availableServiceResponse.setDescription ( availableService.getDescription ( ) );
//        availableServiceResponse.setPrice ( availableService.getPrice ( ) );
//        availableServiceResponse.setLaborPrice ( availableService.getLaborPrice ( ) );
        return availableServiceResponse;
    }
}
