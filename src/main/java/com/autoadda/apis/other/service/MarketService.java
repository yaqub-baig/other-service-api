package com.autoadda.apis.other.service;

import com.autoadda.apis.other.entity.CatalogService;
import com.autoadda.apis.other.entity.ServiceMarketAvailability;
import com.autoadda.apis.other.entity.Vendor;
import com.autoadda.apis.other.exception.BusinessException;
import com.autoadda.apis.other.repository.ServiceMarketAvailabilityRepository;
import com.autoadda.apis.other.request.MarketAvailabilityRequest;
import com.autoadda.apis.other.response.MarketAvailabilityResponse;
import com.autoadda.apis.other.util.IsoCodes;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Country availability of catalog services and the currency rule for offerings.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MarketService {

    private final ServiceMarketAvailabilityRepository marketRepository;
    private final ServiceCatalogService serviceCatalogService;
    private final ObjectMapper objectMapper;

    @Value("${catalog.default-country:AE}")
    private String defaultCountry;

    public String getDefaultCountry() {
        return IsoCodes.country(defaultCountry);
    }

    @Transactional(readOnly = true)
    public List<MarketAvailabilityResponse> listForService(Integer serviceId) {
        serviceCatalogService.getEntity(serviceId);
        return marketRepository.findByCatalogServiceIdOrderByCountryCodeAsc(serviceId).stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * Creates or updates the service's availability in one country.
     */
    @Transactional
    public MarketAvailabilityResponse upsert(Integer serviceId, MarketAvailabilityRequest request) {
        CatalogService service = serviceCatalogService.getEntity(serviceId);
        String country = IsoCodes.country(request.getCountryCode());
        ServiceMarketAvailability market = marketRepository.findByCatalogServiceIdAndCountryCode(serviceId, country)
                .orElseGet(() -> ServiceMarketAvailability.builder()
                        .catalogService(service)
                        .countryCode(country)
                        .build());
        if (request.getCurrencyCode() != null) {
            market.setCurrencyCode(IsoCodes.currency(request.getCurrencyCode()));
        }
        if (market.getCurrencyCode() == null) {
            throw new BusinessException("currencyCode is required for a new market");
        }
        if (request.getIsAvailable() != null) {
            market.setIsAvailable(request.getIsAvailable());
        }
        if (request.getRegulatoryConfig() != null) {
            market.setRegulatoryConfig(toJson(request.getRegulatoryConfig()));
        }
        if (request.getRegulatoryNote() != null) market.setRegulatoryNote(request.getRegulatoryNote());
        if (request.getRegulatoryNoteAr() != null) market.setRegulatoryNoteAr(request.getRegulatoryNoteAr());
        ServiceMarketAvailability saved = marketRepository.save(market);
        log.info("Service {} market {} -> {} (available={})", service.getCode(), country,
                saved.getCurrencyCode(), saved.getIsAvailable());
        return toResponse(saved);
    }

    /**
     * The currency an offering must use. For a catalog service it is the currency of the vendor's
     * market, and the service must be available there; a different requested currency is rejected.
     * Without a service, the requested currency (or AED) is used.
     */
    @Transactional(readOnly = true)
    public String resolveOfferingCurrency(CatalogService service, Vendor vendor, String requestedCurrency) {
        if (service == null) {
            return requestedCurrency != null ? IsoCodes.currency(requestedCurrency) : "AED";
        }
        String country = vendorCountry(vendor);
        ServiceMarketAvailability market = marketRepository
                .findByCatalogServiceIdAndCountryCodeAndIsAvailableTrue(service.getId(), country)
                .orElseThrow(() -> new BusinessException(
                        "Service " + service.getCode() + " is not available in " + country));
        if (requestedCurrency != null && !market.getCurrencyCode().equals(IsoCodes.currency(requestedCurrency))) {
            throw new BusinessException("Offerings for " + service.getCode() + " in " + country
                    + " must be priced in " + market.getCurrencyCode());
        }
        return market.getCurrencyCode();
    }

    public String vendorCountry(Vendor vendor) {
        return vendor != null && vendor.getCountryCode() != null ? vendor.getCountryCode() : getDefaultCountry();
    }

    private MarketAvailabilityResponse toResponse(ServiceMarketAvailability m) {
        return MarketAvailabilityResponse.builder()
                .id(m.getId())
                .serviceId(m.getCatalogService().getId())
                .serviceCode(m.getCatalogService().getCode())
                .countryCode(m.getCountryCode())
                .currencyCode(m.getCurrencyCode())
                .isAvailable(m.getIsAvailable())
                .regulatoryConfig(m.getRegulatoryConfig())
                .regulatoryNote(m.getRegulatoryNote())
                .regulatoryNoteAr(m.getRegulatoryNoteAr())
                .updatedAt(m.getUpdatedAt())
                .build();
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new BusinessException("regulatoryConfig is not valid JSON");
        }
    }
}
