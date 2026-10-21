package com.autoadda.apis.other.validation;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Validates submitted attribute values against their field specs: required, data type, allowed
 * options and validation_rules. Unknown codes are rejected. Used for offering attributes and
 * quotation request fields alike.
 */
@Component
public class AttributeValidator {

    /**
     * Returns every problem found; an empty list means the values are valid.
     */
    public List<FieldError> validate(List<FieldSpec> specs, Map<String, Object> values) {
        Map<String, Object> submitted = values != null ? values : Map.of();
        Map<String, FieldSpec> byCode = new LinkedHashMap<>();
        specs.forEach(spec -> byCode.put(spec.code(), spec));

        List<FieldError> errors = new ArrayList<>();
        for (String code : submitted.keySet()) {
            if (!byCode.containsKey(code)) {
                errors.add(new FieldError(code, "Unknown attribute '" + code + "'"));
            }
        }
        for (FieldSpec spec : byCode.values()) {
            Object value = submitted.get(spec.code());
            if (isEmpty(value)) {
                if (spec.required()) {
                    errors.add(new FieldError(spec.code(), label(spec) + " is required"));
                }
                continue;
            }
            checkValue(spec, value, errors);
        }
        return errors;
    }

    public void validateOrThrow(List<FieldSpec> specs, Map<String, Object> values) {
        List<FieldError> errors = validate(specs, values);
        if (!errors.isEmpty()) {
            throw new AttributeValidationException(errors);
        }
    }

    private void checkValue(FieldSpec spec, Object value, List<FieldError> errors) {
        switch (spec.kind()) {
            case TEXT -> checkText(spec, value, errors);
            case INTEGER -> checkNumber(spec, value, true, errors);
            case DECIMAL -> checkNumber(spec, value, false, errors);
            case BOOLEAN -> {
                if (!(value instanceof Boolean) && !"true".equalsIgnoreCase(value.toString())
                        && !"false".equalsIgnoreCase(value.toString())) {
                    errors.add(new FieldError(spec.code(), label(spec) + " must be true or false"));
                }
            }
            case DATE -> {
                try {
                    LocalDate.parse(value.toString());
                } catch (DateTimeParseException e) {
                    errors.add(new FieldError(spec.code(), label(spec) + " must be a date (yyyy-MM-dd)"));
                }
            }
            case SINGLE_SELECT -> {
                if (value instanceof Collection<?> || value instanceof Map<?, ?>) {
                    errors.add(new FieldError(spec.code(), label(spec) + " takes a single value"));
                } else {
                    checkOption(spec, value.toString(), errors);
                }
            }
            case MULTI_SELECT -> checkMulti(spec, value, errors);
            case JSON -> {
                // any JSON value is accepted
            }
        }
    }

    private void checkText(FieldSpec spec, Object value, List<FieldError> errors) {
        if (value instanceof Collection<?> || value instanceof Map<?, ?>) {
            errors.add(new FieldError(spec.code(), label(spec) + " must be text"));
            return;
        }
        String text = value.toString();
        Integer minLength = intRule(spec, "minLength");
        Integer maxLength = intRule(spec, "maxLength");
        if (minLength != null && text.length() < minLength) {
            errors.add(new FieldError(spec.code(), label(spec) + " must be at least " + minLength + " characters"));
        }
        if (maxLength != null && text.length() > maxLength) {
            errors.add(new FieldError(spec.code(), label(spec) + " must be at most " + maxLength + " characters"));
        }
        Object pattern = spec.rules().get("pattern");
        if (pattern != null) {
            try {
                if (!Pattern.compile(pattern.toString()).matcher(text).matches()) {
                    errors.add(new FieldError(spec.code(), label(spec) + " has an invalid format"));
                }
            } catch (PatternSyntaxException e) {
                errors.add(new FieldError(spec.code(), label(spec) + " has a misconfigured pattern rule"));
            }
        }
        if (!spec.allowedValues().isEmpty()) {
            checkOption(spec, text, errors);
        }
    }

    private void checkNumber(FieldSpec spec, Object value, boolean wholeOnly, List<FieldError> errors) {
        BigDecimal number;
        try {
            number = value instanceof Number n ? new BigDecimal(n.toString()) : new BigDecimal(value.toString().trim());
        } catch (NumberFormatException e) {
            errors.add(new FieldError(spec.code(), label(spec) + " must be a number"));
            return;
        }
        if (wholeOnly && number.stripTrailingZeros().scale() > 0) {
            errors.add(new FieldError(spec.code(), label(spec) + " must be a whole number"));
            return;
        }
        BigDecimal min = decimalRule(spec, "minimum", "min");
        BigDecimal max = decimalRule(spec, "maximum", "max");
        if (min != null && number.compareTo(min) < 0) {
            errors.add(new FieldError(spec.code(), label(spec) + " must be at least " + min.toPlainString()));
        }
        if (max != null && number.compareTo(max) > 0) {
            errors.add(new FieldError(spec.code(), label(spec) + " must be at most " + max.toPlainString()));
        }
    }

    private void checkMulti(FieldSpec spec, Object value, List<FieldError> errors) {
        if (!(value instanceof Collection<?> items)) {
            errors.add(new FieldError(spec.code(), label(spec) + " must be a list"));
            return;
        }
        Integer minItems = intRule(spec, "minItems");
        Integer maxItems = intRule(spec, "maxItems");
        if (minItems != null && items.size() < minItems) {
            errors.add(new FieldError(spec.code(), label(spec) + " needs at least " + minItems + " choice(s)"));
        }
        if (maxItems != null && items.size() > maxItems) {
            errors.add(new FieldError(spec.code(), label(spec) + " allows at most " + maxItems + " choice(s)"));
        }
        for (Object item : items) {
            if (item == null) {
                errors.add(new FieldError(spec.code(), label(spec) + " contains an empty choice"));
            } else {
                checkOption(spec, item.toString(), errors);
            }
        }
    }

    private void checkOption(FieldSpec spec, String value, List<FieldError> errors) {
        if (!spec.allowedValues().isEmpty() && !spec.allowedValues().contains(value)) {
            errors.add(new FieldError(spec.code(), "'" + value + "' is not an allowed value for " + label(spec)));
        }
    }

    private static boolean isEmpty(Object value) {
        return value == null
                || (value instanceof String s && s.isBlank())
                || (value instanceof Collection<?> c && c.isEmpty());
    }

    private static String label(FieldSpec spec) {
        return spec.label() != null ? spec.label() : spec.code();
    }

    private static Integer intRule(FieldSpec spec, String key) {
        BigDecimal value = decimalRule(spec, key);
        return value != null ? value.intValue() : null;
    }

    private static BigDecimal decimalRule(FieldSpec spec, String... keys) {
        for (String key : keys) {
            Object raw = spec.rules().get(key);
            if (raw != null) {
                try {
                    return new BigDecimal(raw.toString());
                } catch (NumberFormatException ignored) {
                    // a non-numeric rule is ignored rather than failing every submission
                }
            }
        }
        return null;
    }
}
