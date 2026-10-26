package com.autoadda.apis.other.service;

import com.autoadda.apis.other.entity.CatalogService;
import com.autoadda.apis.other.entity.QuotationFieldDefinition;
import com.autoadda.apis.other.entity.QuotationScopeDefinition;
import com.autoadda.apis.other.entity.ServiceCategory;
import com.autoadda.apis.other.exception.AlreadyExistsException;
import com.autoadda.apis.other.exception.BusinessException;
import com.autoadda.apis.other.exception.ResourceNotFoundException;
import com.autoadda.apis.other.mapper.QuotationFieldDefinitionMapper;
import com.autoadda.apis.other.mapper.QuotationScopeDefinitionMapper;
import com.autoadda.apis.other.repository.QuotationFieldDefinitionRepository;
import com.autoadda.apis.other.repository.QuotationScopeDefinitionRepository;
import com.autoadda.apis.other.repository.ServiceCategoryRepository;
import com.autoadda.apis.other.request.QuotationFieldDefinitionRequest;
import com.autoadda.apis.other.request.QuotationScopeDefinitionRequest;
import com.autoadda.apis.other.response.QuotationFieldDefinitionResponse;
import com.autoadda.apis.other.response.QuotationScopeDefinitionResponse;
import com.autoadda.apis.other.util.QuotationType;
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
import java.util.Optional;

/**
 * Service for admin-level operations on quotation configurations.
 * Handles management of scope definitions and field definitions.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class QuotationAdminService {

    private final QuotationScopeDefinitionRepository scopeRepository;
    private final QuotationFieldDefinitionRepository fieldRepository;
    private final ServiceCategoryRepository categoryRepository;
    private final ObjectMapper objectMapper;
    private final QuotationScopeDefinitionMapper scopeMapper; // Inject mapper
    private final QuotationFieldDefinitionMapper fieldMapper; // Inject mapper
    private final ServiceCatalogService serviceCatalogService;

    // ==================== Scope Definition Operations ====================

    /**
     * Create or update the scope definition for a service category, or for one catalog service
     * when serviceId is set (that scope then overrides the category default).
     */
    @Transactional
    public QuotationScopeDefinitionResponse saveScopeDefinition(QuotationScopeDefinitionRequest request) {
        CatalogService catalogService = request.getServiceId() != null
                ? serviceCatalogService.getForCategory(request.getServiceId(), request.getServiceCategoryId())
                : null;
        Integer categoryId = catalogService != null
                ? catalogService.getServiceCategory().getId()
                : request.getServiceCategoryId();
        if (categoryId == null) {
            throw new BusinessException("serviceCategoryId or serviceId is required");
        }
        ServiceCategory category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Service category not found with id: " + categoryId));

        QuotationScopeDefinition scope = (catalogService != null
                ? scopeRepository.findByCatalogServiceId(catalogService.getId())
                : scopeRepository.findByServiceCategoryIdAndCatalogServiceIsNull(categoryId))
                .orElse(QuotationScopeDefinition.builder()
                        .serviceCategory(category)
                        .catalogService(catalogService)
                        .build());

        scope.setIsQuotationEnabled(request.getIsQuotationEnabled() != null ? request.getIsQuotationEnabled() : false);
        scope.setQuotationType(request.getQuotationType() != null ? request.getQuotationType() : QuotationType.OPTIONAL);
        scope.setAutoExpiryHours(request.getAutoExpiryHours() != null ? request.getAutoExpiryHours() : 48);
        scope.setAllowMultiVendorBroadcast(request.getAllowMultiVendorBroadcast() != null ? request.getAllowMultiVendorBroadcast() : true);
        scope.setMaxBroadcastVendors(request.getMaxBroadcastVendors());
        scope.setMinQuotesRequired(request.getMinQuotesRequired() != null ? request.getMinQuotesRequired() : 1);
        scope.setIsActive(true);

        QuotationScopeDefinition saved = scopeRepository.save(scope);
        log.info("Saved quotation scope definition for category: {}", category.getName());
        return scopeMapper.toResponse(saved);
    }

    /**
     * The scope that applies to a request: the service's own scope when it has one, otherwise the
     * category default. Empty when neither is configured.
     */
    @Transactional(readOnly = true)
    public Optional<QuotationScopeDefinition> resolveScope(Integer categoryId, Integer serviceId) {
        if (serviceId != null) {
            Optional<QuotationScopeDefinition> serviceScope = scopeRepository.findByCatalogServiceIdAndIsActiveTrue(serviceId);
            if (serviceScope.isPresent()) {
                return serviceScope;
            }
        }
        return scopeRepository.findByServiceCategoryIdAndCatalogServiceIsNullAndIsActiveTrue(categoryId);
    }

    /**
     * The form fields for a request: the service's own fields, plus category defaults whose code
     * the service does not redefine.
     */
    @Transactional(readOnly = true)
    public List<QuotationFieldDefinition> resolveFields(Integer categoryId, Integer serviceId) {
        Map<String, QuotationFieldDefinition> byCode = new LinkedHashMap<>();
        if (serviceId != null) {
            fieldRepository.findByCatalogServiceIdAndIsActiveTrueOrderByDisplayOrderAsc(serviceId)
                    .forEach(f -> byCode.put(f.getFieldCode(), f));
        }
        fieldRepository.findByServiceCategoryIdAndCatalogServiceIsNullAndIsActiveTrueOrderByDisplayOrderAsc(categoryId)
                .forEach(f -> byCode.putIfAbsent(f.getFieldCode(), f));
        return byCode.values().stream()
                .sorted(Comparator.comparing(f -> f.getDisplayOrder() != null ? f.getDisplayOrder() : 0))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<QuotationFieldDefinitionResponse> getFieldsForService(Integer categoryId, Integer serviceId) {
        return fieldMapper.toResponseList(resolveFields(categoryId, serviceId));
    }

    /**
     * Get scope definition for a service category.
     */
    @Transactional(readOnly = true)
    public QuotationScopeDefinitionResponse getScopeDefinitionForCategory(Integer categoryId) {
        return scopeRepository.findByServiceCategoryIdAndCatalogServiceIsNullAndIsActiveTrue(categoryId)
                .map(scopeMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Quotation scope not configured for category: " + categoryId));
    }

    @Transactional(readOnly = true)
    public QuotationScopeDefinition getScopeDefinitionForCategory1(Integer categoryId) {
        return scopeRepository.findByServiceCategoryIdAndCatalogServiceIsNullAndIsActiveTrue(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Quotation scope not configured for category: " + categoryId));
    }

    /**
     * Get scope definition by ID.
     */
    @Transactional(readOnly = true)
    public QuotationScopeDefinitionResponse getScopeDefinitionById(Integer id) {
        return scopeRepository.findById(id)
                .map(scopeMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Quotation scope definition not found with id: " + id));
    }
    /**
     * Get scope definition by ID.
     */
    @Transactional(readOnly = true)
    public QuotationScopeDefinition getScopeDefinitionById1(Integer id) {
        return scopeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Quotation scope definition not found with id: " + id));
    }
    /**
     * Check if quotation is enabled for a category.
     */
    @Transactional(readOnly = true)
    public boolean isQuotationEnabled(Integer categoryId) {
        return isQuotationEnabled(categoryId, null);
    }

    @Transactional(readOnly = true)
    public boolean isQuotationEnabled(Integer categoryId, Integer serviceId) {
        return resolveScope(categoryId, serviceId)
                .map(QuotationScopeDefinition::getIsQuotationEnabled)
                .orElse(false);
    }

    /**
     * Get all categories with quotation enabled.
     */
    @Transactional(readOnly = true)
    public List<QuotationScopeDefinitionResponse> getQuotationEnabledCategories() {
        return scopeMapper.toResponseList(scopeRepository.findByIsQuotationEnabledTrueAndIsActiveTrue());
    }

    /**
     * Disable quotation for a category.
     */
    @Transactional
    public void disableQuotationForCategory(Integer categoryId) {
        QuotationScopeDefinition scope = getScopeDefinitionForCategory1(categoryId);
        scope.setIsQuotationEnabled(false);
        scopeRepository.save(scope);
        log.info("Disabled quotation for category: {}", categoryId);
    }

    /**
     * Delete scope definition.
     */
    @Transactional
    public void deleteScopeDefinition(Integer id) {
        QuotationScopeDefinition scope = getScopeDefinitionById1(id);
        scope.setIsActive(false);
        scopeRepository.save(scope);
        log.info("Soft deleted quotation scope definition: {}", id);
    }

    // ==================== Field Definition Operations ====================

    /**
     * Create a new field definition for a service category.
     */
    @Transactional
    public QuotationFieldDefinitionResponse createFieldDefinition(QuotationFieldDefinitionRequest request) {
        CatalogService catalogService = request.getServiceId() != null
                ? serviceCatalogService.getForCategory(request.getServiceId(), request.getServiceCategoryId())
                : null;
        Integer categoryId = catalogService != null
                ? catalogService.getServiceCategory().getId()
                : request.getServiceCategoryId();
        if (categoryId == null) {
            throw new BusinessException("serviceCategoryId or serviceId is required");
        }
        ServiceCategory category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Service category not found with id: " + categoryId));

        ensureFieldCodeIsFree(categoryId, catalogService, request.getFieldCode());

        // Get depends on field if specified
        QuotationFieldDefinition dependsOn = null;
        if (request.getDependsOnFieldId() != null) {
            dependsOn = fieldRepository.findById(request.getDependsOnFieldId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Depends on field not found with id: " + request.getDependsOnFieldId()));
        }

        QuotationFieldDefinition field = QuotationFieldDefinition.builder()
                .serviceCategory(category)
                .catalogService(catalogService)
                .fieldCode(request.getFieldCode())
                .label(request.getLabel())
                .labelAr(request.getLabelAr())
                .placeholderAr(request.getPlaceholderAr())
                .helpTextAr(request.getHelpTextAr())
                .fieldType(request.getFieldType())
                .isRequired(request.getIsRequired() != null ? request.getIsRequired() : true)
                .displayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0)
                .placeholder(request.getPlaceholder())
                .helpText(request.getHelpText())
                .defaultValue(request.getDefaultValue())
                .options(convertToJson(request.getOptions()))
                .validationRules(convertToJson(request.getValidationRules()))
                .dependsOn(dependsOn)
                .dependsOnCondition(convertToJson(request.getDependsOnCondition()))
                .isActive(true)
                .build();

        QuotationFieldDefinition saved = fieldRepository.save(field);
        log.info("Created quotation field '{}' for category: {}", request.getFieldCode(), category.getName());
        return fieldMapper.toResponse(saved);
    }

    /**
     * Update a field definition.
     */
    @Transactional
    public QuotationFieldDefinitionResponse updateFieldDefinition(Integer fieldId, QuotationFieldDefinitionRequest request) {
        QuotationFieldDefinition field = fieldRepository.findById(fieldId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Quotation field not found with id: " + fieldId));

        // Check for duplicate field code if changing
        if (request.getFieldCode() != null && !request.getFieldCode().equals(field.getFieldCode())) {
            ensureFieldCodeIsFree(field.getServiceCategory().getId(), field.getCatalogService(), request.getFieldCode());
            field.setFieldCode(request.getFieldCode());
        }

        if (request.getLabel() != null) {
            field.setLabel(request.getLabel());
        }
        if (request.getLabelAr() != null) {
            field.setLabelAr(request.getLabelAr());
        }
        if (request.getPlaceholderAr() != null) {
            field.setPlaceholderAr(request.getPlaceholderAr());
        }
        if (request.getHelpTextAr() != null) {
            field.setHelpTextAr(request.getHelpTextAr());
        }
        if (request.getFieldType() != null) {
            field.setFieldType(request.getFieldType());
        }
        if (request.getIsRequired() != null) {
            field.setIsRequired(request.getIsRequired());
        }
        if (request.getDisplayOrder() != null) {
            field.setDisplayOrder(request.getDisplayOrder());
        }
        if (request.getPlaceholder() != null) {
            field.setPlaceholder(request.getPlaceholder());
        }
        if (request.getHelpText() != null) {
            field.setHelpText(request.getHelpText());
        }
        if (request.getDefaultValue() != null) {
            field.setDefaultValue(request.getDefaultValue());
        }
        if (request.getOptions() != null) {
            field.setOptions(convertToJson(request.getOptions()));
        }
        if (request.getValidationRules() != null) {
            field.setValidationRules(convertToJson(request.getValidationRules()));
        }
        if (request.getDependsOnFieldId() != null) {
            QuotationFieldDefinition dependsOn = fieldRepository.findById(request.getDependsOnFieldId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Depends on field not found with id: " + request.getDependsOnFieldId()));
            field.setDependsOn(dependsOn);
        }
        if (request.getDependsOnCondition() != null) {
            field.setDependsOnCondition(convertToJson(request.getDependsOnCondition()));
        }

        QuotationFieldDefinition saved = fieldRepository.save(field);
        log.info("Updated quotation field: {}", fieldId);
        return fieldMapper.toResponse(saved);
    }

    /**
     * Get all fields for a service category.
     */
    @Transactional(readOnly = true)
    public List<QuotationFieldDefinitionResponse> getFieldsForCategory(Integer categoryId) {
        return fieldMapper.toResponseList(fieldRepository.findByServiceCategoryIdAndCatalogServiceIsNullAndIsActiveTrueOrderByDisplayOrderAsc(categoryId));
    }

    /**
     * Get required fields for a service category.
     */
    @Transactional(readOnly = true)
    public List<QuotationFieldDefinitionResponse> getRequiredFieldsForCategory(Integer categoryId) {
        return fieldMapper.toResponseList(fieldRepository.findByServiceCategoryIdAndCatalogServiceIsNullAndIsRequiredTrueAndIsActiveTrueOrderByDisplayOrderAsc(categoryId));
    }

    /**
     * Get field by ID.
     */
    @Transactional(readOnly = true)
    public QuotationFieldDefinitionResponse getFieldById(Integer fieldId) {
        return fieldRepository.findById(fieldId)
                .map(fieldMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Quotation field not found with id: " + fieldId));
    }


    public QuotationFieldDefinition getFieldById1(Integer fieldId) {
        return fieldRepository.findById(fieldId)

                .orElseThrow(() -> new ResourceNotFoundException(
                        "Quotation field not found with id: " + fieldId));
    }


    /**
     * Delete a field definition (soft delete).
     */
    @Transactional
    public void deleteFieldDefinition(Integer fieldId) {
        QuotationFieldDefinition field = getFieldById1(fieldId);

        // Check if any fields depend on this one
        List<QuotationFieldDefinition> dependentFields = fieldRepository.findByDependsOnIdAndIsActiveTrue(fieldId);
        if (!dependentFields.isEmpty()) {
            throw new IllegalStateException(
                    "Cannot delete field. " + dependentFields.size() + " other fields depend on it.");
        }

        field.setIsActive(false);
        fieldRepository.save(field);
        log.info("Soft deleted quotation field: {}", fieldId);
    }

    /**
     * Reorder fields within a category.
     */
    @Transactional
    public void reorderFields(Integer categoryId, List<Integer> fieldIds) {
        for (int i = 0; i < fieldIds.size(); i++) {
            QuotationFieldDefinition field = getFieldById1(fieldIds.get(i));
            if (!field.getServiceCategory().getId().equals(categoryId)) {
                throw new IllegalArgumentException("Field " + fieldIds.get(i) + " does not belong to category " + categoryId);
            }
            field.setDisplayOrder(i);
            fieldRepository.save(field);
        }
        log.info("Reordered {} fields for category: {}", fieldIds.size(), categoryId);
    }

    // ==================== Helper Methods ====================

    private void ensureFieldCodeIsFree(Integer categoryId, CatalogService catalogService, String fieldCode) {
        boolean taken = catalogService != null
                ? fieldRepository.existsByCatalogServiceIdAndFieldCode(catalogService.getId(), fieldCode)
                : fieldRepository.existsByServiceCategoryIdAndCatalogServiceIsNullAndFieldCode(categoryId, fieldCode);
        if (taken) {
            throw new AlreadyExistsException("Field with code '" + fieldCode + "' already exists for this "
                    + (catalogService != null ? "service" : "category"));
        }
    }

    private String convertToJson(Object obj) {
        if (obj == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to convert to JSON", e);
        }
    }
}
