package com.autoadda.apis.other.catalog;

import com.autoadda.apis.other.config.CatalogSeeder;
import com.autoadda.apis.other.entity.CatalogService;
import com.autoadda.apis.other.repository.CatalogServiceRepository;
import com.autoadda.apis.other.repository.ServiceCategoryRepository;
import com.autoadda.apis.other.repository.ServiceMarketAvailabilityRepository;
import com.autoadda.apis.other.service.SchemaVersionService;
import com.autoadda.apis.other.service.ServiceAttributeDefinitionService;
import com.autoadda.apis.other.util.SchemaVersionStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Boots with the seeder on (AE and SA) and checks the starter catalog lands complete and published.
 */
@SpringBootTest(properties = {"catalog.seed.enabled=true", "catalog.seed.markets=AE,SA",
        // own in-memory database so the seeded rows do not leak into other test contexts
        "spring.datasource.url=jdbc:h2:mem:seeded;MODE=MySQL;DATABASE_TO_LOWER=TRUE;CASE_INSENSITIVE_IDENTIFIERS=TRUE;NON_KEYWORDS=VALUE,KEY,YEAR,MONTH,DAY"})
@ActiveProfiles("test")
class CatalogSeederTest {

    @Autowired private ServiceCategoryRepository categoryRepository;
    @Autowired private CatalogServiceRepository catalogServiceRepository;
    @Autowired private ServiceMarketAvailabilityRepository marketRepository;
    @Autowired private SchemaVersionService schemaVersionService;
    @Autowired private ServiceAttributeDefinitionService attributeService;
    @Autowired private CatalogSeeder seeder;

    @Test
    void seedsTheStarterCatalog() throws Exception {
        List<CatalogService> services = catalogServiceRepository.findAll();
        assertThat(categoryRepository.findByCode("TIRES_WHEELS")).isPresent();
        assertThat(services).hasSize(14);

        int attributes = 0;
        for (CatalogService service : services) {
            assertThat(schemaVersionService.currentPublished(service.getId()))
                    .as(service.getCode() + " has a published v1")
                    .hasValueSatisfying(v -> {
                        assertThat(v.getVersionNo()).isEqualTo(1);
                        assertThat(v.getStatus()).isEqualTo(SchemaVersionStatus.PUBLISHED);
                    });
            attributes += attributeService.getByService(service.getId()).size();
            assertThat(marketRepository.findByCatalogServiceIdOrderByCountryCodeAsc(service.getId()))
                    .extracting(m -> m.getCountryCode())
                    .containsExactly("AE", "SA");
        }
        assertThat(attributes).isEqualTo(39);

        CatalogService oilChange = catalogServiceRepository.findByCode("OIL_CHANGE").orElseThrow();
        assertThat(oilChange.getNameAr()).isEqualTo("تغيير زيت المحرك");

        Integer washing = categoryRepository.findByCode("CLEANING_DETAILING").orElseThrow().getId();
        assertThat(attributeService.resolveVendorAttributes(washing)).hasSize(8);

        // Running again changes nothing
        seeder.run(new DefaultApplicationArguments());
        assertThat(catalogServiceRepository.count()).isEqualTo(14);
        assertThat(attributeService.resolveVendorAttributes(washing)).hasSize(8);
    }
}
