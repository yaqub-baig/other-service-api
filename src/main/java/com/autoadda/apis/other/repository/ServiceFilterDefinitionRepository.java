package com.autoadda.apis.other.repository;

import com.autoadda.apis.other.entity.ServiceFilterDefinition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ServiceFilterDefinitionRepository extends JpaRepository<ServiceFilterDefinition, Long> {

    /**
     * Find all active filters for a service category.
     */
    List<ServiceFilterDefinition> findByServiceCategoryIdAndIsActiveTrueOrderByDisplayOrderAsc(Integer serviceCategoryId);

    /**
     * Find all filters for a service category (including inactive).
     */
    List<ServiceFilterDefinition> findByServiceCategoryIdOrderByDisplayOrderAsc(Integer serviceCategoryId);

    /**
     * Find required filters for a service category.
     */
    List<ServiceFilterDefinition> findByServiceCategoryIdAndIsRequiredTrueAndIsActiveTrueOrderByDisplayOrderAsc(Integer serviceCategoryId);

    /**
     * Find filters that allow multiple selections.
     */
    List<ServiceFilterDefinition> findByServiceCategoryIdAndAllowMultipleTrueAndIsActiveTrueOrderByDisplayOrderAsc(Integer serviceCategoryId);

    /**
     * Find filter by code within a service category.
     */
    Optional<ServiceFilterDefinition> findByServiceCategoryIdAndFilterCode(Integer serviceCategoryId, String filterCode);

    /**
     * Check if filter code exists within a service category.
     */
    boolean existsByServiceCategoryIdAndFilterCode(Integer serviceCategoryId, String filterCode);

    /**
     * Find filters that depend on a specific filter.
     */
    List<ServiceFilterDefinition> findByServiceCategoryIdAndDependsOnAndIsActiveTrueOrderByDisplayOrderAsc(
            Integer serviceCategoryId, String dependsOn);

    /**
     * Find all active filters across all categories.
     */
    List<ServiceFilterDefinition> findByIsActiveTrueOrderByServiceCategoryIdAscDisplayOrderAsc();

    /**
     * Count filters for a service category.
     */
    long countByServiceCategoryIdAndIsActiveTrue(Integer serviceCategoryId);
}
