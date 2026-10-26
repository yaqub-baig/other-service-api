package com.autoadda.apis.other.validation;

/**
 * One field-level validation failure, returned to the client.
 */
public record FieldError(String field, String message) {
}
