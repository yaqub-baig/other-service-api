package com.autoadda.apis.other.catalog;

import com.autoadda.apis.other.entity.ServiceCategory;
import com.autoadda.apis.other.entity.Vendor;
import com.autoadda.apis.other.entity.VendorService;
import com.autoadda.apis.other.exception.BusinessException;
import com.autoadda.apis.other.repository.ServiceCategoryRepository;
import com.autoadda.apis.other.repository.VendorRepository;
import com.autoadda.apis.other.repository.VendorServiceRepository;
import com.autoadda.apis.other.request.AttributeOption;
import com.autoadda.apis.other.request.CatalogServiceRequest;
import com.autoadda.apis.other.request.MarketAvailabilityRequest;
import com.autoadda.apis.other.request.ServiceAttributeDefinitionRequest;
import com.autoadda.apis.other.request.ServiceOfferingRequest;
import com.autoadda.apis.other.response.CatalogServiceResponse;
import com.autoadda.apis.other.response.SchemaVersionResponse;
import com.autoadda.apis.other.response.ServiceAttributeDefinitionResponse;
import com.autoadda.apis.other.response.ServiceOfferingResponse;
import com.autoadda.apis.other.service.MarketService;
import com.autoadda.apis.other.service.SchemaVersionService;
import com.autoadda.apis.other.service.ServiceAttributeDefinitionService;
import com.autoadda.apis.other.service.ServiceCatalogService;
import com.autoadda.apis.other.service.ServiceOfferingService;
import com.autoadda.apis.other.util.DataType;
import com.autoadda.apis.other.util.InputContext;
import com.autoadda.apis.other.util.SchemaVersionStatus;
import com.autoadda.apis.other.validation.AttributeValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Service → schema version → market → offering, end to end on H2.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CatalogFlowIntegrationTest {

    @Autowired private ServiceCategoryRepository categoryRepository;
    @Autowired private VendorRepository vendorRepository;
    @Autowired private VendorServiceRepository vendorServiceRepository;
    @Autowired private ServiceCatalogService catalogService;
    @Autowired private ServiceAttributeDefinitionService attributeService;
    @Autowired private SchemaVersionService schemaVersionService;
    @Autowired private MarketService marketService;
    @Autowired private ServiceOfferingService offeringService;

    private Integer serviceId;
    private Integer vendorServiceId;

    @BeforeEach
    void setUp() {
        ServiceCategory category = new ServiceCategory();
        category.setName("Routine Maintenance");
        category = categoryRepository.save(category);

        CatalogServiceResponse oilChange = catalogService.create(CatalogServiceRequest.builder()
                .serviceCategoryId(category.getId()).code("oil_change").name("Engine Oil Change").build());
        serviceId = oilChange.getId();

        marketService.upsert(serviceId, MarketAvailabilityRequest.builder().countryCode("ae").currencyCode("aed").build());

        Vendor vendor = new Vendor();
        vendor.setName("Quick Lube");
        vendor.setCountryCode("AE");
        vendor = vendorRepository.save(vendor);
        VendorService vendorService = new VendorService();
        vendorService.setVendor(vendor);
        vendorService.setServiceCategory(category);
        vendorService = vendorServiceRepository.save(vendorService);
        vendorServiceId = vendorService.getId();
    }

    private ServiceAttributeDefinitionResponse addOilType() {
        return attributeService.create(ServiceAttributeDefinitionRequest.builder()
                .serviceId(serviceId).attributeCode("oil_type").attributeName("Oil type")
                .dataType(DataType.SINGLE_SELECT).isRequired(true).inputContext(InputContext.PROVIDER)
                .options(List.of(AttributeOption.builder().value("SYNTHETIC").build(),
                        AttributeOption.builder().value("MINERAL").build()))
                .build());
    }

    private ServiceOfferingRequest.ServiceOfferingRequestBuilder offering(Map<String, Object> attributes) {
        return ServiceOfferingRequest.builder()
                .vendorServiceId(vendorServiceId).serviceId(serviceId)
                .offeringName("Synthetic oil change").price(new BigDecimal("199.00"))
                .filterableAttributes(attributes);
    }

    @Test
    void codeIsNormalisedAndUnique() {
        assertThat(catalogService.getById(serviceId).getCode()).isEqualTo("OIL_CHANGE");
    }

    @Test
    void draftAttributesAreNotLiveUntilPublished() {
        ServiceAttributeDefinitionResponse oilType = addOilType();
        assertThat(oilType.getSchemaVersionStatus()).isEqualTo(SchemaVersionStatus.DRAFT);
        assertThat(oilType.getSchemaVersionNo()).isEqualTo(1);

        // Not published yet: the attribute is unknown to offerings
        assertThatThrownBy(() -> offeringService.create(offering(Map.of("oil_type", "SYNTHETIC")).build()))
                .isInstanceOf(AttributeValidationException.class);

        schemaVersionService.publish(serviceId, oilType.getSchemaVersionId());
        ServiceOfferingResponse created = offeringService.create(offering(Map.of("oil_type", "SYNTHETIC")).build());
        assertThat(created.getSchemaVersionNo()).isEqualTo(1);
        assertThat(created.getCurrency()).isEqualTo("AED");
    }

    @Test
    void requiredAttributeCannotBeSkippedAndOptionsAreEnforced() {
        ServiceAttributeDefinitionResponse oilType = addOilType();
        schemaVersionService.publish(serviceId, oilType.getSchemaVersionId());

        assertThatThrownBy(() -> offeringService.create(offering(null).build()))
                .isInstanceOf(AttributeValidationException.class);
        assertThatThrownBy(() -> offeringService.create(offering(Map.of("oil_type", "DIESEL")).build()))
                .isInstanceOf(AttributeValidationException.class);
    }

    @Test
    void currencyMustMatchTheVendorsMarket() {
        assertThatThrownBy(() -> offeringService.create(offering(Map.of()).currency("INR").build()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("AED");
    }

    @Test
    void publishedVersionsAreImmutableAndNewVersionLeavesOldOfferingsAlone() {
        ServiceAttributeDefinitionResponse oilType = addOilType();
        SchemaVersionResponse v1 = schemaVersionService.publish(serviceId, oilType.getSchemaVersionId());
        ServiceOfferingResponse onV1 = offeringService.create(offering(Map.of("oil_type", "MINERAL")).build());

        // v1 cannot be edited in place
        assertThatThrownBy(() -> attributeService.update(oilType.getId(),
                ServiceAttributeDefinitionRequest.builder().attributeName("Oil").build()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("PUBLISHED");

        // v2 starts as a copy of v1 and adds a field
        SchemaVersionResponse v2Draft = schemaVersionService.openDraft(serviceId, "add viscosity");
        assertThat(v2Draft.getVersionNo()).isEqualTo(2);
        assertThat(v2Draft.getAttributeCount()).isEqualTo(1);
        attributeService.create(ServiceAttributeDefinitionRequest.builder()
                .serviceId(serviceId).attributeCode("oil_grade").attributeName("Viscosity")
                .dataType(DataType.STRING).build());
        schemaVersionService.publish(serviceId, v2Draft.getId());

        List<SchemaVersionResponse> versions = schemaVersionService.list(serviceId);
        assertThat(versions).extracting(SchemaVersionResponse::getStatus)
                .containsExactly(SchemaVersionStatus.PUBLISHED, SchemaVersionStatus.RETIRED);
        assertThat(offeringService.getById(onV1.getId()).getSchemaVersionId()).isEqualTo(v1.getId());

        ServiceOfferingResponse onV2 = offeringService.create(offering(Map.of("oil_type", "MINERAL", "oil_grade", "5W-30")).build());
        assertThat(onV2.getSchemaVersionNo()).isEqualTo(2);
    }
}
