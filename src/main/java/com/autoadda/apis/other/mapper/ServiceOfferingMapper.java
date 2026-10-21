package com.autoadda.apis.other.mapper;

import com.autoadda.apis.other.util.Localized;
import com.autoadda.apis.other.entity.ServiceOffering;
import com.autoadda.apis.other.response.ServiceOfferingResponse;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Manual mapper for ServiceOffering entity.
 */
@Component
public class ServiceOfferingMapper {

    public ServiceOfferingResponse toResponse(ServiceOffering entity) {
        return Optional.ofNullable(entity).map(e -> {
            ServiceOfferingResponse response = new ServiceOfferingResponse();
            response.setId(e.getId());
            response.setVendorServiceId(e.getVendorService() != null ? e.getVendorService().getId() : null);
            // VendorService doesn't have a name, use serviceCategory name instead
            response.setVendorServiceName(e.getVendorService() != null && e.getVendorService().getServiceCategory() != null
                    ? e.getVendorService().getServiceCategory().getName() : null);
            response.setVendorId(e.getVendorService() != null && e.getVendorService().getVendor() != null
                    ? e.getVendorService().getVendor().getId() : null);
            response.setVendorName(e.getVendorService() != null && e.getVendorService().getVendor() != null
                    ? e.getVendorService().getVendor().getName() : null);
            response.setServiceCategoryId(e.getVendorService() != null && e.getVendorService().getServiceCategory() != null
                    ? e.getVendorService().getServiceCategory().getId() : null);
            response.setServiceCategoryName(e.getVendorService() != null && e.getVendorService().getServiceCategory() != null
                    ? e.getVendorService().getServiceCategory().getName() : null);
            if (e.getCatalogService() != null) {
                response.setServiceId(e.getCatalogService().getId());
                response.setServiceCode(e.getCatalogService().getCode());
                response.setServiceName(Localized.pick(e.getCatalogService().getName(), e.getCatalogService().getNameAr()));
            }
            response.setOfferingName(e.getOfferingName());
            response.setShortDescription(e.getShortDescription());
            response.setDescription(e.getDescription());
            response.setSku(e.getSku());
            response.setPricingType(e.getPricingType());
            response.setPrice(e.getPrice());
            response.setMinPrice(e.getMinPrice());
            response.setMaxPrice(e.getMaxPrice());
            response.setOriginalPrice(e.getOriginalPrice());
            response.setCurrency(e.getCurrency());
            response.setStockStatus(e.getStockStatus());
            response.setQuantityAvailable(e.getQuantityAvailable());
            response.setFilterableAttributes(e.getFilterableAttributes());
            response.setSpecifications(e.getSpecifications());
            response.setImageUrl(e.getImageUrl());
            response.setGalleryImages(e.getGalleryImages());
            response.setAverageRating(e.getAverageRating());
            response.setTotalRatings(e.getTotalRatings());
            response.setTotalOrders(e.getTotalOrders());
            response.setDisplayOrder(e.getDisplayOrder());
            response.setIsFeatured(e.getIsFeatured());
            response.setIsActive(e.getIsActive());
            response.setCreatedAt(e.getCreatedAt());
            response.setUpdatedAt(e.getUpdatedAt());
            return response;
        }).orElse(null);
    }

    public List<ServiceOfferingResponse> toResponseList(List<ServiceOffering> entities) {
        if (entities == null || entities.isEmpty()) {
            return Collections.emptyList();
        }
        return entities.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
}
