package com.autoadda.apis.other.mapper;

import com.autoadda.apis.other.entity.CatalogService;
import com.autoadda.apis.other.response.CatalogServiceResponse;
import com.autoadda.apis.other.util.Localized;
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
                .serviceCategoryName(e.getServiceCategory() != null
                        ? Localized.pick(e.getServiceCategory().getName(), e.getServiceCategory().getNameAr()) : null)
                .code(e.getCode())
                .name(Localized.pick(e.getName(), e.getNameAr()))
                .description(Localized.pick(e.getDescription(), e.getDescriptionAr()))
                .nameAr(e.getNameAr())
                .descriptionAr(e.getDescriptionAr())
                .quotationMode(e.getQuotationMode())
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
