package com.autoadda.apis.other.mapper;

import com.autoadda.apis.other.util.Localized;
import com.autoadda.apis.other.entity.QuotationFieldDefinition;
import com.autoadda.apis.other.response.QuotationFieldDefinitionResponse;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class QuotationFieldDefinitionMapper {

    public QuotationFieldDefinitionResponse toResponse(QuotationFieldDefinition entity) {
        return Optional.ofNullable(entity).map(e -> {
            QuotationFieldDefinitionResponse response = new QuotationFieldDefinitionResponse();
            response.setId(e.getId());
            response.setServiceCategoryId(e.getServiceCategory().getId());
            response.setServiceCategoryName(e.getServiceCategory().getName());
            if (e.getCatalogService() != null) {
                response.setServiceId(e.getCatalogService().getId());
                response.setServiceCode(e.getCatalogService().getCode());
            }
            response.setFieldCode(e.getFieldCode());
            response.setLabel(Localized.pick(e.getLabel(), e.getLabelAr()));
            response.setLabelAr(e.getLabelAr());
            response.setPlaceholderAr(e.getPlaceholderAr());
            response.setHelpTextAr(e.getHelpTextAr());
            response.setFieldType(e.getFieldType());
            response.setIsRequired(e.getIsRequired());
            response.setDisplayOrder(e.getDisplayOrder());
            response.setPlaceholder(Localized.pick(e.getPlaceholder(), e.getPlaceholderAr()));
            response.setHelpText(Localized.pick(e.getHelpText(), e.getHelpTextAr()));
            response.setDefaultValue(e.getDefaultValue());
            response.setOptions(e.getOptions());
            response.setValidationRules(e.getValidationRules());
            response.setDependsOnFieldId(e.getDependsOn() != null ? e.getDependsOn().getId() : null);
            response.setDependsOnCondition(e.getDependsOnCondition());
            response.setIsActive(e.getIsActive());
            response.setCreatedAt(e.getCreatedAt());
            response.setUpdatedAt(e.getUpdatedAt());
            return response;
        }).orElse(null);
    }

    public List<QuotationFieldDefinitionResponse> toResponseList(List<QuotationFieldDefinition> entities) {
        if (entities == null || entities.isEmpty()) {
            return Collections.emptyList();
        }
        return entities.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
}
