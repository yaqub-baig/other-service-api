package com.autoadda.apis.other.mapper;

import com.autoadda.apis.other.entity.VendorServiceBridge;
import com.autoadda.apis.other.response.VendorServiceBridgeResponse;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Manual mapper for VendorServiceBridge entity.
 */
@Component
public class VendorServiceBridgeMapper {

    public VendorServiceBridgeResponse toResponse(VendorServiceBridge entity) {
        return Optional.ofNullable(entity).map(e -> {
            VendorServiceBridgeResponse response = new VendorServiceBridgeResponse();
            response.setId(e.getId());
            response.setIsActive(e.getIsActive());
            response.setVendorId(e.getVendor() != null ? e.getVendor().getId() : null);
            response.setVendorName(e.getVendor() != null ? e.getVendor().getName() : null);
            response.setServiceCategoryId(e.getServiceCategory() != null ? e.getServiceCategory().getId() : null);
            response.setServiceCategoryName(e.getServiceCategory() != null ? e.getServiceCategory().getName() : null);
            return response;
        }).orElse(null);
    }

    public List<VendorServiceBridgeResponse> toResponseList(List<VendorServiceBridge> entities) {
        if (entities == null || entities.isEmpty()) {
            return Collections.emptyList();
        }
        return entities.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
}
