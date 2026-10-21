package com.autoadda.apis.other.mapper;

import com.autoadda.apis.other.util.Localized;
import com.autoadda.apis.other.entity.FilterOption;
import com.autoadda.apis.other.entity.ServiceFilterDefinition;
import com.autoadda.apis.other.response.FilterOptionResponse;
import com.autoadda.apis.other.response.ServiceFilterDefinitionResponse;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Manual mapper for ServiceFilterDefinition and FilterOption entities.
 */
@Component
public class ServiceFilterDefinitionMapper {

    public ServiceFilterDefinitionResponse toResponse(ServiceFilterDefinition entity) {
        return Optional.ofNullable(entity).map(e -> {
            ServiceFilterDefinitionResponse response = new ServiceFilterDefinitionResponse();
            response.setId(e.getId());
            response.setServiceCategoryId(e.getServiceCategory() != null ? e.getServiceCategory().getId() : null);
            response.setServiceCategoryName(e.getServiceCategory() != null ? e.getServiceCategory().getName() : null);
            response.setFilterCode(e.getFilterCode());
            response.setAttributeDefinitionId(e.getAttributeDefinition() != null ? e.getAttributeDefinition().getId() : null);
            response.setFilterLabel(Localized.pick(e.getFilterLabel(), e.getFilterLabelAr()));
            response.setFilterLabelAr(e.getFilterLabelAr());
            response.setFilterType(e.getFilterType());
            response.setDataSource(e.getDataSource());
            response.setDataSourceConfig(e.getDataSourceConfig());
            response.setDependsOn(e.getDependsOn());
            response.setDisplayOrder(e.getDisplayOrder());
            response.setIsRequired(e.getIsRequired());
            response.setAllowMultiple(e.getAllowMultiple());
            response.setPlaceholder(e.getPlaceholder());
            response.setHelpText(e.getHelpText());
            response.setMinValue(e.getMinValue());
            response.setMaxValue(e.getMaxValue());
            response.setStepValue(e.getStepValue());
            response.setIsActive(e.getIsActive());
            response.setOptions(toOptionResponseList(e.getOptions()));
            response.setCreatedAt(e.getCreatedAt());
            response.setUpdatedAt(e.getUpdatedAt());
            return response;
        }).orElse(null);
    }

    public List<ServiceFilterDefinitionResponse> toResponseList(List<ServiceFilterDefinition> entities) {
        if (entities == null || entities.isEmpty()) {
            return Collections.emptyList();
        }
        return entities.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public FilterOptionResponse toOptionResponse(FilterOption entity) {
        return Optional.ofNullable(entity).map(e -> {
            FilterOptionResponse response = new FilterOptionResponse();
            response.setId(e.getId());
            response.setOptionValue(e.getOptionValue());
            response.setOptionLabel(Localized.pick(e.getOptionLabel(), e.getOptionLabelAr()));
            response.setOptionLabelAr(e.getOptionLabelAr());
            response.setDisplayOrder(e.getDisplayOrder());
            response.setIsDefault(e.getIsDefault());
            response.setIsActive(e.getIsActive());
            response.setIconUrl(e.getIconUrl());
            response.setParentValue(e.getParentValue());
            response.setMetadata(e.getMetadata());
            return response;
        }).orElse(null);
    }

    public List<FilterOptionResponse> toOptionResponseList(List<FilterOption> entities) {
        if (entities == null || entities.isEmpty()) {
            return Collections.emptyList();
        }
        return entities.stream()
                .map(this::toOptionResponse)
                .collect(Collectors.toList());
    }
}
