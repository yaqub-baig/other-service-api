package com.autoadda.apis.other.mapper;

import com.autoadda.apis.other.entity.ServiceCategory;
import com.autoadda.apis.other.response.AvailableServiceResponse;
import org.springframework.stereotype.Component;

@Component
public class ServiceCategoryMapper {

    public AvailableServiceResponse toResponse(ServiceCategory serviceCategory) {
        AvailableServiceResponse response = new AvailableServiceResponse();
        response.setId(serviceCategory.getId());
        response.setName(serviceCategory.getName());
        response.setDescription(serviceCategory.getDescription());
        return response;
    }

    public ServiceCategory toEntity(AvailableServiceResponse response) {
        ServiceCategory serviceCategory = new ServiceCategory();
        serviceCategory.setId(response.getId());
        serviceCategory.setName(response.getName());
        serviceCategory.setDescription(response.getDescription());
        return serviceCategory;
    }
}
