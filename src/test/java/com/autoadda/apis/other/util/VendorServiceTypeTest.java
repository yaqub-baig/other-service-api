package com.autoadda.apis.other.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class VendorServiceTypeTest {

    @Test
    void everyTypeFilesUnderASeededCategory() throws Exception {
        Set<String> seeded = new HashSet<>();
        try (InputStream in = new ClassPathResource("seed/catalog-seed.json").getInputStream()) {
            for (JsonNode category : new ObjectMapper().readTree(in).get("categories")) {
                seeded.add(category.get("code").asText());
            }
        }
        for (VendorServiceType type : VendorServiceType.values()) {
            assertThat(seeded).as(type + " category").contains(type.getCategoryCode());
        }
    }

    @Test
    void carWashAndDetailingShareWashingAndDetailing() {
        assertThat(VendorServiceType.CAR_WASH.getCategoryCode()).isEqualTo("CLEANING_DETAILING");
        assertThat(VendorServiceType.CAR_DETAILING.getCategoryCode()).isEqualTo("CLEANING_DETAILING");
    }
}
