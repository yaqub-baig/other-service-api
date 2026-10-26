package com.autoadda.apis.other.repository;

import com.autoadda.apis.other.entity.QuotationScopeDefinition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * A category has one default scope (catalogService IS NULL); a catalog service may add its own
 * scope that overrides the category default.
 */
@Repository
public interface QuotationScopeDefinitionRepository extends JpaRepository<QuotationScopeDefinition, Integer> {

    Optional<QuotationScopeDefinition> findByServiceCategoryIdAndCatalogServiceIsNull(Integer serviceCategoryId);

    Optional<QuotationScopeDefinition> findByServiceCategoryIdAndCatalogServiceIsNullAndIsActiveTrue(Integer serviceCategoryId);

    Optional<QuotationScopeDefinition> findByCatalogServiceId(Integer catalogServiceId);

    Optional<QuotationScopeDefinition> findByCatalogServiceIdAndIsActiveTrue(Integer catalogServiceId);

    List<QuotationScopeDefinition> findByIsQuotationEnabledTrueAndIsActiveTrue();
}
