package com.autoadda.apis.other.mapper;

import com.autoadda.apis.other.entity.VendorService;
import com.autoadda.apis.other.response.VendorServiceResponse;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Manual mapper for VendorService entity.
 */
@Component
public class VendorServiceMapper {

    public VendorServiceResponse toResponse(VendorService entity) {
        return Optional.ofNullable(entity).map(e -> {
            VendorServiceResponse response = new VendorServiceResponse();
            response.setId(e.getId());
            response.setIsActive(e.getIsActive());
            response.setVendorId(e.getVendor() != null ? e.getVendor().getId() : null);
            response.setVendorName(e.getVendor() != null ? e.getVendor().getName() : null);
            response.setServiceCategoryId(e.getServiceCategory() != null ? e.getServiceCategory().getId() : null);
            response.setServiceCategoryName(e.getServiceCategory() != null ? e.getServiceCategory().getName() : null);
            response.setPricingType(e.getPricingType());
            response.setPrice(e.getPrice());
            response.setMinPrice(e.getMinPrice());
            response.setMaxPrice(e.getMaxPrice());
            response.setCurrency(e.getCurrency());
            response.setDescription(e.getDescription());
            return response;
        }).orElse(null);
    }

    public List<VendorServiceResponse> toResponseList(List<VendorService> entities) {
        if (entities == null || entities.isEmpty()) {
            return Collections.emptyList();
        }
        return entities.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
}
