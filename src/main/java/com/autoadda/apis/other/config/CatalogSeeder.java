package com.autoadda.apis.other.config;

import com.autoadda.apis.other.entity.ServiceCategory;
import com.autoadda.apis.other.repository.CatalogServiceRepository;
import com.autoadda.apis.other.repository.ServiceAttributeDefinitionRepository;
import com.autoadda.apis.other.repository.ServiceCategoryRepository;
import com.autoadda.apis.other.request.AttributeOption;
import com.autoadda.apis.other.request.CatalogServiceRequest;
import com.autoadda.apis.other.request.MarketAvailabilityRequest;
import com.autoadda.apis.other.request.ServiceAttributeDefinitionRequest;
import com.autoadda.apis.other.response.CatalogServiceResponse;
import com.autoadda.apis.other.response.ServiceAttributeDefinitionResponse;
import com.autoadda.apis.other.service.MarketService;
import com.autoadda.apis.other.service.SchemaVersionService;
import com.autoadda.apis.other.service.ServiceAttributeDefinitionService;
import com.autoadda.apis.other.service.ServiceCatalogService;
import com.autoadda.apis.other.util.DataType;
import com.autoadda.apis.other.util.FulfillmentMode;
import com.autoadda.apis.other.util.InputContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Loads the Middle East starter catalog (seed/catalog-seed.json) into an empty or partly seeded
 * database: categories with their VENDOR attributes, services with a published v1 schema,
 * attributes, options and markets.
 * Goes through the normal services, so the same validation applies. Existing categories and
 * services and category attributes (matched by code) are left untouched, so restarts are safe.
 *
 * catalog.seed.enabled=false turns it off; catalog.seed.markets limits which countries are seeded.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "catalog.seed.enabled", havingValue = "true", matchIfMissing = true)
public class CatalogSeeder implements ApplicationRunner {

    private final ObjectMapper objectMapper;
    private final ServiceCategoryRepository categoryRepository;
    private final CatalogServiceRepository catalogServiceRepository;
    private final ServiceAttributeDefinitionRepository attributeRepository;
    private final ServiceCatalogService serviceCatalogService;
    private final ServiceAttributeDefinitionService attributeService;
    private final SchemaVersionService schemaVersionService;
    private final MarketService marketService;
    private final TransactionTemplate transactionTemplate;

    @Value("${catalog.seed.markets:AE}")
    private String seedMarkets;

    @Override
    public void run(ApplicationArguments args) throws IOException {
        Seed seed;
        try (InputStream in = new ClassPathResource("seed/catalog-seed.json").getInputStream()) {
            seed = objectMapper.readValue(in, Seed.class);
        }
        Set<String> countries = List.of(seedMarkets.split(",")).stream()
                .map(c -> c.trim().toUpperCase(Locale.ROOT))
                .filter(c -> !c.isEmpty())
                .collect(Collectors.toSet());

        int created = 0;
        for (SeedCategory category : seed.getCategories()) {
            ServiceCategory saved = transactionTemplate.execute(status -> upsertCategory(category));
            for (SeedAttribute attribute : category.getVendorAttributes()) {
                if (!attributeRepository.existsByServiceCategoryIdAndCatalogServiceIsNullAndAttributeCode(
                        saved.getId(), attribute.getCode())) {
                    transactionTemplate.executeWithoutResult(status -> attributeService.create(
                            attributeRequest(attribute).serviceCategoryId(saved.getId()).build()));
                }
            }
            for (SeedService service : category.getServices()) {
                if (catalogServiceRepository.existsByCode(service.getCode())) {
                    continue;
                }
                // one transaction per service: a bad entry rolls back alone
                transactionTemplate.executeWithoutResult(status -> seedService(saved, service, countries));
                created++;
            }
        }
        if (created > 0) {
            log.info("Catalog seed: created {} services (markets: {})", created, countries);
        }
    }

    private ServiceCategory upsertCategory(SeedCategory seed) {
        return categoryRepository.findByCode(seed.getCode()).orElseGet(() -> {
            ServiceCategory category = new ServiceCategory();
            category.setCode(seed.getCode());
            category.setName(seed.getName());
            category.setNameAr(seed.getNameAr());
            category.setDisplayOrder(seed.getDisplayOrder());
            category.setIsActive(true);
            return categoryRepository.save(category);
        });
    }

    private void seedService(ServiceCategory category, SeedService seed, Set<String> countries) {
        CatalogServiceResponse service = serviceCatalogService.create(CatalogServiceRequest.builder()
                .serviceCategoryId(category.getId())
                .code(seed.getCode())
                .name(seed.getName())
                .nameAr(seed.getNameAr())
                .fulfillmentMode(seed.getFulfillmentMode())
                .estimatedDurationMinutes(seed.getEstimatedDurationMinutes())
                .displayOrder(seed.getDisplayOrder())
                .build());

        Integer draftId = null;
        for (SeedAttribute attribute : seed.getAttributes()) {
            ServiceAttributeDefinitionResponse saved = attributeService.create(
                    attributeRequest(attribute).serviceId(service.getId()).build());
            draftId = saved.getSchemaVersionId();
        }
        if (draftId != null) {
            schemaVersionService.publish(service.getId(), draftId);
        }

        for (SeedMarket market : seed.getMarkets()) {
            if (countries.contains(market.getCountryCode())) {
                marketService.upsert(service.getId(), MarketAvailabilityRequest.builder()
                        .countryCode(market.getCountryCode())
                        .currencyCode(market.getCurrencyCode())
                        .isAvailable(true)
                        .regulatoryNote(market.getRegulatoryNote())
                        .regulatoryNoteAr(market.getRegulatoryNoteAr())
                        .build());
            }
        }
    }

    private ServiceAttributeDefinitionRequest.ServiceAttributeDefinitionRequestBuilder attributeRequest(SeedAttribute attribute) {
        return ServiceAttributeDefinitionRequest.builder()
                .attributeCode(attribute.getCode())
                .attributeName(attribute.getName())
                .attributeNameAr(attribute.getNameAr())
                .dataType(attribute.getDataType())
                .inputContext(attribute.getInputContext())
                .isRequired(attribute.isRequired())
                .isFilterable(attribute.getOptions() != null)
                .unit(attribute.getUnit())
                .description(attribute.getDescription())
                .validationRules(attribute.getValidationRules() != null
                        ? writeJson(attribute.getValidationRules()) : null)
                .options(attribute.getOptions())
                .displayOrder(attribute.getDisplayOrder());
    }

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (IOException e) {
            throw new IllegalStateException("Bad validation rules in catalog seed", e);
        }
    }

    @Data
    static class Seed {
        private String source;
        private List<SeedCategory> categories = List.of();
    }

    @Data
    static class SeedCategory {
        private String code;
        private String name;
        private String nameAr;
        private Integer displayOrder;
        /** Category-level details a vendor fills in for a service in this category. */
        private List<SeedAttribute> vendorAttributes = List.of();
        private List<SeedService> services = List.of();
    }

    @Data
    static class SeedService {
        private String code;
        private String name;
        private String nameAr;
        private FulfillmentMode fulfillmentMode;
        private Integer estimatedDurationMinutes;
        private Integer displayOrder;
        private List<SeedAttribute> attributes = List.of();
        private List<SeedMarket> markets = List.of();
    }

    @Data
    static class SeedAttribute {
        private String code;
        private String name;
        private String nameAr;
        private DataType dataType;
        private InputContext inputContext;
        private boolean required;
        private String unit;
        private String description;
        private Map<String, Object> validationRules;
        private Integer displayOrder;
        private List<AttributeOption> options;
    }

    @Data
    static class SeedMarket {
        private String countryCode;
        private String currencyCode;
        private String regulatoryNote;
        private String regulatoryNoteAr;
    }
}
