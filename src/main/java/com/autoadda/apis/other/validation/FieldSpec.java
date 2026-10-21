package com.autoadda.apis.other.validation;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A single field the AttributeValidator checks a submitted value against. Built from either a
 * ServiceAttributeDefinition (offerings) or a QuotationFieldDefinition (quotation requests).
 *
 * @param allowedValues option values for SINGLE_SELECT / MULTI_SELECT; empty means unrestricted
 * @param rules         validation_rules JSON: minimum/maximum (or min/max), minLength/maxLength,
 *                      pattern, minItems/maxItems
 */
public record FieldSpec(String code,
                        String label,
                        Kind kind,
                        boolean required,
                        List<String> allowedValues,
                        Map<String, Object> rules) {

    public enum Kind { TEXT, INTEGER, DECIMAL, BOOLEAN, DATE, SINGLE_SELECT, MULTI_SELECT, JSON }

    public FieldSpec {
        allowedValues = allowedValues != null ? List.copyOf(allowedValues) : List.of();
        Map<String, Object> nonNull = new LinkedHashMap<>();
        if (rules != null) {
            rules.forEach((k, v) -> {
                if (v != null) nonNull.put(k, v);
            });
        }
        rules = Collections.unmodifiableMap(nonNull);
    }
}
