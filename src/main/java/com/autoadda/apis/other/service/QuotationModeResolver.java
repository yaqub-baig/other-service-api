package com.autoadda.apis.other.service;

import com.autoadda.apis.other.entity.CatalogService;
import com.autoadda.apis.other.entity.QuotationScopeDefinition;
import com.autoadda.apis.other.repository.QuotationScopeDefinitionRepository;
import com.autoadda.apis.other.util.QuotationMode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Works out how a service is bought. A service's own quotationMode wins, then a scope defined for the
 * service, then the category's quotation scope: disabled or missing = DIRECT, MANDATORY = REQUIRED, OPTIONAL/HYBRID = OPTIONAL.
 */
@Component
@RequiredArgsConstructor
public class QuotationModeResolver {

    private final QuotationScopeDefinitionRepository scopeRepository;

    @Transactional(readOnly = true)
    public QuotationMode effectiveMode(Integer categoryId, CatalogService service) {
        if (service != null && service.getQuotationMode() != null) {
            return service.getQuotationMode();
        }
        if (service != null) {
            var serviceScope = scopeRepository.findByCatalogServiceIdAndIsActiveTrue(service.getId());
            if (serviceScope.isPresent()) {
                return fromScope(serviceScope.get());
            }
        }
        Integer category = service != null ? service.getServiceCategory().getId() : categoryId;
        return scopeRepository.findByServiceCategoryIdAndCatalogServiceIsNullAndIsActiveTrue(category)
                .map(QuotationModeResolver::fromScope)
                .orElse(QuotationMode.DIRECT);
    }

    static QuotationMode fromScope(QuotationScopeDefinition scope) {
        if (!Boolean.TRUE.equals(scope.getIsQuotationEnabled())) {
            return QuotationMode.DIRECT;
        }
        return switch (scope.getQuotationType()) {
            case MANDATORY -> QuotationMode.REQUIRED;
            case OPTIONAL, HYBRID -> QuotationMode.OPTIONAL;
        };
    }
}
