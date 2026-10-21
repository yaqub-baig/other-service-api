package com.autoadda.apis.other.service;

import com.autoadda.apis.other.entity.CatalogService;
import com.autoadda.apis.other.entity.ServiceMarketAvailability;
import com.autoadda.apis.other.entity.Vendor;
import com.autoadda.apis.other.exception.BusinessException;
import com.autoadda.apis.other.repository.ServiceMarketAvailabilityRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MarketServiceTest {

    private final ServiceMarketAvailabilityRepository repository = mock(ServiceMarketAvailabilityRepository.class);
    private final MarketService marketService =
            new MarketService(repository, mock(ServiceCatalogService.class), new ObjectMapper());

    private final CatalogService oilChange = CatalogService.builder().id(7).code("OIL_CHANGE").build();

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(marketService, "defaultCountry", "AE");
        when(repository.findByCatalogServiceIdAndCountryCodeAndIsAvailableTrue(7, "AE"))
                .thenReturn(Optional.of(ServiceMarketAvailability.builder().countryCode("AE").currencyCode("AED").build()));
        when(repository.findByCatalogServiceIdAndCountryCodeAndIsAvailableTrue(7, "SA"))
                .thenReturn(Optional.empty());
    }

    private static Vendor vendorIn(String country) {
        Vendor vendor = new Vendor();
        vendor.setCountryCode(country);
        return vendor;
    }

    @Test
    void usesMarketCurrencyWhenNoneRequested() {
        assertThat(marketService.resolveOfferingCurrency(oilChange, vendorIn("AE"), null)).isEqualTo("AED");
    }

    @Test
    void acceptsMatchingCurrencyInAnyCase() {
        assertThat(marketService.resolveOfferingCurrency(oilChange, vendorIn("AE"), "aed")).isEqualTo("AED");
    }

    @Test
    void rejectsCurrencyThatDoesNotMatchTheMarket() {
        assertThatThrownBy(() -> marketService.resolveOfferingCurrency(oilChange, vendorIn("AE"), "INR"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("AED");
    }

    @Test
    void rejectsServiceNotAvailableInVendorCountry() {
        assertThatThrownBy(() -> marketService.resolveOfferingCurrency(oilChange, vendorIn("SA"), null))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("not available in SA");
    }

    @Test
    void vendorWithoutCountryFallsBackToDefault() {
        assertThat(marketService.resolveOfferingCurrency(oilChange, vendorIn(null), null)).isEqualTo("AED");
    }

    @Test
    void offeringWithoutServiceKeepsRequestedCurrency() {
        assertThat(marketService.resolveOfferingCurrency(null, vendorIn("AE"), "sar")).isEqualTo("SAR");
        assertThat(marketService.resolveOfferingCurrency(null, vendorIn("AE"), null)).isEqualTo("AED");
        assertThatThrownBy(() -> marketService.resolveOfferingCurrency(null, vendorIn("AE"), "XYZ1"))
                .isInstanceOf(BusinessException.class);
    }
}
