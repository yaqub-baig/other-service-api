package com.autoadda.apis.other.service;

import com.autoadda.apis.other.entity.CatalogService;
import com.autoadda.apis.other.entity.ServiceAttributeDefinition;
import com.autoadda.apis.other.entity.ServiceCategory;
import com.autoadda.apis.other.exception.AlreadyExistsException;
import com.autoadda.apis.other.exception.BusinessException;
import com.autoadda.apis.other.exception.ResourceNotFoundException;
import com.autoadda.apis.other.mapper.ServiceAttributeDefinitionMapper;
import com.autoadda.apis.other.repository.ServiceAttributeDefinitionRepository;
import com.autoadda.apis.other.repository.ServiceCategoryRepository;
import com.autoadda.apis.other.request.ServiceAttributeDefinitionRequest;
import com.autoadda.apis.other.response.ServiceAttributeDefinitionResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ServiceAttributeDefinitionService {

    private final ServiceAttributeDefinitionRepository attributeRepository;
    private final ServiceCategoryRepository serviceCategoryRepository;
    private final ServiceAttributeDefinitionMapper attributeMapper; // Inject mapper
    private final ServiceCatalogService serviceCatalogService;

    /**
     * Create a new attribute definition for a service category.
     */
    @Transactional
    public ServiceAttributeDefinitionResponse create(ServiceAttributeDefinitionRequest request) {
        // A service-level attribute takes its category from the service
        CatalogService catalogService = request.getServiceId() != null
                ? serviceCatalogService.getForCategory(request.getServiceId(), request.getServiceCategoryId())
                : null;
        Integer categoryId = catalogService != null
                ? catalogService.getServiceCategory().getId()
                : request.getServiceCategoryId();
        if (categoryId == null) {
            throw new BusinessException("serviceCategoryId or serviceId is required");
        }
        ServiceCategory category = serviceCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Service category not found with id: " + categoryId));

        ensureCodeIsFree(categoryId, catalogService, request.getAttributeCode());

        ServiceAttributeDefinition attribute = ServiceAttributeDefinition.builder()
                .serviceCategory(category)
                .catalogService(catalogService)
                .attributeCode(request.getAttributeCode())
                .attributeName(request.getAttributeName())
                .dataType(request.getDataType())
                .unit(request.getUnit())
                .isRequired(request.getIsRequired() != null ? request.getIsRequired() : false)
                .isFilterable(request.getIsFilterable() != null ? request.getIsFilterable() : false)
                .isSearchable(request.getIsSearchable() != null ? request.getIsSearchable() : false)
                .displayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0)
                .description(request.getDescription())
                .defaultValue(request.getDefaultValue())
                .validationRules(request.getValidationRules())
                .isActive(true)
                .build();

        ServiceAttributeDefinition savedAttribute = attributeRepository.save(attribute);
        return attributeMapper.toResponse(savedAttribute);
    }

    /**
     * Get all attributes for a service category.
     */
    @Transactional(readOnly = true)
    public List<ServiceAttributeDefinitionResponse> getByServiceCategory(Integer serviceCategoryId) {
        return attributeMapper.toResponseList(attributeRepository.findByServiceCategoryIdAndCatalogServiceIsNullAndIsActiveTrueOrderByDisplayOrderAsc(serviceCategoryId));
    }

    /**
     * Get all attributes for a service category (including inactive).
     */
    @Transactional(readOnly = true)
    public List<ServiceAttributeDefinitionResponse> getAllByServiceCategory(Integer serviceCategoryId) {
        return attributeMapper.toResponseList(attributeRepository.findByServiceCategoryIdAndCatalogServiceIsNullOrderByDisplayOrderAsc(serviceCategoryId));
    }

    /**
     * Get filterable attributes for a service category.
     */
    @Transactional(readOnly = true)
    public List<ServiceAttributeDefinitionResponse> getFilterableByServiceCategory(Integer serviceCategoryId) {
        return attributeMapper.toResponseList(attributeRepository.findByServiceCategoryIdAndCatalogServiceIsNullAndIsFilterableTrueAndIsActiveTrueOrderByDisplayOrderAsc(serviceCategoryId));
    }

    /**
     * Get required attributes for a service category.
     */
    @Transactional(readOnly = true)
    public List<ServiceAttributeDefinitionResponse> getRequiredByServiceCategory(Integer serviceCategoryId) {
        return attributeMapper.toResponseList(attributeRepository.findByServiceCategoryIdAndCatalogServiceIsNullAndIsRequiredTrueAndIsActiveTrueOrderByDisplayOrderAsc(serviceCategoryId));
    }

    /**
     * Get attribute by ID.
     */
    @Transactional(readOnly = true)
    public ServiceAttributeDefinitionResponse getById(Long id) {
        return attributeRepository.findById(id)
                .map(attributeMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Attribute definition not found with id: " + id));
    }

    public ServiceAttributeDefinition getById1(Long id) {
        return attributeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Attribute definition not found with id: " + id));
    }

    /**
     * Get attribute by code within a service category.
     */
    @Transactional(readOnly = true)
    public ServiceAttributeDefinitionResponse getByCode(Integer serviceCategoryId, String attributeCode) {
        return attributeRepository.findByServiceCategoryIdAndCatalogServiceIsNullAndAttributeCode(serviceCategoryId, attributeCode)
                .map(attributeMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Attribute not found with code: " + attributeCode + " in category: " + serviceCategoryId));
    }

    /**
     * Update an attribute definition.
     */
    @Transactional
    public ServiceAttributeDefinitionResponse update(Long id, ServiceAttributeDefinitionRequest request) {
        ServiceAttributeDefinition attribute = attributeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Attribute definition not found with id: " + id));

        // If changing attribute code, check for duplicates
        if (request.getAttributeCode() != null &&
                !request.getAttributeCode().equals(attribute.getAttributeCode())) {
            ensureCodeIsFree(attribute.getServiceCategory().getId(), attribute.getCatalogService(),
                    request.getAttributeCode());
            attribute.setAttributeCode(request.getAttributeCode());
        }

        if (request.getAttributeName() != null) {
            attribute.setAttributeName(request.getAttributeName());
        }
        if (request.getDataType() != null) {
            attribute.setDataType(request.getDataType());
        }
        if (request.getUnit() != null) {
            attribute.setUnit(request.getUnit());
        }
        if (request.getIsRequired() != null) {
            attribute.setIsRequired(request.getIsRequired());
        }
        if (request.getIsFilterable() != null) {
            attribute.setIsFilterable(request.getIsFilterable());
        }
        if (request.getIsSearchable() != null) {
            attribute.setIsSearchable(request.getIsSearchable());
        }
        if (request.getDisplayOrder() != null) {
            attribute.setDisplayOrder(request.getDisplayOrder());
        }
        if (request.getDescription() != null) {
            attribute.setDescription(request.getDescription());
        }
        if (request.getDefaultValue() != null) {
            attribute.setDefaultValue(request.getDefaultValue());
        }
        if (request.getValidationRules() != null) {
            attribute.setValidationRules(request.getValidationRules());
        }

        ServiceAttributeDefinition updatedAttribute = attributeRepository.save(attribute);
        return attributeMapper.toResponse(updatedAttribute);
    }

    /**
     * Soft delete an attribute definition.
     */
    @Transactional
    public void delete(Long id) {
        ServiceAttributeDefinition attribute = getById1(id);
        attribute.setIsActive(false);
        attributeRepository.save(attribute);
        log.info("Soft deleted attribute definition with id: {}", id);
    }

    /**
     * Hard delete an attribute definition.
     */
    @Transactional
    public void hardDelete(Long id) {
        if (!attributeRepository.existsById(id)) {
            throw new ResourceNotFoundException("Attribute definition not found with id: " + id);
        }
        attributeRepository.deleteById(id);
        log.info("Hard deleted attribute definition with id: {}", id);
    }

    /**
     * Reactivate a soft-deleted attribute definition.
     */
    @Transactional
    public ServiceAttributeDefinitionResponse reactivate(Long id) {
        ServiceAttributeDefinition attribute = attributeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Attribute definition not found with id: " + id));
        attribute.setIsActive(true);
        ServiceAttributeDefinition reactivatedAttribute = attributeRepository.save(attribute);
        return attributeMapper.toResponse(reactivatedAttribute);
    }

    /**
     * Active attributes defined directly on a catalog service.
     */
    @Transactional(readOnly = true)
    public List<ServiceAttributeDefinitionResponse> getByService(Integer serviceId) {
        return attributeMapper.toResponseList(attributeRepository.findByCatalogServiceIdAndIsActiveTrueOrderByDisplayOrderAsc(serviceId));
    }

    private void ensureCodeIsFree(Integer categoryId, CatalogService catalogService, String attributeCode) {
        boolean taken = catalogService != null
                ? attributeRepository.existsByCatalogServiceIdAndAttributeCode(catalogService.getId(), attributeCode)
                : attributeRepository.existsByServiceCategoryIdAndCatalogServiceIsNullAndAttributeCode(categoryId, attributeCode);
        if (taken) {
            throw new AlreadyExistsException("Attribute with code '" + attributeCode + "' already exists for this "
                    + (catalogService != null ? "service" : "category"));
        }
    }

    /**
     * Get all active attributes.
     */
    @Transactional(readOnly = true)
    public List<ServiceAttributeDefinitionResponse> getAllActive() {
        return attributeMapper.toResponseList(attributeRepository.findByIsActiveTrueOrderByServiceCategoryIdAscDisplayOrderAsc());
    }
}
