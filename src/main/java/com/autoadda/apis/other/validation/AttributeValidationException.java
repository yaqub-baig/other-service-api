package com.autoadda.apis.other.validation;

import com.autoadda.apis.other.exception.BusinessException;

import java.util.List;

/**
 * Thrown when submitted attribute or form values do not match their definitions.
 * Carries every field error, not just the first.
 */
public class AttributeValidationException extends BusinessException {

    private final transient List<FieldError> errors;

    public AttributeValidationException(List<FieldError> errors) {
        super("Invalid attribute values: " + errors.size() + " field(s) failed validation");
        this.errors = List.copyOf(errors);
    }

    public List<FieldError> getErrors() {
        return errors;
    }
}
