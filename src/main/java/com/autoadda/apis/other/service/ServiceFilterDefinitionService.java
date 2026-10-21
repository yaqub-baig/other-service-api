package com.autoadda.apis.other.service;

import com.autoadda.apis.other.util.AttributeCodes;
import com.autoadda.apis.other.repository.ServiceAttributeDefinitionRepository;
import com.autoadda.apis.other.exception.BusinessException;
import com.autoadda.apis.other.entity.ServiceAttributeDefinition;
import com.autoadda.apis.other.entity.FilterOption;
import com.autoadda.apis.other.entity.ServiceCategory;
import com.autoadda.apis.other.entity.ServiceFilterDefinition;
import com.autoadda.apis.other.exception.AlreadyExistsException;
import com.autoadda.apis.other.exception.ResourceNotFoundException;
import com.autoadda.apis.other.mapper.ServiceFilterDefinitionMapper;
import com.autoadda.apis.other.repository.FilterOptionRepository;
import com.autoadda.apis.other.repository.ServiceCategoryRepository;
import com.autoadda.apis.other.repository.ServiceFilterDefinitionRepository;
import com.autoadda.apis.other.request.FilterOptionRequest;
import com.autoadda.apis.other.request.ServiceFilterDefinitionRequest;
import com.autoadda.apis.other.response.FilterOptionResponse;
import com.autoadda.apis.other.response.ServiceFilterDefinitionResponse;
import com.autoadda.apis.other.util.DataSourceType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ServiceFilterDefinitionService {

    private final ServiceFilterDefinitionRepository filterRepository;
    private final FilterOptionRepository optionRepository;
    private final ServiceCategoryRepository serviceCategoryRepository;
    private final ServiceFilterDefinitionMapper filterMapper;
    private final ServiceAttributeDefinitionRepository attributeRepository;

    /**
     * Create a new filter definition for a service category.
     */
    @Transactional
    public ServiceFilterDefinitionResponse create(ServiceFilterDefinitionRequest request) {
        // Validate service category exists
        ServiceCategory category = serviceCategoryRepository.findById(request.getServiceCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Service category not found with id: " + request.getServiceCategoryId()));

        ServiceAttributeDefinition attribute = linkedAttribute(request.getAttributeDefinitionId(), category.getId());
        String filterCode = attribute != null ? attribute.getAttributeCode() : request.getFilterCode();
        if (attribute != null && request.getFilterCode() != null && !request.getFilterCode().equals(filterCode)) {
            throw new BusinessException("filterCode must match the linked attribute's code '" + filterCode + "'");
        }
        AttributeCodes.requireValid(filterCode);

        // Check for duplicate filter code
        if (filterRepository.existsByServiceCategoryIdAndFilterCode(request.getServiceCategoryId(), filterCode)) {
            throw new AlreadyExistsException(
                    "Filter with code '" + filterCode + "' already exists for this category");
        }

        ServiceFilterDefinition filter = ServiceFilterDefinition.builder()
                .serviceCategory(category)
                .attributeDefinition(attribute)
                .filterCode(filterCode)
                .filterLabel(request.getFilterLabel())
                .filterLabelAr(request.getFilterLabelAr())
                .filterType(request.getFilterType())
                .dataSource(request.getDataSource() != null ? request.getDataSource() : DataSourceType.STATIC)
                .dataSourceConfig(request.getDataSourceConfig())
                .dependsOn(request.getDependsOn())
                .displayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0)
                .isRequired(request.getIsRequired() != null ? request.getIsRequired() : false)
                .allowMultiple(request.getAllowMultiple() != null ? request.getAllowMultiple() : false)
                .placeholder(request.getPlaceholder())
                .helpText(request.getHelpText())
                .minValue(request.getMinValue())
                .maxValue(request.getMaxValue())
                .stepValue(request.getStepValue())
                .isActive(true)
                .build();

        // Save the filter first
        filter = filterRepository.save(filter);

        // Add options if provided
        if (request.getOptions() != null && !request.getOptions().isEmpty()) {
            for (FilterOptionRequest optionRequest : request.getOptions()) {
                FilterOption option = createOptionFromRequest(optionRequest);
                filter.addOption(option);
            }
            filter = filterRepository.save(filter);
        }

        return filterMapper.toResponse(filter);
    }

    /**
     * Get all active filters for a service category.
     */
    @Transactional(readOnly = true)
    public List<ServiceFilterDefinitionResponse> getByServiceCategory(Integer serviceCategoryId) {
        List<ServiceFilterDefinition> filters = filterRepository.findByServiceCategoryIdAndIsActiveTrueOrderByDisplayOrderAsc(serviceCategoryId);
        return filterMapper.toResponseList(filters);
    }

    /**
     * Get all filters for a service category (including inactive).
     */
    @Transactional(readOnly = true)
    public List<ServiceFilterDefinitionResponse> getAllByServiceCategory(Integer serviceCategoryId) {
        List<ServiceFilterDefinition> filters = filterRepository.findByServiceCategoryIdOrderByDisplayOrderAsc(serviceCategoryId);
        return filterMapper.toResponseList(filters);
    }

    /**
     * Get required filters for a service category.
     */
    @Transactional(readOnly = true)
    public List<ServiceFilterDefinitionResponse> getRequiredByServiceCategory(Integer serviceCategoryId) {
        List<ServiceFilterDefinition> filters = filterRepository.findByServiceCategoryIdAndIsRequiredTrueAndIsActiveTrueOrderByDisplayOrderAsc(serviceCategoryId);
        return filterMapper.toResponseList(filters);
    }

    /**
     * Get filters that depend on a specific filter.
     */
    @Transactional(readOnly = true)
    public List<ServiceFilterDefinitionResponse> getDependentFilters(Integer serviceCategoryId, String parentFilterCode) {
        List<ServiceFilterDefinition> filters = filterRepository.findByServiceCategoryIdAndDependsOnAndIsActiveTrueOrderByDisplayOrderAsc(
                serviceCategoryId, parentFilterCode);
        return filterMapper.toResponseList(filters);
    }

    /**
     * Get filter by ID.
     */
    @Transactional(readOnly = true)
    public ServiceFilterDefinitionResponse getById(Long id) {
        ServiceFilterDefinition filter = filterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Filter definition not found with id: " + id));
        return filterMapper.toResponse(filter);
    }

    /**
     * Get filter by code within a service category.
     */
    @Transactional(readOnly = true)
    public ServiceFilterDefinitionResponse getByCode(Integer serviceCategoryId, String filterCode) {
        ServiceFilterDefinition filter = filterRepository.findByServiceCategoryIdAndFilterCode(serviceCategoryId, filterCode)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Filter not found with code: " + filterCode + " in category: " + serviceCategoryId));
        return filterMapper.toResponse(filter);
    }

    /**
     * Update a filter definition.
     */
    @Transactional
    public ServiceFilterDefinitionResponse update(Long id, ServiceFilterDefinitionRequest request) {
        ServiceFilterDefinition filter = findEntityById(id);

        if (request.getAttributeDefinitionId() != null) {
            ServiceAttributeDefinition attribute = linkedAttribute(request.getAttributeDefinitionId(),
                    filter.getServiceCategory().getId());
            filter.setAttributeDefinition(attribute);
            request.setFilterCode(attribute.getAttributeCode());
        } else if (request.getFilterCode() != null && filter.getAttributeDefinition() != null
                && !request.getFilterCode().equals(filter.getAttributeDefinition().getAttributeCode())) {
            throw new BusinessException("filterCode must match the linked attribute's code");
        }

        // If changing filter code, check for duplicates
        if (request.getFilterCode() != null &&
                !request.getFilterCode().equals(filter.getFilterCode())) {
            AttributeCodes.requireValid(request.getFilterCode());
            if (filterRepository.existsByServiceCategoryIdAndFilterCode(
                    filter.getServiceCategory().getId(), request.getFilterCode())) {
                throw new AlreadyExistsException(
                        "Filter with code '" + request.getFilterCode() + "' already exists for this category");
            }
            filter.setFilterCode(request.getFilterCode());
        }

        if (request.getFilterLabel() != null) {
            filter.setFilterLabel(request.getFilterLabel());
        }
        if (request.getFilterLabelAr() != null) {
            filter.setFilterLabelAr(request.getFilterLabelAr());
        }
        if (request.getFilterType() != null) {
            filter.setFilterType(request.getFilterType());
        }
        if (request.getDataSource() != null) {
            filter.setDataSource(request.getDataSource());
        }
        if (request.getDataSourceConfig() != null) {
            filter.setDataSourceConfig(request.getDataSourceConfig());
        }
        if (request.getDependsOn() != null) {
            filter.setDependsOn(request.getDependsOn());
        }
        if (request.getDisplayOrder() != null) {
            filter.setDisplayOrder(request.getDisplayOrder());
        }
        if (request.getIsRequired() != null) {
            filter.setIsRequired(request.getIsRequired());
        }
        if (request.getAllowMultiple() != null) {
            filter.setAllowMultiple(request.getAllowMultiple());
        }
        if (request.getPlaceholder() != null) {
            filter.setPlaceholder(request.getPlaceholder());
        }
        if (request.getHelpText() != null) {
            filter.setHelpText(request.getHelpText());
        }
        if (request.getMinValue() != null) {
            filter.setMinValue(request.getMinValue());
        }
        if (request.getMaxValue() != null) {
            filter.setMaxValue(request.getMaxValue());
        }
        if (request.getStepValue() != null) {
            filter.setStepValue(request.getStepValue());
        }

        // Update options if provided
        if (request.getOptions() != null) {
            updateFilterOptions(filter, request.getOptions());
        }

        ServiceFilterDefinition saved = filterRepository.save(filter);
        return filterMapper.toResponse(saved);
    }

    /**
     * Add an option to a filter definition.
     */
    @Transactional
    public ServiceFilterDefinitionResponse addOption(Long filterId, FilterOptionRequest request) {
        ServiceFilterDefinition filter = findEntityById(filterId);

        // Check for duplicate option value
        if (optionRepository.existsByFilterDefinitionIdAndOptionValue(filterId, request.getOptionValue())) {
            throw new AlreadyExistsException(
                    "Option with value '" + request.getOptionValue() + "' already exists for this filter");
        }

        FilterOption option = createOptionFromRequest(request);
        filter.addOption(option);

        ServiceFilterDefinition saved = filterRepository.save(filter);
        return filterMapper.toResponse(saved);
    }

    /**
     * Update an option.
     */
    @Transactional
    public FilterOptionResponse updateOption(Long optionId, FilterOptionRequest request) {
        FilterOption option = optionRepository.findById(optionId)
                .orElseThrow(() -> new ResourceNotFoundException("Filter option not found with id: " + optionId));

        if (request.getOptionValue() != null) {
            option.setOptionValue(request.getOptionValue());
        }
        if (request.getOptionLabel() != null) {
            option.setOptionLabel(request.getOptionLabel());
        }
        if (request.getOptionLabelAr() != null) {
            option.setOptionLabelAr(request.getOptionLabelAr());
        }
        if (request.getDisplayOrder() != null) {
            option.setDisplayOrder(request.getDisplayOrder());
        }
        if (request.getIsDefault() != null) {
            option.setIsDefault(request.getIsDefault());
        }
        if (request.getIsActive() != null) {
            option.setIsActive(request.getIsActive());
        }
        if (request.getIconUrl() != null) {
            option.setIconUrl(request.getIconUrl());
        }
        if (request.getParentValue() != null) {
            option.setParentValue(request.getParentValue());
        }
        if (request.getMetadata() != null) {
            option.setMetadata(request.getMetadata());
        }

        FilterOption saved = optionRepository.save(option);
        return filterMapper.toOptionResponse(saved);
    }

    /**
     * Deactivate an option (kept so offerings and saved searches using the value stay readable).
     */
    @Transactional
    public void deleteOption(Long optionId) {
        FilterOption option = optionRepository.findById(optionId)
                .orElseThrow(() -> new ResourceNotFoundException("Filter option not found with id: " + optionId));
        option.setIsActive(false);
        optionRepository.save(option);
        log.info("Deactivated filter option with id: {}", optionId);
    }

    private ServiceAttributeDefinition linkedAttribute(Long attributeId, Integer categoryId) {
        if (attributeId == null) {
            return null;
        }
        ServiceAttributeDefinition attribute = attributeRepository.findById(attributeId)
                .orElseThrow(() -> new ResourceNotFoundException("Attribute definition not found with id: " + attributeId));
        if (!attribute.getServiceCategory().getId().equals(categoryId)) {
            throw new BusinessException("Attribute " + attributeId + " does not belong to category " + categoryId);
        }
        return attribute;
    }

    /**
     * Get options for a filter (optionally filtered by parent value for dependent filters).
     */
    @Transactional(readOnly = true)
    public List<FilterOptionResponse> getOptions(Long filterId, String parentValue) {
        List<FilterOption> options;
        if (parentValue != null && !parentValue.isEmpty()) {
            options = optionRepository.findByFilterDefinitionIdAndParentValueAndIsActiveTrueOrderByDisplayOrderAsc(
                    filterId, parentValue);
        } else {
            options = optionRepository.findByFilterDefinitionIdAndIsActiveTrueOrderByDisplayOrderAsc(filterId);
        }
        return filterMapper.toOptionResponseList(options);
    }

    /**
     * Soft delete a filter definition.
     */
    @Transactional
    public void delete(Long id) {
        ServiceFilterDefinition filter = findEntityById(id);
        filter.setIsActive(false);
        filterRepository.save(filter);
        log.info("Soft deleted filter definition with id: {}", id);
    }

    /**
     * Reactivate a soft-deleted filter definition.
     */
    @Transactional
    public ServiceFilterDefinitionResponse reactivate(Long id) {
        ServiceFilterDefinition filter = findEntityById(id);
        filter.setIsActive(true);
        ServiceFilterDefinition saved = filterRepository.save(filter);
        return filterMapper.toResponse(saved);
    }

    /**
     * Get all active filters.
     */
    @Transactional(readOnly = true)
    public List<ServiceFilterDefinitionResponse> getAllActive() {
        List<ServiceFilterDefinition> filters = filterRepository.findByIsActiveTrueOrderByServiceCategoryIdAscDisplayOrderAsc();
        return filterMapper.toResponseList(filters);
    }

    /**
     * Internal method to get entity by ID (for update operations).
     */
    private ServiceFilterDefinition findEntityById(Long id) {
        return filterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Filter definition not found with id: " + id));
    }

    // Helper methods

    private FilterOption createOptionFromRequest(FilterOptionRequest request) {
        return FilterOption.builder()
                .optionValue(request.getOptionValue())
                .optionLabel(request.getOptionLabel())
                .optionLabelAr(request.getOptionLabelAr())
                .displayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0)
                .isDefault(request.getIsDefault() != null ? request.getIsDefault() : false)
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .iconUrl(request.getIconUrl())
                .parentValue(request.getParentValue())
                .metadata(request.getMetadata())
                .build();
    }

    private void updateFilterOptions(ServiceFilterDefinition filter, List<FilterOptionRequest> optionRequests) {
        // Clear existing options and add new ones
        filter.getOptions().clear();

        for (FilterOptionRequest optionRequest : optionRequests) {
            FilterOption option = createOptionFromRequest(optionRequest);
            filter.addOption(option);
        }
    }
}
