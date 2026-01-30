package com.autoadda.apis.other.repository;

import com.autoadda.apis.other.entity.QuotationScopeDefinition;
import com.autoadda.apis.other.util.QuotationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuotationScopeDefinitionRepository extends JpaRepository<QuotationScopeDefinition, Integer> {

    /**
     * Find scope definition by service category ID.
     */
    Optional<QuotationScopeDefinition> findByServiceCategoryId(Integer serviceCategoryId);

    /**
     * Find active scope definition by service category ID.
     */
    Optional<QuotationScopeDefinition> findByServiceCategoryIdAndIsActiveTrue(Integer serviceCategoryId);

    /**
     * Check if scope definition exists for a service category.
     */
    boolean existsByServiceCategoryId(Integer serviceCategoryId);

    /**
     * Find all active scope definitions where quotation is enabled.
     */
    List<QuotationScopeDefinition> findByIsQuotationEnabledTrueAndIsActiveTrue();

    /**
     * Find scope definitions by quotation type.
     */
    List<QuotationScopeDefinition> findByQuotationTypeAndIsActiveTrue(QuotationType quotationType);

    /**
     * Find categories that allow multi-vendor broadcast.
     */
    List<QuotationScopeDefinition> findByAllowMultiVendorBroadcastTrueAndIsActiveTrue();
}
