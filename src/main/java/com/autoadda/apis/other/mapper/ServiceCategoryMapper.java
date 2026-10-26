package com.autoadda.apis.other.mapper;

import com.autoadda.apis.other.util.Localized;
import com.autoadda.apis.other.entity.ServiceCategory;
import com.autoadda.apis.other.response.AvailableServiceResponse;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ServiceCategoryMapper {

    public AvailableServiceResponse toResponse(ServiceCategory serviceCategory) {
        AvailableServiceResponse response = new AvailableServiceResponse();
        response.setId(serviceCategory.getId());
        response.setCode(serviceCategory.getCode());
        response.setName(Localized.pick(serviceCategory.getName(), serviceCategory.getNameAr()));
        response.setDescription(Localized.pick(serviceCategory.getDescription(), serviceCategory.getDescriptionAr()));
        response.setNameAr(serviceCategory.getNameAr());
        response.setDescriptionAr(serviceCategory.getDescriptionAr());
        response.setDisplayOrder(serviceCategory.getDisplayOrder());
        response.setIsActive(serviceCategory.getIsActive());
        response.setParentId(serviceCategory.getParent() != null ? serviceCategory.getParent().getId() : null);
        response.setImage(serviceCategory.getImage());
        return response;
    }

    public ServiceCategory toEntity(AvailableServiceResponse response) {
        ServiceCategory serviceCategory = new ServiceCategory();
        serviceCategory.setId(response.getId());
        serviceCategory.setName(response.getName());
        serviceCategory.setDescription(response.getDescription());
        return serviceCategory;
    }

    public List<AvailableServiceResponse> toResponseList(List<ServiceCategory> entities) {
        if (entities == null || entities.isEmpty()) {
            return Collections.emptyList();
        }
        return entities.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

}
