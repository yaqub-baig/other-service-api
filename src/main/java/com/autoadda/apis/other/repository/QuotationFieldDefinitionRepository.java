package com.autoadda.apis.other.repository;

import com.autoadda.apis.other.entity.QuotationFieldDefinition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * "Category default" methods only see rows with no catalog service; service-level fields are read
 * through the CatalogServiceId methods.
 */
@Repository
public interface QuotationFieldDefinitionRepository extends JpaRepository<QuotationFieldDefinition, Integer> {

    List<QuotationFieldDefinition> findByServiceCategoryIdAndCatalogServiceIsNullAndIsActiveTrueOrderByDisplayOrderAsc(Integer serviceCategoryId);

    List<QuotationFieldDefinition> findByServiceCategoryIdAndCatalogServiceIsNullOrderByDisplayOrderAsc(Integer serviceCategoryId);

    List<QuotationFieldDefinition> findByServiceCategoryIdAndCatalogServiceIsNullAndIsRequiredTrueAndIsActiveTrueOrderByDisplayOrderAsc(Integer serviceCategoryId);

    boolean existsByServiceCategoryIdAndCatalogServiceIsNullAndFieldCode(Integer serviceCategoryId, String fieldCode);

    List<QuotationFieldDefinition> findByCatalogServiceIdAndIsActiveTrueOrderByDisplayOrderAsc(Integer catalogServiceId);

    boolean existsByCatalogServiceIdAndFieldCode(Integer catalogServiceId, String fieldCode);

    Optional<QuotationFieldDefinition> findByServiceCategoryIdAndFieldCode(Integer serviceCategoryId, String fieldCode);

    List<QuotationFieldDefinition> findByDependsOnIdAndIsActiveTrue(Integer parentFieldId);
}
