package com.autoadda.apis.other.mapper;

import com.autoadda.apis.other.entity.Location;
import com.autoadda.apis.other.entity.VendorService;
import com.autoadda.apis.other.response.LocationResponse;
import com.autoadda.apis.other.response.VendorServiceResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Manual mapper for {@link VendorService}.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class VendorServiceMapper {

    private final MediaFileMapper mediaFileMapper;
    private final ObjectMapper objectMapper;

    public VendorServiceResponse toResponse(VendorService e) {
        return Optional.ofNullable(e).map(v -> {
            VendorServiceResponse r = new VendorServiceResponse();
            r.setId(v.getId());
            r.setIsActive(v.getIsActive());
            r.setVendorId(v.getVendor() != null ? v.getVendor().getId() : null);
            r.setVendorName(v.getVendor() != null ? v.getVendor().getName() : null);
            r.setServiceCategoryId(v.getServiceCategory() != null ? v.getServiceCategory().getId() : null);
            r.setServiceCategoryName(v.getServiceCategory() != null ? v.getServiceCategory().getName() : null);
            r.setServiceType(v.getServiceType());
            r.setOpeningTime(v.getOpeningTime());
            r.setClosingTime(v.getClosingTime());
            r.setWorkingDays(v.getWorkingDays());
            r.setConcurrentAppointmentCapacity(v.getConcurrentAppointmentCapacity());
            r.setTradeLicenceNumber(v.getTradeLicenceNumber());
            r.setAccountStatus(v.getAccountStatus());
            r.setPricingType(v.getPricingType());
            r.setPrice(v.getPrice());
            r.setMinPrice(v.getMinPrice());
            r.setMaxPrice(v.getMaxPrice());
            r.setCurrency(v.getCurrency());
            r.setDescription(v.getDescription());
            r.setAttributes(readAttributes(v));
            if (v.getServiceLocation() != null) {
                r.setServiceLocation(toLocationResponse(v.getServiceLocation()));
            }
            if (v.getServiceImages() != null && !v.getServiceImages().isEmpty()) {
                r.setServiceImages(v.getServiceImages().stream()
                        .map(mediaFileMapper::toMediaFileResponse).collect(Collectors.toList()));
            }
            if (v.getTradeLicenseAttachment() != null && !v.getTradeLicenseAttachment().isEmpty()) {
                r.setTradeLicenseAttachment(v.getTradeLicenseAttachment().stream()
                        .map(mediaFileMapper::toMediaFileResponse).collect(Collectors.toList()));
            }
            return r;
        }).orElse(null);
    }

    public List<VendorServiceResponse> toResponseList(List<VendorService> entities) {
        if (entities == null || entities.isEmpty()) {
            return Collections.emptyList();
        }
        return entities.stream().map(this::toResponse).collect(Collectors.toList());
    }

    /**
     * The stored VENDOR attribute values; empty when none are set.
     */
    public Map<String, Object> readAttributes(VendorService v) {
        if (v.getAttributes() == null || v.getAttributes().isBlank()) {
            return Map.of();
        }
        try {
            return objectMapper.readValue(v.getAttributes(), new TypeReference<Map<String, Object>>() {});
        } catch (JsonProcessingException e) {
            log.warn("Ignoring unreadable attributes JSON on vendor service {}", v.getId());
            return Map.of();
        }
    }

    private LocationResponse toLocationResponse(Location location) {
        LocationResponse r = new LocationResponse();
        r.setAddress(location.getStreetAddress1());
        r.setCity(location.getCity());
        r.setState(location.getState());
        r.setCountry(location.getCountry());
        r.setZip(location.getPostalCode());
        r.setLatitude(location.getLatitude());
        r.setLongitude(location.getLongitude());
        return r;
    }
}
