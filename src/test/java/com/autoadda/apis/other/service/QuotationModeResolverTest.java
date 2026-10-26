package com.autoadda.apis.other.service;

import com.autoadda.apis.other.entity.CatalogService;
import com.autoadda.apis.other.entity.QuotationScopeDefinition;
import com.autoadda.apis.other.entity.ServiceCategory;
import com.autoadda.apis.other.repository.QuotationScopeDefinitionRepository;
import com.autoadda.apis.other.util.QuotationMode;
import com.autoadda.apis.other.util.QuotationType;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class QuotationModeResolverTest {

    private final QuotationScopeDefinitionRepository scopes = mock(QuotationScopeDefinitionRepository.class);
    private final QuotationModeResolver resolver = new QuotationModeResolver(scopes);

    private static ServiceCategory category(int id) {
        ServiceCategory c = new ServiceCategory();
        c.setId(id);
        return c;
    }

    private static QuotationScopeDefinition scope(boolean enabled, QuotationType type) {
        return QuotationScopeDefinition.builder().isQuotationEnabled(enabled).quotationType(type).build();
    }

    @Test
    void sameCategoryCanMixRequiredAndDirectServices() {
        // Category default: quotes optional
        when(scopes.findByServiceCategoryIdAndCatalogServiceIsNullAndIsActiveTrue(5))
                .thenReturn(Optional.of(scope(true, QuotationType.OPTIONAL)));
        CatalogService accidentRepair = CatalogService.builder().id(1).serviceCategory(category(5))
                .quotationMode(QuotationMode.REQUIRED).build();
        CatalogService carWash = CatalogService.builder().id(2).serviceCategory(category(5))
                .quotationMode(QuotationMode.DIRECT).build();

        assertThat(resolver.effectiveMode(5, accidentRepair)).isEqualTo(QuotationMode.REQUIRED);
        assertThat(resolver.effectiveMode(5, carWash)).isEqualTo(QuotationMode.DIRECT);
    }

    @Test
    void serviceWithoutModeFollowsItsOwnScopeThenCategory() {
        CatalogService service = CatalogService.builder().id(3).serviceCategory(category(5)).build();
        when(scopes.findByCatalogServiceIdAndIsActiveTrue(3)).thenReturn(Optional.of(scope(true, QuotationType.MANDATORY)));
        assertThat(resolver.effectiveMode(5, service)).isEqualTo(QuotationMode.REQUIRED);

        when(scopes.findByCatalogServiceIdAndIsActiveTrue(3)).thenReturn(Optional.empty());
        when(scopes.findByServiceCategoryIdAndCatalogServiceIsNullAndIsActiveTrue(5))
                .thenReturn(Optional.of(scope(true, QuotationType.HYBRID)));
        assertThat(resolver.effectiveMode(5, service)).isEqualTo(QuotationMode.OPTIONAL);
    }

    @Test
    void disabledOrMissingScopeMeansDirect() {
        when(scopes.findByServiceCategoryIdAndCatalogServiceIsNullAndIsActiveTrue(8))
                .thenReturn(Optional.of(scope(false, QuotationType.MANDATORY)));
        assertThat(resolver.effectiveMode(8, null)).isEqualTo(QuotationMode.DIRECT);

        when(scopes.findByServiceCategoryIdAndCatalogServiceIsNullAndIsActiveTrue(9)).thenReturn(Optional.empty());
        assertThat(resolver.effectiveMode(9, null)).isEqualTo(QuotationMode.DIRECT);
    }
}
