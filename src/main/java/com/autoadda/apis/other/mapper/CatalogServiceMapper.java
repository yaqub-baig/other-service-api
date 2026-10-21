package com.autoadda.apis.other.mapper;

import com.autoadda.apis.other.entity.CatalogService;
import com.autoadda.apis.other.response.CatalogServiceResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CatalogServiceMapper {

    public CatalogServiceResponse toResponse(CatalogService e) {
        if (e == null) {
            return null;
        }
        return CatalogServiceResponse.builder()
                .id(e.getId())
                .serviceCategoryId(e.getServiceCategory() != null ? e.getServiceCategory().getId() : null)
                .serviceCategoryName(e.getServiceCategory() != null ? e.getServiceCategory().getName() : null)
                .code(e.getCode())
                .name(e.getName())
                .description(e.getDescription())
                .fulfillmentMode(e.getFulfillmentMode())
                .estimatedDurationMinutes(e.getEstimatedDurationMinutes())
                .displayOrder(e.getDisplayOrder())
                .isActive(e.getIsActive())
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
    }

    public List<CatalogServiceResponse> toResponseList(List<CatalogService> entities) {
        return entities.stream().map(this::toResponse).toList();
    }
}
