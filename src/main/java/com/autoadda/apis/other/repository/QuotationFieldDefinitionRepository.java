package com.autoadda.apis.other.repository;

import com.autoadda.apis.other.entity.QuotationFieldDefinition;
import com.autoadda.apis.other.util.FieldType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuotationFieldDefinitionRepository extends JpaRepository<QuotationFieldDefinition, Integer> {

    /**
     * Find all fields for a service category ordered by display order.
     */
    List<QuotationFieldDefinition> findByServiceCategoryIdAndIsActiveTrueOrderByDisplayOrderAsc(Integer serviceCategoryId);

    /**
     * Find all fields for a service category (including inactive).
     */
    List<QuotationFieldDefinition> findByServiceCategoryIdOrderByDisplayOrderAsc(Integer serviceCategoryId);

    /**
     * Find required fields for a service category.
     */
    List<QuotationFieldDefinition> findByServiceCategoryIdAndIsRequiredTrueAndIsActiveTrueOrderByDisplayOrderAsc(Integer serviceCategoryId);

    /**
     * Find field by code within a service category.
     */
    Optional<QuotationFieldDefinition> findByServiceCategoryIdAndFieldCode(Integer serviceCategoryId, String fieldCode);

    /**
     * Check if field code exists within a service category.
     */
    boolean existsByServiceCategoryIdAndFieldCode(Integer serviceCategoryId, String fieldCode);

    /**
     * Find fields by type within a service category.
     */
    List<QuotationFieldDefinition> findByServiceCategoryIdAndFieldTypeAndIsActiveTrueOrderByDisplayOrderAsc(
            Integer serviceCategoryId, FieldType fieldType);

    /**
     * Find dependent fields (fields that depend on a specific field).
     */
    List<QuotationFieldDefinition> findByDependsOnIdAndIsActiveTrue(Integer parentFieldId);

    /**
     * Count fields for a service category.
     */
    long countByServiceCategoryIdAndIsActiveTrue(Integer serviceCategoryId);
}
