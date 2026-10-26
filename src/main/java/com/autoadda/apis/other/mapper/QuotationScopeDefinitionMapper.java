package com.autoadda.apis.other.mapper;

import com.autoadda.apis.other.entity.QuotationScopeDefinition;
import com.autoadda.apis.other.response.QuotationScopeDefinitionResponse;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class QuotationScopeDefinitionMapper {

    public QuotationScopeDefinitionResponse toResponse(QuotationScopeDefinition entity) {
        return Optional.ofNullable(entity).map(e -> {
            QuotationScopeDefinitionResponse response = new QuotationScopeDefinitionResponse();
            response.setId(e.getId());
            response.setServiceCategoryId(e.getServiceCategory().getId());
            response.setServiceCategoryName(e.getServiceCategory().getName());
            if (e.getCatalogService() != null) {
                response.setServiceId(e.getCatalogService().getId());
                response.setServiceCode(e.getCatalogService().getCode());
            }
            response.setIsQuotationEnabled(e.getIsQuotationEnabled());
            response.setQuotationType(e.getQuotationType());
            response.setAutoExpiryHours(e.getAutoExpiryHours());
            response.setAllowMultiVendorBroadcast(e.getAllowMultiVendorBroadcast());
            response.setMaxBroadcastVendors(e.getMaxBroadcastVendors());
            response.setMinQuotesRequired(e.getMinQuotesRequired());
            response.setIsActive(e.getIsActive());
            response.setCreatedAt(e.getCreatedAt());
            response.setUpdatedAt(e.getUpdatedAt());
            return response;
        }).orElse(null);
    }

    public List<QuotationScopeDefinitionResponse> toResponseList(List<QuotationScopeDefinition> entities) {
        if (entities == null || entities.isEmpty()) {
            return Collections.emptyList();
        }
        return entities.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
}
