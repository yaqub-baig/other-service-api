package com.autoadda.apis.other.service;

import com.autoadda.apis.other.entity.ServiceAttributeDefinition;
import com.autoadda.apis.other.entity.ServiceOffering;
import com.autoadda.apis.other.entity.VendorService;
import com.autoadda.apis.other.exception.AlreadyExistsException;
import com.autoadda.apis.other.exception.ResourceNotFoundException;
import com.autoadda.apis.other.repository.ServiceAttributeDefinitionRepository;
import com.autoadda.apis.other.repository.ServiceOfferingRepository;
import com.autoadda.apis.other.repository.VendorServiceRepository;
import com.autoadda.apis.other.request.ServiceOfferingRequest;
import com.autoadda.apis.other.util.PricingType;
import com.autoadda.apis.other.util.StockStatus;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ServiceOfferingService {

    private final ServiceOfferingRepository offeringRepository;
    private final VendorServiceRepository vendorServiceRepository;
    private final ServiceAttributeDefinitionRepository attributeRepository;
    private final ObjectMapper objectMapper;

    /**
     * Create a new service offering.
     */
    @Transactional
    public ServiceOffering create(ServiceOfferingRequest request) {
        // Validate vendor service exists
        VendorService vendorService = vendorServiceRepository.findById(request.getVendorServiceId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Vendor service not found with id: " + request.getVendorServiceId()));

        // Generate SKU if not provided
        String sku = request.getSku();
        if (sku == null || sku.isBlank()) {
            sku = generateSku(vendorService);
        } else if (offeringRepository.existsBySku(sku)) {
            throw new AlreadyExistsException("Offering with SKU '" + sku + "' already exists");
        }

        // Validate filterable attributes against attribute definitions
        String filterableAttributesJson = null;
        if (request.getFilterableAttributes() != null && !request.getFilterableAttributes().isEmpty()) {
            validateFilterableAttributes(vendorService.getServiceCategory().getId(), request.getFilterableAttributes());
            filterableAttributesJson = convertToJson(request.getFilterableAttributes());
        }

        // Convert specifications to JSON
        String specificationsJson = null;
        if (request.getSpecifications() != null && !request.getSpecifications().isEmpty()) {
            specificationsJson = convertToJson(request.getSpecifications());
        }

        // Convert gallery images to JSON
        String galleryImagesJson = null;
        if (request.getGalleryImages() != null && !request.getGalleryImages().isEmpty()) {
            galleryImagesJson = convertToJson(request.getGalleryImages());
        }

        ServiceOffering offering = ServiceOffering.builder()
                .vendorService(vendorService)
                .offeringName(request.getOfferingName())
                .shortDescription(request.getShortDescription())
                .description(request.getDescription())
                .sku(sku)
                .pricingType(request.getPricingType() != null ? request.getPricingType() : PricingType.FIXED)
                .price(request.getPrice())
                .minPrice(request.getMinPrice())
                .maxPrice(request.getMaxPrice())
                .originalPrice(request.getOriginalPrice())
                .currency(request.getCurrency() != null ? request.getCurrency() : "AED")
                .stockStatus(request.getStockStatus() != null ? request.getStockStatus() : StockStatus.IN_STOCK)
                .quantityAvailable(request.getQuantityAvailable())
                .filterableAttributes(filterableAttributesJson)
                .specifications(specificationsJson)
                .imageUrl(request.getImageUrl())
                .galleryImages(galleryImagesJson)
                .displayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0)
                .isFeatured(request.getIsFeatured() != null ? request.getIsFeatured() : false)
                .isActive(true)
                .build();

        return offeringRepository.save(offering);
    }

    /**
     * Get offering by ID.
     */
    @Transactional(readOnly = true)
    public ServiceOffering getById(Long id) {
        return offeringRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Service offering not found with id: " + id));
    }

    /**
     * Get offering by SKU.
     */
    @Transactional(readOnly = true)
    public ServiceOffering getBySku(String sku) {
        return offeringRepository.findBySku(sku)
                .orElseThrow(() -> new ResourceNotFoundException("Service offering not found with SKU: " + sku));
    }

    /**
     * Get all active offerings for a vendor service.
     */
    @Transactional(readOnly = true)
    public List<ServiceOffering> getByVendorService(Integer vendorServiceId) {
        return offeringRepository.findByVendorServiceIdAndIsActiveTrueOrderByDisplayOrderAsc(vendorServiceId);
    }

    /**
     * Get all offerings for a vendor service (including inactive).
     */
    @Transactional(readOnly = true)
    public List<ServiceOffering> getAllByVendorService(Integer vendorServiceId) {
        return offeringRepository.findByVendorServiceIdOrderByDisplayOrderAsc(vendorServiceId);
    }

    /**
     * Get all active offerings for a vendor.
     */
    @Transactional(readOnly = true)
    public List<ServiceOffering> getByVendor(Integer vendorId) {
        return offeringRepository.findByVendorIdAndIsActiveTrue(vendorId);
    }

    /**
     * Get all offerings for a vendor (including inactive).
     */
    @Transactional(readOnly = true)
    public List<ServiceOffering> getAllByVendor(Integer vendorId) {
        return offeringRepository.findByVendorId(vendorId);
    }

    /**
     * Get offerings by category.
     */
    @Transactional(readOnly = true)
    public List<ServiceOffering> getByCategory(Integer categoryId) {
        return offeringRepository.findByCategoryIdAndIsActiveTrue(categoryId);
    }

    /**
     * Get paginated offerings by category.
     */
    @Transactional(readOnly = true)
    public Page<ServiceOffering> getByCategoryPaginated(Integer categoryId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return offeringRepository.findByCategoryIdAndIsActiveTrue(categoryId, pageable);
    }

    /**
     * Search offerings by name.
     */
    @Transactional(readOnly = true)
    public List<ServiceOffering> searchByName(String searchTerm) {
        return offeringRepository.searchByName(searchTerm);
    }

    /**
     * Search offerings by name within a category.
     */
    @Transactional(readOnly = true)
    public List<ServiceOffering> searchByNameInCategory(Integer categoryId, String searchTerm) {
        return offeringRepository.searchByNameInCategory(categoryId, searchTerm);
    }

    /**
     * Get offerings by price range.
     */
    @Transactional(readOnly = true)
    public List<ServiceOffering> getByPriceRange(Integer categoryId, BigDecimal minPrice, BigDecimal maxPrice) {
        return offeringRepository.findByCategoryIdAndPriceRange(categoryId, minPrice, maxPrice);
    }

    /**
     * Get featured offerings.
     */
    @Transactional(readOnly = true)
    public List<ServiceOffering> getFeatured() {
        return offeringRepository.findByIsFeaturedTrueAndIsActiveTrueOrderByDisplayOrderAsc();
    }

    /**
     * Get featured offerings by category.
     */
    @Transactional(readOnly = true)
    public List<ServiceOffering> getFeaturedByCategory(Integer categoryId) {
        return offeringRepository.findFeaturedByCategoryId(categoryId);
    }

    /**
     * Get top-rated offerings.
     */
    @Transactional(readOnly = true)
    public List<ServiceOffering> getTopRated(int limit) {
        return offeringRepository.findTopRated(PageRequest.of(0, limit));
    }

    /**
     * Get most popular offerings.
     */
    @Transactional(readOnly = true)
    public List<ServiceOffering> getMostPopular(int limit) {
        return offeringRepository.findMostPopular(PageRequest.of(0, limit));
    }

    /**
     * Update an offering.
     */
    @Transactional
    public ServiceOffering update(Long id, ServiceOfferingRequest request) {
        ServiceOffering offering = getById(id);

        // If changing SKU, check for duplicates
        if (request.getSku() != null && !request.getSku().equals(offering.getSku())) {
            if (offeringRepository.existsBySku(request.getSku())) {
                throw new AlreadyExistsException("Offering with SKU '" + request.getSku() + "' already exists");
            }
            offering.setSku(request.getSku());
        }

        if (request.getOfferingName() != null) {
            offering.setOfferingName(request.getOfferingName());
        }
        if (request.getShortDescription() != null) {
            offering.setShortDescription(request.getShortDescription());
        }
        if (request.getDescription() != null) {
            offering.setDescription(request.getDescription());
        }
        if (request.getPricingType() != null) {
            offering.setPricingType(request.getPricingType());
        }
        if (request.getPrice() != null) {
            offering.setPrice(request.getPrice());
        }
        if (request.getMinPrice() != null) {
            offering.setMinPrice(request.getMinPrice());
        }
        if (request.getMaxPrice() != null) {
            offering.setMaxPrice(request.getMaxPrice());
        }
        if (request.getOriginalPrice() != null) {
            offering.setOriginalPrice(request.getOriginalPrice());
        }
        if (request.getCurrency() != null) {
            offering.setCurrency(request.getCurrency());
        }
        if (request.getStockStatus() != null) {
            offering.setStockStatus(request.getStockStatus());
        }
        if (request.getQuantityAvailable() != null) {
            offering.setQuantityAvailable(request.getQuantityAvailable());
        }
        if (request.getFilterableAttributes() != null) {
            validateFilterableAttributes(
                    offering.getVendorService().getServiceCategory().getId(),
                    request.getFilterableAttributes());
            offering.setFilterableAttributes(convertToJson(request.getFilterableAttributes()));
        }
        if (request.getSpecifications() != null) {
            offering.setSpecifications(convertToJson(request.getSpecifications()));
        }
        if (request.getImageUrl() != null) {
            offering.setImageUrl(request.getImageUrl());
        }
        if (request.getGalleryImages() != null) {
            offering.setGalleryImages(convertToJson(request.getGalleryImages()));
        }
        if (request.getDisplayOrder() != null) {
            offering.setDisplayOrder(request.getDisplayOrder());
        }
        if (request.getIsFeatured() != null) {
            offering.setIsFeatured(request.getIsFeatured());
        }

        return offeringRepository.save(offering);
    }

    /**
     * Update stock status.
     */
    @Transactional
    public ServiceOffering updateStockStatus(Long id, StockStatus stockStatus, Integer quantity) {
        ServiceOffering offering = getById(id);
        offering.setStockStatus(stockStatus);
        if (quantity != null) {
            offering.setQuantityAvailable(quantity);
        }
        return offeringRepository.save(offering);
    }

    /**
     * Toggle featured status.
     */
    @Transactional
    public ServiceOffering toggleFeatured(Long id) {
        ServiceOffering offering = getById(id);
        offering.setIsFeatured(!offering.getIsFeatured());
        return offeringRepository.save(offering);
    }

    /**
     * Soft delete an offering.
     */
    @Transactional
    public void delete(Long id) {
        ServiceOffering offering = getById(id);
        offering.setIsActive(false);
        offeringRepository.save(offering);
        log.info("Soft deleted service offering with id: {}", id);
    }

    /**
     * Hard delete an offering.
     */
    @Transactional
    public void hardDelete(Long id) {
        if (!offeringRepository.existsById(id)) {
            throw new ResourceNotFoundException("Service offering not found with id: " + id);
        }
        offeringRepository.deleteById(id);
        log.info("Hard deleted service offering with id: {}", id);
    }

    /**
     * Reactivate a soft-deleted offering.
     */
    @Transactional
    public ServiceOffering reactivate(Long id) {
        ServiceOffering offering = getById(id);
        offering.setIsActive(true);
        return offeringRepository.save(offering);
    }

    /**
     * Get offerings by vendor and category.
     */
    @Transactional(readOnly = true)
    public List<ServiceOffering> getByVendorAndCategory(Integer vendorId, Integer categoryId) {
        return offeringRepository.findByVendorIdAndCategoryId(vendorId, categoryId);
    }

    /**
     * Filter offerings by a single attribute.
     */
    @Transactional(readOnly = true)
    public List<ServiceOffering> filterByAttribute(String attributeCode, String value) {
        String jsonPath = "$." + attributeCode;
        return offeringRepository.findByFilterableAttribute(jsonPath, "\"" + value + "\"");
    }

    /**
     * Filter offerings by multiple attributes.
     */
    @Transactional(readOnly = true)
    public List<ServiceOffering> filterByAttributes(Map<String, Object> attributes) {
        String jsonFragment = convertToJson(attributes);
        return offeringRepository.findByFilterableAttributesContaining(jsonFragment);
    }

    /**
     * Get attribute definitions for a category (helper for frontend forms).
     */
    @Transactional(readOnly = true)
    public List<ServiceAttributeDefinition> getAttributeDefinitions(Integer categoryId) {
        return attributeRepository.findByServiceCategoryIdAndIsActiveTrueOrderByDisplayOrderAsc(categoryId);
    }

    /**
     * Get required attribute definitions for a category.
     */
    @Transactional(readOnly = true)
    public List<ServiceAttributeDefinition> getRequiredAttributeDefinitions(Integer categoryId) {
        return attributeRepository.findByServiceCategoryIdAndIsRequiredTrueAndIsActiveTrueOrderByDisplayOrderAsc(categoryId);
    }

    // Helper methods

    private String generateSku(VendorService vendorService) {
        String categoryPrefix = vendorService.getServiceCategory().getName()
                .substring(0, Math.min(3, vendorService.getServiceCategory().getName().length()))
                .toUpperCase();
        String vendorPrefix = String.format("%04d", vendorService.getVendor().getId());
        String uniquePart = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return categoryPrefix + "-" + vendorPrefix + "-" + uniquePart;
    }

    private void validateFilterableAttributes(Integer categoryId, Map<String, Object> attributes) {
        // Get required attributes for this category
        List<ServiceAttributeDefinition> requiredAttributes =
                attributeRepository.findByServiceCategoryIdAndIsRequiredTrueAndIsActiveTrueOrderByDisplayOrderAsc(categoryId);

        // Check all required attributes are present
        for (ServiceAttributeDefinition attr : requiredAttributes) {
            if (!attributes.containsKey(attr.getAttributeCode())) {
                throw new IllegalArgumentException(
                        "Required attribute '" + attr.getAttributeCode() + "' (" + attr.getAttributeName() + ") is missing");
            }
        }

        // Validate attribute codes exist in definitions
        List<ServiceAttributeDefinition> allAttributes =
                attributeRepository.findByServiceCategoryIdAndIsActiveTrueOrderByDisplayOrderAsc(categoryId);

        for (String attrCode : attributes.keySet()) {
            boolean found = allAttributes.stream()
                    .anyMatch(def -> def.getAttributeCode().equals(attrCode));
            if (!found) {
                log.warn("Unknown attribute code '{}' for category {}. It will be stored but may not be searchable.",
                        attrCode, categoryId);
            }
        }
    }

    private String convertToJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to convert to JSON", e);
        }
    }
}
