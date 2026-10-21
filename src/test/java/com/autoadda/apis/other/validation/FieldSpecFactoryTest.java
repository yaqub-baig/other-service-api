package com.autoadda.apis.other.validation;

import com.autoadda.apis.other.entity.QuotationFieldDefinition;
import com.autoadda.apis.other.entity.ServiceAttributeDefinition;
import com.autoadda.apis.other.util.DataType;
import com.autoadda.apis.other.util.FieldType;
import com.autoadda.apis.other.util.InputContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FieldSpecFactoryTest {

    private final FieldSpecFactory factory = new FieldSpecFactory(new ObjectMapper());

    private static ServiceAttributeDefinition attribute(DataType type, InputContext context, boolean required) {
        return ServiceAttributeDefinition.builder()
                .attributeCode("oil_type").attributeName("Oil type")
                .dataType(type).inputContext(context).isRequired(required)
                .build();
    }

    @Test
    void readsOptionObjectsAndPlainStrings() {
        ServiceAttributeDefinition def = attribute(DataType.SINGLE_SELECT, InputContext.BOTH, false);
        def.setOptions("[{\"value\":\"SYNTHETIC\",\"label\":\"Synthetic\"},\"MINERAL\"]");
        assertThat(factory.forOffering(def).allowedValues()).containsExactly("SYNTHETIC", "MINERAL");
    }

    @Test
    void customerOnlyAttributesAreNotRequiredOnOfferings() {
        assertThat(factory.forOffering(attribute(DataType.STRING, InputContext.CUSTOMER, true)).required()).isFalse();
        assertThat(factory.forOffering(attribute(DataType.STRING, InputContext.PROVIDER, true)).required()).isTrue();
        assertThat(factory.forOffering(attribute(DataType.STRING, InputContext.BOTH, true)).required()).isTrue();
    }

    @Test
    void readsValidationRules() {
        ServiceAttributeDefinition def = attribute(DataType.DECIMAL, InputContext.PROVIDER, false);
        def.setValidationRules("{\"minimum\":1,\"maximum\":15}");
        assertThat(factory.forOffering(def).rules()).containsEntry("minimum", 1).containsEntry("maximum", 15);
    }

    @Test
    void mapsQuotationFieldTypes() {
        QuotationFieldDefinition field = QuotationFieldDefinition.builder()
                .fieldCode("panel").label("Panel").fieldType(FieldType.DROPDOWN).isRequired(true)
                .options("[\"Front Bumper\",\"Hood\"]")
                .build();
        FieldSpec spec = factory.forQuotation(field);
        assertThat(spec.kind()).isEqualTo(FieldSpec.Kind.SINGLE_SELECT);
        assertThat(spec.allowedValues()).containsExactly("Front Bumper", "Hood");
        assertThat(spec.required()).isTrue();
    }
}
