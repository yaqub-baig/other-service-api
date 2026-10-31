package com.autoadda.apis.other.service;

import com.autoadda.apis.other.entity.ServiceCategory;
import com.autoadda.apis.other.entity.Vendor;
import com.autoadda.apis.other.repository.ServiceCategoryRepository;
import com.autoadda.apis.other.repository.VendorRepository;
import com.autoadda.apis.other.request.VendorServiceRequest;
import com.autoadda.apis.other.response.VendorServiceResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The isActive flag on /vendor-services bodies: bound from "isActive" (or "active"), and an update
 * that leaves it out keeps the current value.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class VendorServiceCrudServiceTest {

    @Autowired private ObjectMapper objectMapper;
    @Autowired private VendorRepository vendorRepository;
    @Autowired private ServiceCategoryRepository categoryRepository;
    @Autowired private VendorServiceCrudService crudService;

    private Integer vendorId;
    private Integer categoryId;

    @BeforeEach
    void setUp() {
        Vendor vendor = new Vendor();
        vendor.setName("Quick Lube");
        vendorId = vendorRepository.save(vendor).getId();
        ServiceCategory category = new ServiceCategory();
        category.setName("Routine Maintenance");
        categoryId = categoryRepository.save(category).getId();
    }

    private VendorServiceRequest read(String json) throws Exception {
        return objectMapper.readValue(json.formatted(vendorId, categoryId), VendorServiceRequest.class);
    }

    @Test
    void isActiveAndActiveAreBothRead() throws Exception {
        assertThat(read("{\"vendorId\": %d, \"serviceCategoryId\": %d, \"isActive\": false}").getIsActive()).isFalse();
        assertThat(read("{\"vendorId\": %d, \"serviceCategoryId\": %d, \"active\": false}").getIsActive()).isFalse();
    }

    @Test
    void createDefaultsToActiveAndPricingUpdateKeepsIt() throws Exception {
        VendorServiceResponse created = crudService.createVendorService(
                read("{\"vendorId\": %d, \"serviceCategoryId\": %d}"));
        assertThat(created.getIsActive()).isTrue();

        VendorServiceResponse updated = crudService.updateVendorService(created.getId(),
                read("{\"vendorId\": %d, \"serviceCategoryId\": %d, \"price\": 120.00}"));
        assertThat(updated.getIsActive()).isTrue();
        assertThat(updated.getPrice()).isEqualByComparingTo(new BigDecimal("120.00"));

        updated = crudService.updateVendorService(created.getId(),
                read("{\"vendorId\": %d, \"serviceCategoryId\": %d, \"isActive\": false}"));
        assertThat(updated.getIsActive()).isFalse();
    }
}
