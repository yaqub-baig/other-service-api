package com.autoadda.apis.other.validation;

import com.autoadda.apis.other.validation.FieldSpec.Kind;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AttributeValidatorTest {

    private final AttributeValidator validator = new AttributeValidator();

    private static FieldSpec spec(String code, Kind kind, boolean required) {
        return new FieldSpec(code, code, kind, required, List.of(), Map.of());
    }

    private static FieldSpec spec(String code, Kind kind, List<String> options, Map<String, Object> rules) {
        return new FieldSpec(code, code, kind, false, options, rules);
    }

    private List<String> fieldsWithErrors(List<FieldSpec> specs, Map<String, Object> values) {
        return validator.validate(specs, values).stream().map(FieldError::field).toList();
    }

    @Test
    void requiredFieldIsCheckedEvenWhenNoValuesAreSent() {
        List<FieldSpec> specs = List.of(spec("brand", Kind.TEXT, true));
        assertThat(fieldsWithErrors(specs, null)).containsExactly("brand");
        assertThat(fieldsWithErrors(specs, Map.of())).containsExactly("brand");
        assertThat(fieldsWithErrors(specs, Map.of("brand", "  "))).containsExactly("brand");
    }

    @Test
    void optionalFieldMayBeOmitted() {
        assertThat(validator.validate(List.of(spec("notes", Kind.TEXT, false)), Map.of())).isEmpty();
    }

    @Test
    void unknownCodesAreRejected() {
        assertThat(fieldsWithErrors(List.of(spec("brand", Kind.TEXT, false)), Map.of("colour", "red")))
                .containsExactly("colour");
    }

    @Test
    void textLengthAndPatternRules() {
        FieldSpec plate = spec("plate", Kind.TEXT, List.of(), Map.of("minLength", 2, "maxLength", 5, "pattern", "[A-Z0-9]+"));
        assertThat(validator.validate(List.of(plate), Map.of("plate", "AB12"))).isEmpty();
        assertThat(fieldsWithErrors(List.of(plate), Map.of("plate", "A"))).containsExactly("plate");
        assertThat(fieldsWithErrors(List.of(plate), Map.of("plate", "ABCDEFG"))).containsExactly("plate");
        assertThat(fieldsWithErrors(List.of(plate), Map.of("plate", "ab-1"))).containsExactly("plate");
    }

    @Test
    void textRejectsLists() {
        assertThat(fieldsWithErrors(List.of(spec("brand", Kind.TEXT, false)), Map.of("brand", List.of("a"))))
                .containsExactly("brand");
    }

    @Test
    void integerAcceptsWholeNumbersOnly() {
        FieldSpec qty = spec("qty", Kind.INTEGER, List.of(), Map.of("minimum", 1, "maximum", 10));
        assertThat(validator.validate(List.of(qty), Map.of("qty", 4))).isEmpty();
        assertThat(validator.validate(List.of(qty), Map.of("qty", "4"))).isEmpty();
        assertThat(validator.validate(List.of(qty), Map.of("qty", 4.0))).isEmpty();
        assertThat(fieldsWithErrors(List.of(qty), Map.of("qty", 4.5))).containsExactly("qty");
        assertThat(fieldsWithErrors(List.of(qty), Map.of("qty", "four"))).containsExactly("qty");
        assertThat(fieldsWithErrors(List.of(qty), Map.of("qty", 0))).containsExactly("qty");
        assertThat(fieldsWithErrors(List.of(qty), Map.of("qty", 11))).containsExactly("qty");
    }

    @Test
    void decimalHonoursMinMaxAliases() {
        FieldSpec litres = spec("litres", Kind.DECIMAL, List.of(), Map.of("min", 1, "max", 15));
        assertThat(validator.validate(List.of(litres), Map.of("litres", 4.5))).isEmpty();
        assertThat(fieldsWithErrors(List.of(litres), Map.of("litres", 0.5))).containsExactly("litres");
        assertThat(fieldsWithErrors(List.of(litres), Map.of("litres", "15.01"))).containsExactly("litres");
    }

    @Test
    void booleanAcceptsBooleansAndTrueFalseStrings() {
        FieldSpec flag = spec("replace_filter", Kind.BOOLEAN, false);
        assertThat(validator.validate(List.of(flag), Map.of("replace_filter", true))).isEmpty();
        assertThat(validator.validate(List.of(flag), Map.of("replace_filter", "FALSE"))).isEmpty();
        assertThat(fieldsWithErrors(List.of(flag), Map.of("replace_filter", "yes"))).containsExactly("replace_filter");
    }

    @Test
    void dateMustBeIso() {
        FieldSpec date = spec("expiry", Kind.DATE, false);
        assertThat(validator.validate(List.of(date), Map.of("expiry", "2026-10-21"))).isEmpty();
        assertThat(fieldsWithErrors(List.of(date), Map.of("expiry", "21/10/2026"))).containsExactly("expiry");
    }

    @Test
    void singleSelectMustBeAnAllowedOption() {
        FieldSpec oil = spec("oil_type", Kind.SINGLE_SELECT, List.of("SYNTHETIC", "MINERAL"), Map.of());
        assertThat(validator.validate(List.of(oil), Map.of("oil_type", "SYNTHETIC"))).isEmpty();
        assertThat(fieldsWithErrors(List.of(oil), Map.of("oil_type", "DIESEL"))).containsExactly("oil_type");
        assertThat(fieldsWithErrors(List.of(oil), Map.of("oil_type", List.of("SYNTHETIC")))).containsExactly("oil_type");
    }

    @Test
    void multiSelectChecksEveryItemAndItemCount() {
        FieldSpec extras = spec("extras", Kind.MULTI_SELECT, List.of("WAX", "VACUUM", "TYRE_SHINE"), Map.of("maxItems", 2));
        assertThat(validator.validate(List.of(extras), Map.of("extras", List.of("WAX", "VACUUM")))).isEmpty();
        assertThat(fieldsWithErrors(List.of(extras), Map.of("extras", List.of("WAX", "POLISH")))).containsExactly("extras");
        assertThat(fieldsWithErrors(List.of(extras), Map.of("extras", List.of("WAX", "VACUUM", "TYRE_SHINE")))).containsExactly("extras");
        assertThat(fieldsWithErrors(List.of(extras), Map.of("extras", "WAX"))).containsExactly("extras");
    }

    @Test
    void emptyMultiSelectCountsAsMissingForRequired() {
        FieldSpec extras = new FieldSpec("extras", "Extras", Kind.MULTI_SELECT, true, List.of("WAX"), Map.of());
        assertThat(fieldsWithErrors(List.of(extras), Map.of("extras", List.of()))).containsExactly("extras");
    }

    @Test
    void jsonAcceptsAnyValue() {
        assertThat(validator.validate(List.of(spec("meta", Kind.JSON, false)), Map.of("meta", Map.of("a", 1)))).isEmpty();
    }

    @Test
    void reportsAllErrorsAtOnce() {
        List<FieldSpec> specs = List.of(spec("a", Kind.TEXT, true), spec("b", Kind.INTEGER, false));
        assertThat(fieldsWithErrors(specs, Map.of("b", "x", "c", 1))).containsExactlyInAnyOrder("a", "b", "c");
    }

    @Test
    void validateOrThrowCarriesFieldErrors() {
        assertThatThrownBy(() -> validator.validateOrThrow(List.of(spec("a", Kind.TEXT, true)), Map.of()))
                .isInstanceOf(AttributeValidationException.class)
                .satisfies(e -> assertThat(((AttributeValidationException) e).getErrors())
                        .extracting(FieldError::field).containsExactly("a"));
    }
}
