package com.autoadda.apis.other.repository;

import com.autoadda.apis.other.entity.ServiceAttributeDefinition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ServiceAttributeDefinitionRepository extends JpaRepository<ServiceAttributeDefinition, Long> {

    /**
     * Find all attributes for a service category.
     */
    List<ServiceAttributeDefinition> findByServiceCategoryIdAndIsActiveTrueOrderByDisplayOrderAsc(Integer serviceCategoryId);

    /**
     * Find all active attributes for a service category.
     */
    List<ServiceAttributeDefinition> findByServiceCategoryIdOrderByDisplayOrderAsc(Integer serviceCategoryId);

    /**
     * Find filterable attributes for a service category.
     */
    List<ServiceAttributeDefinition> findByServiceCategoryIdAndIsFilterableTrueAndIsActiveTrueOrderByDisplayOrderAsc(Integer serviceCategoryId);

    /**
     * Find required attributes for a service category.
     */
    List<ServiceAttributeDefinition> findByServiceCategoryIdAndIsRequiredTrueAndIsActiveTrueOrderByDisplayOrderAsc(Integer serviceCategoryId);

    /**
     * Find searchable attributes for a service category.
     */
    List<ServiceAttributeDefinition> findByServiceCategoryIdAndIsSearchableTrueAndIsActiveTrueOrderByDisplayOrderAsc(Integer serviceCategoryId);

    /**
     * Find attribute by code within a service category.
     */
    Optional<ServiceAttributeDefinition> findByServiceCategoryIdAndAttributeCode(Integer serviceCategoryId, String attributeCode);

    /**
     * Check if attribute code exists within a service category.
     */
    boolean existsByServiceCategoryIdAndAttributeCode(Integer serviceCategoryId, String attributeCode);

    /**
     * Find all active attributes across all categories.
     */
    List<ServiceAttributeDefinition> findByIsActiveTrueOrderByServiceCategoryIdAscDisplayOrderAsc();

    /**
     * Count attributes for a service category.
     */
    long countByServiceCategoryIdAndIsActiveTrue(Integer serviceCategoryId);
}
