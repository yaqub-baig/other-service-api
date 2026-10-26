package com.autoadda.apis.other.catalog;

import com.autoadda.apis.other.entity.ServiceCategory;
import com.autoadda.apis.other.entity.Vendor;
import com.autoadda.apis.other.exception.BusinessException;
import com.autoadda.apis.other.repository.ServiceCategoryRepository;
import com.autoadda.apis.other.repository.VendorRepository;
import com.autoadda.apis.other.request.CatalogServiceRequest;
import com.autoadda.apis.other.request.ServiceAttributeDefinitionRequest;
import com.autoadda.apis.other.request.vendor.VendorServiceOnboardingRequest;
import com.autoadda.apis.other.response.VendorServiceResponse;
import com.autoadda.apis.other.service.ServiceAttributeDefinitionService;
import com.autoadda.apis.other.service.ServiceCatalogService;
import com.autoadda.apis.other.service.VendorManagementService;
import com.autoadda.apis.other.util.DataType;
import com.autoadda.apis.other.util.InputContext;
import com.autoadda.apis.other.util.VendorServiceType;
import com.autoadda.apis.other.validation.AttributeValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * A vendor service's details (bay count, mobile service, ...) are the category's VENDOR attributes.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class VendorServiceAttributesTest {

    @Autowired private ServiceCategoryRepository categoryRepository;
    @Autowired private VendorRepository vendorRepository;
    @Autowired private ServiceAttributeDefinitionService attributeService;
    @Autowired private ServiceCatalogService catalogService;
    @Autowired private VendorManagementService vendorManagementService;

    private Integer categoryId;
    private Integer vendorId;

    @BeforeEach
    void setUp() {
        ServiceCategory category = new ServiceCategory();
        category.setCode("CLEANING_DETAILING");
        category.setName("Washing & Detailing");
        categoryId = categoryRepository.save(category).getId();

        attributeService.create(ServiceAttributeDefinitionRequest.builder()
                .serviceCategoryId(categoryId).attributeCode("bay_count").attributeName("Wash bays")
                .dataType(DataType.INTEGER).inputContext(InputContext.VENDOR).isRequired(true)
                .validationRules("{\"minimum\": 1}").build());
        attributeService.create(ServiceAttributeDefinitionRequest.builder()
                .serviceCategoryId(categoryId).attributeCode("mobile_service").attributeName("Mobile service")
                .dataType(DataType.BOOLEAN).inputContext(InputContext.VENDOR).build());

        Vendor vendor = new Vendor();
        vendor.setName("Shine Car Wash");
        vendor.setCountryCode("AE");
        vendorId = vendorRepository.save(vendor).getId();
    }

    private VendorServiceOnboardingRequest carWash(Map<String, Object> attributes) {
        VendorServiceOnboardingRequest request = new VendorServiceOnboardingRequest();
        request.setServiceType(VendorServiceType.CAR_WASH);
        request.setOpeningTime(LocalTime.of(8, 0));
        request.setClosingTime(LocalTime.of(20, 0));
        request.setWorkingDays(List.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY));
        request.setAttributes(attributes);
        return request;
    }

    @Test
    void detailsAreValidatedAndStored() {
        VendorServiceResponse added = vendorManagementService.addService(
                vendorId, carWash(Map.of("bay_count", 4, "mobile_service", true)), "1");

        assertThat(added.getServiceCategoryId()).isEqualTo(categoryId);
        assertThat(added.getAttributes()).containsEntry("bay_count", 4).containsEntry("mobile_service", true);
    }

    @Test
    void requiredUnknownAndInvalidDetailsAreRefused() {
        assertThatThrownBy(() -> vendorManagementService.addService(vendorId, carWash(null), "1"))
                .isInstanceOf(AttributeValidationException.class);
        assertThatThrownBy(() -> vendorManagementService.addService(
                vendorId, carWash(Map.of("bay_count", 2, "colour", "red")), "1"))
                .isInstanceOf(AttributeValidationException.class);
        assertThatThrownBy(() -> vendorManagementService.addService(vendorId, carWash(Map.of("bay_count", 0)), "1"))
                .isInstanceOf(AttributeValidationException.class);
    }

    @Test
    void updateMergesDetailsAndNullClearsOne() {
        VendorServiceResponse added = vendorManagementService.addService(
                vendorId, carWash(Map.of("bay_count", 4, "mobile_service", true)), "1");

        Map<String, Object> changes = new HashMap<>();
        changes.put("bay_count", 6);
        changes.put("mobile_service", null);
        VendorServiceOnboardingRequest update = new VendorServiceOnboardingRequest();
        update.setAttributes(changes);

        VendorServiceResponse updated = vendorManagementService.updateService(added.getId(), update);
        assertThat(updated.getAttributes()).containsOnly(Map.entry("bay_count", 6));
    }

    @Test
    void vendorAttributesStayOffOfferingFormsAndServices() {
        assertThat(attributeService.resolveAttributes(categoryId, null)).isEmpty();
        assertThat(vendorManagementService.getVendorAttributes(VendorServiceType.CAR_WASH))
                .extracting(a -> a.getAttributeCode())
                .containsExactly("bay_count", "mobile_service");

        Integer serviceId = catalogService.create(CatalogServiceRequest.builder()
                .serviceCategoryId(categoryId).code("CAR_WASH").name("Car Wash").build()).getId();
        assertThatThrownBy(() -> attributeService.create(ServiceAttributeDefinitionRequest.builder()
                .serviceId(serviceId).attributeCode("bay_size").attributeName("Bay size")
                .dataType(DataType.INTEGER).inputContext(InputContext.VENDOR).build()))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void serviceTypeIsRequired() {
        VendorServiceOnboardingRequest request = carWash(Map.of("bay_count", 1));
        request.setServiceType(null);
        assertThatThrownBy(() -> vendorManagementService.addService(vendorId, request, "1"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("serviceType");
    }
}
