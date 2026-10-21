package com.autoadda.apis.other.repository;

import com.autoadda.apis.other.entity.ServiceAttributeDefinition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * "Category default" methods only see rows with no catalog service; service-level rows are read
 * through the CatalogServiceId methods.
 */
@Repository
public interface ServiceAttributeDefinitionRepository extends JpaRepository<ServiceAttributeDefinition, Long> {

    // ---- category defaults (catalogService IS NULL) ----

    List<ServiceAttributeDefinition> findByServiceCategoryIdAndCatalogServiceIsNullAndIsActiveTrueOrderByDisplayOrderAsc(Integer serviceCategoryId);

    List<ServiceAttributeDefinition> findByServiceCategoryIdAndCatalogServiceIsNullOrderByDisplayOrderAsc(Integer serviceCategoryId);

    List<ServiceAttributeDefinition> findByServiceCategoryIdAndCatalogServiceIsNullAndIsFilterableTrueAndIsActiveTrueOrderByDisplayOrderAsc(Integer serviceCategoryId);

    List<ServiceAttributeDefinition> findByServiceCategoryIdAndCatalogServiceIsNullAndIsRequiredTrueAndIsActiveTrueOrderByDisplayOrderAsc(Integer serviceCategoryId);

    Optional<ServiceAttributeDefinition> findByServiceCategoryIdAndCatalogServiceIsNullAndAttributeCode(Integer serviceCategoryId, String attributeCode);

    boolean existsByServiceCategoryIdAndCatalogServiceIsNullAndAttributeCode(Integer serviceCategoryId, String attributeCode);

    // ---- service-level ----

    List<ServiceAttributeDefinition> findByCatalogServiceIdAndIsActiveTrueOrderByDisplayOrderAsc(Integer catalogServiceId);

    List<ServiceAttributeDefinition> findByCatalogServiceIdOrderByDisplayOrderAsc(Integer catalogServiceId);

    boolean existsByCatalogServiceIdAndAttributeCode(Integer catalogServiceId, String attributeCode);

    // ---- any level ----

    List<ServiceAttributeDefinition> findByServiceCategoryIdAndIsActiveTrue(Integer serviceCategoryId);

    List<ServiceAttributeDefinition> findByIsActiveTrueOrderByServiceCategoryIdAscDisplayOrderAsc();
}
