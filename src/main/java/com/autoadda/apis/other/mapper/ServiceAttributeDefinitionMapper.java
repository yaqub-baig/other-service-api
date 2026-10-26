package com.autoadda.apis.other.mapper;

import com.autoadda.apis.other.util.Localized;
import com.autoadda.apis.other.entity.ServiceAttributeDefinition;
import com.autoadda.apis.other.response.ServiceAttributeDefinitionResponse;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class ServiceAttributeDefinitionMapper {

    private final ServiceCategoryMapper serviceCategoryMapper;

    public ServiceAttributeDefinitionMapper(ServiceCategoryMapper serviceCategoryMapper) {
        this.serviceCategoryMapper = serviceCategoryMapper;
    }

    public ServiceAttributeDefinitionResponse toResponse(ServiceAttributeDefinition entity) {
        return Optional.ofNullable(entity).map(e -> {
            ServiceAttributeDefinitionResponse response = new ServiceAttributeDefinitionResponse();
            response.setId(e.getId());
            response.setServiceCategoryId(e.getServiceCategory() != null ? e.getServiceCategory().getId() : null);
            response.setServiceCategoryName(e.getServiceCategory() != null ? e.getServiceCategory().getName() : null);
            if (e.getCatalogService() != null) {
                response.setServiceId(e.getCatalogService().getId());
                response.setServiceCode(e.getCatalogService().getCode());
            }
            if (e.getSchemaVersion() != null) {
                response.setSchemaVersionId(e.getSchemaVersion().getId());
                response.setSchemaVersionNo(e.getSchemaVersion().getVersionNo());
                response.setSchemaVersionStatus(e.getSchemaVersion().getStatus());
            }
            response.setAttributeCode(e.getAttributeCode());
            response.setAttributeName(Localized.pick(e.getAttributeName(), e.getAttributeNameAr()));
            response.setAttributeNameAr(e.getAttributeNameAr());
            response.setDescriptionAr(e.getDescriptionAr());
            response.setDataType(e.getDataType());
            response.setUnit(e.getUnit());
            response.setIsRequired(e.getIsRequired());
            response.setIsFilterable(e.getIsFilterable());
            response.setIsSearchable(e.getIsSearchable());
            response.setIsIndexed(e.getIsIndexed());
            response.setDisplayOrder(e.getDisplayOrder());
            response.setDescription(Localized.pick(e.getDescription(), e.getDescriptionAr()));
            response.setDefaultValue(e.getDefaultValue());
            response.setValidationRules(e.getValidationRules());
            response.setOptions(e.getOptions());
            response.setInputContext(e.getInputContext());
            response.setIsActive(e.getIsActive());
            response.setCreatedAt(e.getCreatedAt());
            response.setUpdatedAt(e.getUpdatedAt());
            return response;
        }).orElse(null);
    }

    public List<ServiceAttributeDefinitionResponse> toResponseList(List<ServiceAttributeDefinition> entities) {
        if (entities == null || entities.isEmpty()) {
            return Collections.emptyList();
        }
        return entities.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
}
