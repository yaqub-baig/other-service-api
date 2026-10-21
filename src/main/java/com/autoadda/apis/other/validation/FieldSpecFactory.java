package com.autoadda.apis.other.validation;

import com.autoadda.apis.other.entity.QuotationFieldDefinition;
import com.autoadda.apis.other.entity.ServiceAttributeDefinition;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Builds FieldSpecs from the two kinds of definitions the service stores.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FieldSpecFactory {

    private final ObjectMapper objectMapper;

    /**
     * Spec for an offering attribute. CUSTOMER-only attributes are never required on an offering.
     */
    public FieldSpec forOffering(ServiceAttributeDefinition def) {
        boolean required = Boolean.TRUE.equals(def.getIsRequired())
                && (def.getInputContext() == null || def.getInputContext().appliesToProvider());
        return new FieldSpec(def.getAttributeCode(), def.getAttributeName(), kindOf(def), required,
                optionValues(def.getOptions()), rules(def.getValidationRules()));
    }

    public List<FieldSpec> forOffering(List<ServiceAttributeDefinition> defs) {
        return defs.stream().map(this::forOffering).toList();
    }

    public FieldSpec forQuotation(QuotationFieldDefinition def) {
        FieldSpec.Kind kind = switch (def.getFieldType()) {
            case NUMBER -> FieldSpec.Kind.DECIMAL;
            case DROPDOWN -> FieldSpec.Kind.SINGLE_SELECT;
            case MULTI_SELECT -> FieldSpec.Kind.MULTI_SELECT;
            case BOOLEAN -> FieldSpec.Kind.BOOLEAN;
            case DATE -> FieldSpec.Kind.DATE;
            case TEXT, TEXTAREA, FILE_UPLOAD -> FieldSpec.Kind.TEXT;
        };
        return new FieldSpec(def.getFieldCode(), def.getLabel(), kind, Boolean.TRUE.equals(def.getIsRequired()),
                kind == FieldSpec.Kind.TEXT ? List.of() : optionValues(def.getOptions()),
                rules(def.getValidationRules()));
    }

    public List<FieldSpec> forQuotation(List<QuotationFieldDefinition> defs) {
        return defs.stream().map(this::forQuotation).toList();
    }

    private static FieldSpec.Kind kindOf(ServiceAttributeDefinition def) {
        return switch (def.getDataType()) {
            case STRING -> FieldSpec.Kind.TEXT;
            case INTEGER -> FieldSpec.Kind.INTEGER;
            case NUMBER, DECIMAL -> FieldSpec.Kind.DECIMAL;
            case BOOLEAN -> FieldSpec.Kind.BOOLEAN;
            case DATE -> FieldSpec.Kind.DATE;
            case SINGLE_SELECT -> FieldSpec.Kind.SINGLE_SELECT;
            case MULTI_SELECT -> FieldSpec.Kind.MULTI_SELECT;
            case JSON -> FieldSpec.Kind.JSON;
        };
    }

    /**
     * Reads an options JSON array of strings or of objects with a "value" key.
     */
    List<String> optionValues(String optionsJson) {
        if (optionsJson == null || optionsJson.isBlank()) {
            return List.of();
        }
        try {
            List<Object> raw = objectMapper.readValue(optionsJson, new TypeReference<List<Object>>() {});
            List<String> values = new ArrayList<>();
            for (Object item : raw) {
                if (item instanceof Map<?, ?> map && map.get("value") != null) {
                    values.add(map.get("value").toString());
                } else if (item != null && !(item instanceof Map<?, ?>)) {
                    values.add(item.toString());
                }
            }
            return values;
        } catch (JsonProcessingException e) {
            log.warn("Ignoring unreadable options JSON: {}", optionsJson);
            return List.of();
        }
    }

    private Map<String, Object> rules(String rulesJson) {
        if (rulesJson == null || rulesJson.isBlank()) {
            return Map.of();
        }
        try {
            return objectMapper.readValue(rulesJson, new TypeReference<Map<String, Object>>() {});
        } catch (JsonProcessingException e) {
            log.warn("Ignoring unreadable validation_rules JSON: {}", rulesJson);
            return Map.of();
        }
    }
}
