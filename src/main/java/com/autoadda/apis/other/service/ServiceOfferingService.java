package com.autoadda.apis.other.service;

import com.autoadda.apis.other.entity.ServiceAttributeDefinition;
import com.autoadda.apis.other.entity.ServiceOffering;
import com.autoadda.apis.other.entity.VendorService;
import com.autoadda.apis.other.exception.AlreadyExistsException;
import com.autoadda.apis.other.exception.ResourceNotFoundException;
import com.autoadda.apis.other.mapper.ServiceAttributeDefinitionMapper;
import com.autoadda.apis.other.mapper.ServiceOfferingMapper;
import com.autoadda.apis.other.repository.ServiceAttributeDefinitionRepository;
import com.autoadda.apis.other.repository.ServiceOfferingRepository;
import com.autoadda.apis.other.repository.VendorServiceRepository;
import com.autoadda.apis.other.request.ServiceOfferingRequest;
import com.autoadda.apis.other.response.ServiceAttributeDefinitionResponse;
import com.autoadda.apis.other.response.ServiceOfferingResponse;
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
    private final ServiceOfferingMapper offeringMapper;
    private final ServiceAttributeDefinitionMapper attributeMapper;
    private final ServiceCatalogService serviceCatalogService;

    /**
     * Create a new service offering.
     */
    @Transactional
    public ServiceOfferingResponse create(ServiceOfferingRequest request) {
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
                .catalogService(request.getServiceId() != null
                        ? serviceCatalogService.getForCategory(request.getServiceId(), vendorService.getServiceCategory().getId())
                        : null)
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

        ServiceOffering saved = offeringRepository.save(offering);
        return offeringMapper.toResponse(saved);
    }

    /**
     * Get offering by ID.
     */
    @Transactional(readOnly = true)
    public ServiceOfferingResponse getById(Long id) {
        ServiceOffering offering = findEntityById(id);
        return offeringMapper.toResponse(offering);
    }

    /**
     * Get offering by SKU.
     */
    @Transactional(readOnly = true)
    public ServiceOfferingResponse getBySku(String sku) {
        ServiceOffering offering = offeringRepository.findBySku(sku)
                .orElseThrow(() -> new ResourceNotFoundException("Service offering not found with SKU: " + sku));
        return offeringMapper.toResponse(offering);
    }

    /**
     * Get all active offerings for a vendor service.
     */
    @Transactional(readOnly = true)
    public List<ServiceOfferingResponse> getByVendorService(Integer vendorServiceId) {
        List<ServiceOffering> offerings = offeringRepository.findByVendorServiceIdAndIsActiveTrueOrderByDisplayOrderAsc(vendorServiceId);
        return offeringMapper.toResponseList(offerings);
    }

    /**
     * Get all offerings for a vendor service (including inactive).
     */
    @Transactional(readOnly = true)
    public List<ServiceOfferingResponse> getAllByVendorService(Integer vendorServiceId) {
        List<ServiceOffering> offerings = offeringRepository.findByVendorServiceIdOrderByDisplayOrderAsc(vendorServiceId);
        return offeringMapper.toResponseList(offerings);
    }

    /**
     * Get all active offerings for a vendor.
     */
    @Transactional(readOnly = true)
    public List<ServiceOfferingResponse> getByVendor(Integer vendorId) {
        List<ServiceOffering> offerings = offeringRepository.findByVendorIdAndIsActiveTrue(vendorId);
        return offeringMapper.toResponseList(offerings);
    }

    /**
     * Get all offerings for a vendor (including inactive).
     */
    @Transactional(readOnly = true)
    public List<ServiceOfferingResponse> getAllByVendor(Integer vendorId) {
        List<ServiceOffering> offerings = offeringRepository.findByVendorId(vendorId);
        return offeringMapper.toResponseList(offerings);
    }

    /**
     * Get offerings by category.
     */
    @Transactional(readOnly = true)
    public List<ServiceOfferingResponse> getByCategory(Integer categoryId) {
        List<ServiceOffering> offerings = offeringRepository.findByCategoryIdAndIsActiveTrue(categoryId);
        return offeringMapper.toResponseList(offerings);
    }

    /**
     * Get paginated offerings by category.
     */
    @Transactional(readOnly = true)
    public Page<ServiceOfferingResponse> getByCategoryPaginated(Integer categoryId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ServiceOffering> offerings = offeringRepository.findByCategoryIdAndIsActiveTrue(categoryId, pageable);
        return offerings.map(offeringMapper::toResponse);
    }

    /**
     * Search offerings by name.
     */
    @Transactional(readOnly = true)
    public List<ServiceOfferingResponse> searchByName(String searchTerm) {
        List<ServiceOffering> offerings = offeringRepository.searchByName(searchTerm);
        return offeringMapper.toResponseList(offerings);
    }

    /**
     * Search offerings by name within a category.
     */
    @Transactional(readOnly = true)
    public List<ServiceOfferingResponse> searchByNameInCategory(Integer categoryId, String searchTerm) {
        List<ServiceOffering> offerings = offeringRepository.searchByNameInCategory(categoryId, searchTerm);
        return offeringMapper.toResponseList(offerings);
    }

    /**
     * Get offerings by price range.
     */
    @Transactional(readOnly = true)
    public List<ServiceOfferingResponse> getByPriceRange(Integer categoryId, BigDecimal minPrice, BigDecimal maxPrice) {
        List<ServiceOffering> offerings = offeringRepository.findByCategoryIdAndPriceRange(categoryId, minPrice, maxPrice);
        return offeringMapper.toResponseList(offerings);
    }

    /**
     * Get featured offerings.
     */
    @Transactional(readOnly = true)
    public List<ServiceOfferingResponse> getFeatured() {
        List<ServiceOffering> offerings = offeringRepository.findByIsFeaturedTrueAndIsActiveTrueOrderByDisplayOrderAsc();
        return offeringMapper.toResponseList(offerings);
    }

    /**
     * Get featured offerings by category.
     */
    @Transactional(readOnly = true)
    public List<ServiceOfferingResponse> getFeaturedByCategory(Integer categoryId) {
        List<ServiceOffering> offerings = offeringRepository.findFeaturedByCategoryId(categoryId);
        return offeringMapper.toResponseList(offerings);
    }

    /**
     * Get top-rated offerings.
     */
    @Transactional(readOnly = true)
    public List<ServiceOfferingResponse> getTopRated(int limit) {
        List<ServiceOffering> offerings = offeringRepository.findTopRated(PageRequest.of(0, limit));
        return offeringMapper.toResponseList(offerings);
    }

    /**
     * Get most popular offerings.
     */
    @Transactional(readOnly = true)
    public List<ServiceOfferingResponse> getMostPopular(int limit) {
        List<ServiceOffering> offerings = offeringRepository.findMostPopular(PageRequest.of(0, limit));
        return offeringMapper.toResponseList(offerings);
    }

    /**
     * Update an offering.
     */
    @Transactional
    public ServiceOfferingResponse update(Long id, ServiceOfferingRequest request) {
        ServiceOffering offering = findEntityById(id);

        // If changing SKU, check for duplicates
        if (request.getSku() != null && !request.getSku().equals(offering.getSku())) {
            if (offeringRepository.existsBySku(request.getSku())) {
                throw new AlreadyExistsException("Offering with SKU '" + request.getSku() + "' already exists");
            }
            offering.setSku(request.getSku());
        }

        if (request.getServiceId() != null) {
            offering.setCatalogService(serviceCatalogService.getForCategory(
                    request.getServiceId(), offering.getVendorService().getServiceCategory().getId()));
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

        ServiceOffering saved = offeringRepository.save(offering);
        return offeringMapper.toResponse(saved);
    }

    /**
     * Update stock status.
     */
    @Transactional
    public ServiceOfferingResponse updateStockStatus(Long id, StockStatus stockStatus, Integer quantity) {
        ServiceOffering offering = findEntityById(id);
        offering.setStockStatus(stockStatus);
        if (quantity != null) {
            offering.setQuantityAvailable(quantity);
        }
        ServiceOffering saved = offeringRepository.save(offering);
        return offeringMapper.toResponse(saved);
    }

    /**
     * Toggle featured status.
     */
    @Transactional
    public ServiceOfferingResponse toggleFeatured(Long id) {
        ServiceOffering offering = findEntityById(id);
        offering.setIsFeatured(!offering.getIsFeatured());
        ServiceOffering saved = offeringRepository.save(offering);
        return offeringMapper.toResponse(saved);
    }

    /**
     * Soft delete an offering.
     */
    @Transactional
    public void delete(Long id) {
        ServiceOffering offering = findEntityById(id);
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
    public ServiceOfferingResponse reactivate(Long id) {
        ServiceOffering offering = findEntityById(id);
        offering.setIsActive(true);
        ServiceOffering saved = offeringRepository.save(offering);
        return offeringMapper.toResponse(saved);
    }

    /**
     * Get offerings by vendor and category.
     */
    @Transactional(readOnly = true)
    public List<ServiceOfferingResponse> getByVendorAndCategory(Integer vendorId, Integer categoryId) {
        List<ServiceOffering> offerings = offeringRepository.findByVendorIdAndCategoryId(vendorId, categoryId);
        return offeringMapper.toResponseList(offerings);
    }

    /**
     * Filter offerings by a single attribute.
     */
    @Transactional(readOnly = true)
    public List<ServiceOfferingResponse> filterByAttribute(String attributeCode, String value) {
        String jsonPath = "$." + attributeCode;
        List<ServiceOffering> offerings = offeringRepository.findByFilterableAttribute(jsonPath, "\"" + value + "\"");
        return offeringMapper.toResponseList(offerings);
    }

    /**
     * Filter offerings by multiple attributes.
     */
    @Transactional(readOnly = true)
    public List<ServiceOfferingResponse> filterByAttributes(Map<String, Object> attributes) {
        String jsonFragment = convertToJson(attributes);
        List<ServiceOffering> offerings = offeringRepository.findByFilterableAttributesContaining(jsonFragment);
        return offeringMapper.toResponseList(offerings);
    }

    /**
     * Get attribute definitions for a category (helper for frontend forms).
     */
    @Transactional(readOnly = true)
    public List<ServiceAttributeDefinitionResponse> getAttributeDefinitions(Integer categoryId) {
        List<ServiceAttributeDefinition> attributes = attributeRepository.findByServiceCategoryIdAndCatalogServiceIsNullAndIsActiveTrueOrderByDisplayOrderAsc(categoryId);
        return attributeMapper.toResponseList(attributes);
    }

    /**
     * Get required attribute definitions for a category.
     */
    @Transactional(readOnly = true)
    public List<ServiceAttributeDefinitionResponse> getRequiredAttributeDefinitions(Integer categoryId) {
        List<ServiceAttributeDefinition> attributes = attributeRepository.findByServiceCategoryIdAndCatalogServiceIsNullAndIsRequiredTrueAndIsActiveTrueOrderByDisplayOrderAsc(categoryId);
        return attributeMapper.toResponseList(attributes);
    }

    /**
     * Internal method to get entity by ID (for update operations).
     */
    private ServiceOffering findEntityById(Long id) {
        return offeringRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Service offering not found with id: " + id));
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
                attributeRepository.findByServiceCategoryIdAndCatalogServiceIsNullAndIsRequiredTrueAndIsActiveTrueOrderByDisplayOrderAsc(categoryId);

        // Check all required attributes are present
        for (ServiceAttributeDefinition attr : requiredAttributes) {
            if (!attributes.containsKey(attr.getAttributeCode())) {
                throw new IllegalArgumentException(
                        "Required attribute '" + attr.getAttributeCode() + "' (" + attr.getAttributeName() + ") is missing");
            }
        }

        // Validate attribute codes exist in definitions
        List<ServiceAttributeDefinition> allAttributes =
                attributeRepository.findByServiceCategoryIdAndCatalogServiceIsNullAndIsActiveTrueOrderByDisplayOrderAsc(categoryId);

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
