package com.autoadda.apis.other.service;

import org.springframework.context.ApplicationEventPublisher;
import com.autoadda.apis.other.search.OfferingAttributeIndexer;
import com.autoadda.apis.other.util.AttributeCodes;
import com.autoadda.apis.other.entity.CatalogService;
import com.autoadda.apis.other.entity.ServiceAttributeDefinition;
import com.autoadda.apis.other.entity.ServiceCategory;
import com.autoadda.apis.other.entity.ServiceSchemaVersion;
import com.autoadda.apis.other.exception.AlreadyExistsException;
import com.autoadda.apis.other.exception.BusinessException;
import com.autoadda.apis.other.exception.ResourceNotFoundException;
import com.autoadda.apis.other.mapper.ServiceAttributeDefinitionMapper;
import com.autoadda.apis.other.repository.ServiceAttributeDefinitionRepository;
import com.autoadda.apis.other.repository.ServiceCategoryRepository;
import com.autoadda.apis.other.request.AttributeOption;
import com.autoadda.apis.other.request.ServiceAttributeDefinitionRequest;
import com.autoadda.apis.other.response.ServiceAttributeDefinitionResponse;
import com.autoadda.apis.other.util.DataType;
import com.autoadda.apis.other.util.InputContext;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ServiceAttributeDefinitionService {

    private final ServiceAttributeDefinitionRepository attributeRepository;
    private final ServiceCategoryRepository serviceCategoryRepository;
    private final ServiceAttributeDefinitionMapper attributeMapper; // Inject mapper
    private final ServiceCatalogService serviceCatalogService;
    private final ObjectMapper objectMapper;
    private final SchemaVersionService schemaVersionService;
    private final ApplicationEventPublisher eventPublisher;

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

        // Service-level attributes always go into the service's open draft (opened if needed)
        ServiceSchemaVersion draft = catalogService != null
                ? schemaVersionService.getOrCreateDraft(catalogService.getId(), null)
                : null;
        AttributeCodes.requireValid(request.getAttributeCode());
        ensureCodeIsFree(categoryId, draft, request.getAttributeCode());

        ServiceAttributeDefinition attribute = ServiceAttributeDefinition.builder()
                .serviceCategory(category)
                .catalogService(catalogService)
                .schemaVersion(draft)
                .attributeCode(request.getAttributeCode())
                .attributeName(request.getAttributeName())
                .attributeNameAr(request.getAttributeNameAr())
                .descriptionAr(request.getDescriptionAr())
                .dataType(request.getDataType())
                .unit(request.getUnit())
                .isRequired(request.getIsRequired() != null ? request.getIsRequired() : false)
                .isFilterable(request.getIsFilterable() != null ? request.getIsFilterable() : false)
                .isSearchable(request.getIsSearchable() != null ? request.getIsSearchable() : false)
                .isIndexed(request.getIsIndexed() != null ? request.getIsIndexed() : false)
                .displayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0)
                .description(request.getDescription())
                .defaultValue(request.getDefaultValue())
                .validationRules(request.getValidationRules())
                .options(toOptionsJson(request.getOptions()))
                .inputContext(request.getInputContext() != null ? request.getInputContext() : InputContext.BOTH)
                .isActive(true)
                .build();
        checkDefinition(attribute);

        ServiceAttributeDefinition savedAttribute = attributeRepository.save(attribute);
        if (Boolean.TRUE.equals(savedAttribute.getIsIndexed())) {
            eventPublisher.publishEvent(new OfferingAttributeIndexer.IndexedAttributesChanged());
        }
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
        schemaVersionService.requireEditable(attribute);

        // If changing attribute code, check for duplicates
        if (request.getAttributeCode() != null &&
                !request.getAttributeCode().equals(attribute.getAttributeCode())) {
            AttributeCodes.requireValid(request.getAttributeCode());
            ensureCodeIsFree(attribute.getServiceCategory().getId(), attribute.getSchemaVersion(),
                    request.getAttributeCode());
            attribute.setAttributeCode(request.getAttributeCode());
        }

        if (request.getAttributeName() != null) {
            attribute.setAttributeName(request.getAttributeName());
        }
        if (request.getAttributeNameAr() != null) {
            attribute.setAttributeNameAr(request.getAttributeNameAr());
        }
        if (request.getDescriptionAr() != null) {
            attribute.setDescriptionAr(request.getDescriptionAr());
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
        if (request.getIsIndexed() != null) {
            attribute.setIsIndexed(request.getIsIndexed());
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
        if (request.getOptions() != null) {
            attribute.setOptions(toOptionsJson(request.getOptions()));
        }
        if (request.getInputContext() != null) {
            attribute.setInputContext(request.getInputContext());
        }
        checkDefinition(attribute);

        ServiceAttributeDefinition updatedAttribute = attributeRepository.save(attribute);
        if (request.getIsIndexed() != null) {
            eventPublisher.publishEvent(new OfferingAttributeIndexer.IndexedAttributesChanged());
        }
        return attributeMapper.toResponse(updatedAttribute);
    }

    /**
     * Soft delete an attribute definition.
     */
    @Transactional
    public void delete(Long id) {
        ServiceAttributeDefinition attribute = getById1(id);
        schemaVersionService.requireEditable(attribute);
        attribute.setIsActive(false);
        attributeRepository.save(attribute);
        log.info("Soft deleted attribute definition with id: {}", id);
    }

    /**
     * Reactivate a soft-deleted attribute definition.
     */
    @Transactional
    public ServiceAttributeDefinitionResponse reactivate(Long id) {
        ServiceAttributeDefinition attribute = attributeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Attribute definition not found with id: " + id));
        schemaVersionService.requireEditable(attribute);
        attribute.setIsActive(true);
        ServiceAttributeDefinition reactivatedAttribute = attributeRepository.save(attribute);
        return attributeMapper.toResponse(reactivatedAttribute);
    }

    /**
     * Attributes of the service's newest schema version: the open draft if there is one,
     * otherwise the published version.
     */
    @Transactional(readOnly = true)
    public List<ServiceAttributeDefinitionResponse> getByService(Integer serviceId) {
        return schemaVersionService.latest(serviceId)
                .map(v -> attributeMapper.toResponseList(attributeRepository.findBySchemaVersionIdOrderByDisplayOrderAsc(v.getId())))
                .orElse(List.of());
    }

    @Transactional(readOnly = true)
    public List<ServiceAttributeDefinitionResponse> getByVersion(Integer serviceId, Integer versionId) {
        schemaVersionService.getForService(serviceId, versionId);
        return attributeMapper.toResponseList(attributeRepository.findBySchemaVersionIdOrderByDisplayOrderAsc(versionId));
    }

    /**
     * The attributes that apply to an offering or request: the service's published schema version,
     * plus the category defaults whose code that version does not redefine. Drafts are not live.
     * VENDOR attributes are left out. Ordered by display order.
     */
    @Transactional(readOnly = true)
    public List<ServiceAttributeDefinition> resolveAttributes(Integer categoryId, Integer serviceId) {
        Map<String, ServiceAttributeDefinition> byCode = new LinkedHashMap<>();
        if (serviceId != null) {
            schemaVersionService.currentPublished(serviceId).ifPresent(live ->
                    attributeRepository.findBySchemaVersionIdAndIsActiveTrueOrderByDisplayOrderAsc(live.getId())
                            .forEach(a -> byCode.put(a.getAttributeCode(), a)));
        }
        attributeRepository.findByServiceCategoryIdAndCatalogServiceIsNullAndIsActiveTrueOrderByDisplayOrderAsc(categoryId)
                .stream()
                .filter(a -> a.getInputContext() != InputContext.VENDOR)
                .forEach(a -> byCode.putIfAbsent(a.getAttributeCode(), a));
        return byCode.values().stream()
                .sorted(Comparator.comparing(a -> a.getDisplayOrder() != null ? a.getDisplayOrder() : 0))
                .toList();
    }

    /**
     * The category's VENDOR attributes: details a vendor fills in once for a service they add.
     */
    @Transactional(readOnly = true)
    public List<ServiceAttributeDefinition> resolveVendorAttributes(Integer categoryId) {
        return attributeRepository.findByServiceCategoryIdAndCatalogServiceIsNullAndIsActiveTrueOrderByDisplayOrderAsc(categoryId)
                .stream()
                .filter(a -> a.getInputContext() == InputContext.VENDOR)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ServiceAttributeDefinitionResponse> getResolved(Integer categoryId, Integer serviceId) {
        return attributeMapper.toResponseList(resolveAttributes(categoryId, serviceId));
    }

    /**
     * Rejects definitions the validator could not enforce: select types without options, and
     * validation rules that are not a JSON object.
     */
    private void checkDefinition(ServiceAttributeDefinition attribute) {
        if (attribute.getDataType() == null) {
            throw new BusinessException("dataType is required");
        }
        if (attribute.getInputContext() == InputContext.VENDOR && attribute.getCatalogService() != null) {
            throw new BusinessException("VENDOR attributes belong to a category, not a service");
        }
        boolean isSelect = attribute.getDataType() == DataType.SINGLE_SELECT || attribute.getDataType() == DataType.MULTI_SELECT;
        if (isSelect && (attribute.getOptions() == null || "[]".equals(attribute.getOptions()))) {
            throw new BusinessException("Attribute '" + attribute.getAttributeCode() + "' is a "
                    + attribute.getDataType() + " and needs at least one option");
        }
        if (attribute.getValidationRules() != null && !attribute.getValidationRules().isBlank()) {
            try {
                if (!objectMapper.readTree(attribute.getValidationRules()).isObject()) {
                    throw new BusinessException("validationRules must be a JSON object");
                }
            } catch (JsonProcessingException e) {
                throw new BusinessException("validationRules is not valid JSON");
            }
        }
    }

    private String toOptionsJson(List<AttributeOption> options) {
        if (options == null || options.isEmpty()) {
            return null;
        }
        for (AttributeOption option : options) {
            if (option.getValue() == null || option.getValue().isBlank()) {
                throw new BusinessException("Every option needs a value");
            }
        }
        try {
            return objectMapper.writeValueAsString(options);
        } catch (JsonProcessingException e) {
            throw new BusinessException("Could not store options");
        }
    }

    private void ensureCodeIsFree(Integer categoryId, ServiceSchemaVersion version, String attributeCode) {
        boolean taken = version != null
                ? attributeRepository.existsBySchemaVersionIdAndAttributeCode(version.getId(), attributeCode)
                : attributeRepository.existsByServiceCategoryIdAndCatalogServiceIsNullAndAttributeCode(categoryId, attributeCode);
        if (taken) {
            throw new AlreadyExistsException("Attribute with code '" + attributeCode + "' already exists in this "
                    + (version != null ? "schema version" : "category"));
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
