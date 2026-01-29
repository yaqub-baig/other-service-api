package com.autoadda.apis.other.service;

import com.autoadda.apis.other.entity.FilterOption;
import com.autoadda.apis.other.entity.ServiceCategory;
import com.autoadda.apis.other.entity.ServiceFilterDefinition;
import com.autoadda.apis.other.exception.AlreadyExistsException;
import com.autoadda.apis.other.exception.ResourceNotFoundException;
import com.autoadda.apis.other.repository.FilterOptionRepository;
import com.autoadda.apis.other.repository.ServiceCategoryRepository;
import com.autoadda.apis.other.repository.ServiceFilterDefinitionRepository;
import com.autoadda.apis.other.request.FilterOptionRequest;
import com.autoadda.apis.other.request.ServiceFilterDefinitionRequest;
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

    /**
     * Create a new filter definition for a service category.
     */
    @Transactional
    public ServiceFilterDefinition create(ServiceFilterDefinitionRequest request) {
        // Validate service category exists
        ServiceCategory category = serviceCategoryRepository.findById(request.getServiceCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Service category not found with id: " + request.getServiceCategoryId()));

        // Check for duplicate filter code
        if (filterRepository.existsByServiceCategoryIdAndFilterCode(
                request.getServiceCategoryId(), request.getFilterCode())) {
            throw new AlreadyExistsException(
                    "Filter with code '" + request.getFilterCode() + "' already exists for this category");
        }

        ServiceFilterDefinition filter = ServiceFilterDefinition.builder()
                .serviceCategory(category)
                .filterCode(request.getFilterCode())
                .filterLabel(request.getFilterLabel())
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

        return filter;
    }

    /**
     * Get all active filters for a service category.
     */
    @Transactional(readOnly = true)
    public List<ServiceFilterDefinition> getByServiceCategory(Integer serviceCategoryId) {
        return filterRepository.findByServiceCategoryIdAndIsActiveTrueOrderByDisplayOrderAsc(serviceCategoryId);
    }

    /**
     * Get all filters for a service category (including inactive).
     */
    @Transactional(readOnly = true)
    public List<ServiceFilterDefinition> getAllByServiceCategory(Integer serviceCategoryId) {
        return filterRepository.findByServiceCategoryIdOrderByDisplayOrderAsc(serviceCategoryId);
    }

    /**
     * Get required filters for a service category.
     */
    @Transactional(readOnly = true)
    public List<ServiceFilterDefinition> getRequiredByServiceCategory(Integer serviceCategoryId) {
        return filterRepository.findByServiceCategoryIdAndIsRequiredTrueAndIsActiveTrueOrderByDisplayOrderAsc(serviceCategoryId);
    }

    /**
     * Get filters that depend on a specific filter.
     */
    @Transactional(readOnly = true)
    public List<ServiceFilterDefinition> getDependentFilters(Integer serviceCategoryId, String parentFilterCode) {
        return filterRepository.findByServiceCategoryIdAndDependsOnAndIsActiveTrueOrderByDisplayOrderAsc(
                serviceCategoryId, parentFilterCode);
    }

    /**
     * Get filter by ID.
     */
    @Transactional(readOnly = true)
    public ServiceFilterDefinition getById(Long id) {
        return filterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Filter definition not found with id: " + id));
    }

    /**
     * Get filter by code within a service category.
     */
    @Transactional(readOnly = true)
    public ServiceFilterDefinition getByCode(Integer serviceCategoryId, String filterCode) {
        return filterRepository.findByServiceCategoryIdAndFilterCode(serviceCategoryId, filterCode)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Filter not found with code: " + filterCode + " in category: " + serviceCategoryId));
    }

    /**
     * Update a filter definition.
     */
    @Transactional
    public ServiceFilterDefinition update(Long id, ServiceFilterDefinitionRequest request) {
        ServiceFilterDefinition filter = getById(id);

        // If changing filter code, check for duplicates
        if (request.getFilterCode() != null &&
                !request.getFilterCode().equals(filter.getFilterCode())) {
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

        return filterRepository.save(filter);
    }

    /**
     * Add an option to a filter definition.
     */
    @Transactional
    public ServiceFilterDefinition addOption(Long filterId, FilterOptionRequest request) {
        ServiceFilterDefinition filter = getById(filterId);

        // Check for duplicate option value
        if (optionRepository.existsByFilterDefinitionIdAndOptionValue(filterId, request.getOptionValue())) {
            throw new AlreadyExistsException(
                    "Option with value '" + request.getOptionValue() + "' already exists for this filter");
        }

        FilterOption option = createOptionFromRequest(request);
        filter.addOption(option);

        return filterRepository.save(filter);
    }

    /**
     * Update an option.
     */
    @Transactional
    public FilterOption updateOption(Long optionId, FilterOptionRequest request) {
        FilterOption option = optionRepository.findById(optionId)
                .orElseThrow(() -> new ResourceNotFoundException("Filter option not found with id: " + optionId));

        if (request.getOptionValue() != null) {
            option.setOptionValue(request.getOptionValue());
        }
        if (request.getOptionLabel() != null) {
            option.setOptionLabel(request.getOptionLabel());
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

        return optionRepository.save(option);
    }

    /**
     * Delete an option.
     */
    @Transactional
    public void deleteOption(Long optionId) {
        if (!optionRepository.existsById(optionId)) {
            throw new ResourceNotFoundException("Filter option not found with id: " + optionId);
        }
        optionRepository.deleteById(optionId);
        log.info("Deleted filter option with id: {}", optionId);
    }

    /**
     * Get options for a filter (optionally filtered by parent value for dependent filters).
     */
    @Transactional(readOnly = true)
    public List<FilterOption> getOptions(Long filterId, String parentValue) {
        if (parentValue != null && !parentValue.isEmpty()) {
            return optionRepository.findByFilterDefinitionIdAndParentValueAndIsActiveTrueOrderByDisplayOrderAsc(
                    filterId, parentValue);
        }
        return optionRepository.findByFilterDefinitionIdAndIsActiveTrueOrderByDisplayOrderAsc(filterId);
    }

    /**
     * Soft delete a filter definition.
     */
    @Transactional
    public void delete(Long id) {
        ServiceFilterDefinition filter = getById(id);
        filter.setIsActive(false);
        filterRepository.save(filter);
        log.info("Soft deleted filter definition with id: {}", id);
    }

    /**
     * Hard delete a filter definition.
     */
    @Transactional
    public void hardDelete(Long id) {
        if (!filterRepository.existsById(id)) {
            throw new ResourceNotFoundException("Filter definition not found with id: " + id);
        }
        filterRepository.deleteById(id);
        log.info("Hard deleted filter definition with id: {}", id);
    }

    /**
     * Reactivate a soft-deleted filter definition.
     */
    @Transactional
    public ServiceFilterDefinition reactivate(Long id) {
        ServiceFilterDefinition filter = getById(id);
        filter.setIsActive(true);
        return filterRepository.save(filter);
    }

    /**
     * Get all active filters.
     */
    @Transactional(readOnly = true)
    public List<ServiceFilterDefinition> getAllActive() {
        return filterRepository.findByIsActiveTrueOrderByServiceCategoryIdAscDisplayOrderAsc();
    }

    // Helper methods

    private FilterOption createOptionFromRequest(FilterOptionRequest request) {
        return FilterOption.builder()
                .optionValue(request.getOptionValue())
                .optionLabel(request.getOptionLabel())
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
