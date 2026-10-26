package com.autoadda.apis.other.service;

import com.autoadda.apis.other.util.QuotationMode;
import com.autoadda.apis.other.entity.Vendor;
import com.autoadda.apis.other.entity.CatalogService;
import com.autoadda.apis.other.entity.ServiceCategory;
import com.autoadda.apis.other.exception.AlreadyExistsException;
import com.autoadda.apis.other.exception.BusinessException;
import com.autoadda.apis.other.exception.ResourceNotFoundException;
import com.autoadda.apis.other.mapper.CatalogServiceMapper;
import com.autoadda.apis.other.repository.CatalogServiceRepository;
import com.autoadda.apis.other.repository.ServiceCategoryRepository;
import com.autoadda.apis.other.request.CatalogServiceRequest;
import com.autoadda.apis.other.response.CatalogServiceResponse;
import com.autoadda.apis.other.util.FulfillmentMode;
import com.autoadda.apis.other.util.IsoCodes;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

/**
 * Admin and read operations for catalog services (the level between category and offering).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ServiceCatalogService {

    private final CatalogServiceRepository catalogServiceRepository;
    private final ServiceCategoryRepository serviceCategoryRepository;
    private final CatalogServiceMapper catalogServiceMapper;
    private final QuotationModeResolver quotationModeResolver;

    @Transactional
    public CatalogServiceResponse create(CatalogServiceRequest request) {
        if (request.getServiceCategoryId() == null || isBlank(request.getCode()) || isBlank(request.getName())) {
            throw new BusinessException("serviceCategoryId, code and name are required");
        }
        String code = normalizeCode(request.getCode());
        if (catalogServiceRepository.existsByCode(code)) {
            throw new AlreadyExistsException("Service with code '" + code + "' already exists");
        }
        CatalogService service = CatalogService.builder()
                .serviceCategory(findCategory(request.getServiceCategoryId()))
                .code(code)
                .name(request.getName())
                .description(request.getDescription())
                .nameAr(request.getNameAr())
                .descriptionAr(request.getDescriptionAr())
                .quotationMode(request.getQuotationMode())
                .fulfillmentMode(request.getFulfillmentMode() != null ? request.getFulfillmentMode() : FulfillmentMode.WORKSHOP)
                .estimatedDurationMinutes(validDuration(request.getEstimatedDurationMinutes()))
                .displayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0)
                .isActive(true)
                .build();
        CatalogService saved = catalogServiceRepository.save(service);
        log.info("Created catalog service {} ({})", saved.getCode(), saved.getId());
        return toResponse(saved);
    }

    @Transactional
    public CatalogServiceResponse update(Integer id, CatalogServiceRequest request) {
        CatalogService service = getEntity(id);
        if (request.getCode() != null) {
            String code = normalizeCode(request.getCode());
            if (!code.equals(service.getCode()) && catalogServiceRepository.existsByCode(code)) {
                throw new AlreadyExistsException("Service with code '" + code + "' already exists");
            }
            service.setCode(code);
        }
        if (request.getServiceCategoryId() != null) {
            service.setServiceCategory(findCategory(request.getServiceCategoryId()));
        }
        if (request.getName() != null) service.setName(request.getName());
        if (request.getDescription() != null) service.setDescription(request.getDescription());
        if (request.getNameAr() != null) service.setNameAr(request.getNameAr());
        if (request.getDescriptionAr() != null) service.setDescriptionAr(request.getDescriptionAr());
        if (request.getQuotationMode() != null) service.setQuotationMode(request.getQuotationMode());
        if (request.getFulfillmentMode() != null) service.setFulfillmentMode(request.getFulfillmentMode());
        if (request.getEstimatedDurationMinutes() != null) {
            service.setEstimatedDurationMinutes(validDuration(request.getEstimatedDurationMinutes()));
        }
        if (request.getDisplayOrder() != null) service.setDisplayOrder(request.getDisplayOrder());
        return toResponse(catalogServiceRepository.save(service));
    }

    @Transactional
    public CatalogServiceResponse setActive(Integer id, boolean active) {
        CatalogService service = getEntity(id);
        service.setIsActive(active);
        return toResponse(catalogServiceRepository.save(service));
    }

    @Transactional(readOnly = true)
    public CatalogServiceResponse getById(Integer id) {
        return toResponse(getEntity(id));
    }

    @Transactional(readOnly = true)
    public CatalogServiceResponse getByCode(String code) {
        return toResponse(catalogServiceRepository.findByCode(normalizeCode(code))
                .orElseThrow(() -> new ResourceNotFoundException("Service not found with code: " + code)));
    }

    @Transactional(readOnly = true)
    public List<CatalogServiceResponse> list(Integer categoryId, boolean activeOnly) {
        List<CatalogService> services;
        if (categoryId != null) {
            services = activeOnly
                    ? catalogServiceRepository.findByServiceCategoryIdAndIsActiveTrueOrderByDisplayOrderAsc(categoryId)
                    : catalogServiceRepository.findByServiceCategoryIdOrderByDisplayOrderAsc(categoryId);
        } else {
            services = activeOnly
                    ? catalogServiceRepository.findByIsActiveTrueOrderByDisplayOrderAsc()
                    : catalogServiceRepository.findAllByOrderByDisplayOrderAsc();
        }
        return toResponses(services);
    }

    /**
     * Active services a customer in the given country can book.
     */
    @Transactional(readOnly = true)
    public List<CatalogServiceResponse> listAvailableInCountry(Integer categoryId, String countryCode) {
        return toResponses(
                catalogServiceRepository.findAvailableInCountry(categoryId, IsoCodes.country(countryCode)));
    }

    /**
     * Loads a service and checks it belongs to the given category (or one of its ancestors' children).
     * Used when offerings, attributes or quotation settings reference a service.
     */
    @Transactional(readOnly = true)
    public CatalogService getForCategory(Integer serviceId, Integer categoryId) {
        CatalogService service = getEntity(serviceId);
        if (categoryId != null && !isInCategory(service.getServiceCategory(), categoryId)) {
            throw new BusinessException("Service " + service.getCode() + " does not belong to category " + categoryId);
        }
        return service;
    }

    /**
     * Loads a service for a direct booking with this vendor. Refuses inactive services, services
     * that require a quote first, and vendors that do not offer the service's category.
     */
    @Transactional(readOnly = true)
    public CatalogService getForDirectBooking(Integer serviceId, Vendor vendor) {
        CatalogService service = getEntity(serviceId);
        if (!Boolean.TRUE.equals(service.getIsActive())) {
            throw new BusinessException("Service " + service.getCode() + " is not available");
        }
        if (quotationModeResolver.effectiveMode(null, service) == QuotationMode.REQUIRED) {
            throw new BusinessException("Service " + service.getCode()
                    + " requires a quotation; request a quote and book from the accepted quote");
        }
        boolean vendorOffersIt = vendor.getServices().stream()
                .filter(vs -> Boolean.TRUE.equals(vs.getIsActive()))
                .anyMatch(vs -> isInCategory(service.getServiceCategory(), vs.getServiceCategory().getId()));
        if (!vendorOffersIt) {
            throw new BusinessException("Vendor " + vendor.getId() + " does not offer " + service.getCode());
        }
        return service;
    }

    public CatalogService getEntity(Integer id) {
        return catalogServiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Service not found with id: " + id));
    }

    /**
     * Clears the service's own quotation mode so it follows its category again.
     */
    @Transactional
    public CatalogServiceResponse clearQuotationMode(Integer id) {
        CatalogService service = getEntity(id);
        service.setQuotationMode(null);
        return toResponse(catalogServiceRepository.save(service));
    }

    private CatalogServiceResponse toResponse(CatalogService service) {
        CatalogServiceResponse response = catalogServiceMapper.toResponse(service);
        response.setEffectiveQuotationMode(quotationModeResolver.effectiveMode(null, service));
        return response;
    }

    private List<CatalogServiceResponse> toResponses(List<CatalogService> services) {
        return services.stream().map(this::toResponse).toList();
    }

    private boolean isInCategory(ServiceCategory category, Integer categoryId) {
        for (ServiceCategory c = category; c != null; c = c.getParent()) {
            if (categoryId.equals(c.getId())) {
                return true;
            }
        }
        return false;
    }

    private ServiceCategory findCategory(Integer id) {
        return serviceCategoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Service category not found with id: " + id));
    }

    private Integer validDuration(Integer minutes) {
        if (minutes != null && minutes <= 0) {
            throw new BusinessException("estimatedDurationMinutes must be greater than 0");
        }
        return minutes;
    }

    private static String normalizeCode(String code) {
        return code.trim().toUpperCase(Locale.ROOT);
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
