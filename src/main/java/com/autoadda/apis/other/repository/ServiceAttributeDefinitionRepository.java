package com.autoadda.apis.other.repository;

import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;
import com.autoadda.apis.other.entity.ServiceAttributeDefinition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * "Category default" methods only see rows with no catalog service; service-level rows belong to a
 * schema version and are read through the SchemaVersionId methods.
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

    // ---- schema versions ----

    List<ServiceAttributeDefinition> findBySchemaVersionIdAndIsActiveTrueOrderByDisplayOrderAsc(Integer schemaVersionId);

    List<ServiceAttributeDefinition> findBySchemaVersionIdOrderByDisplayOrderAsc(Integer schemaVersionId);

    boolean existsBySchemaVersionIdAndAttributeCode(Integer schemaVersionId, String attributeCode);

    // ---- any level ----

    List<ServiceAttributeDefinition> findByServiceCategoryIdAndIsActiveTrue(Integer serviceCategoryId);

    List<ServiceAttributeDefinition> findByIsActiveTrueOrderByServiceCategoryIdAscDisplayOrderAsc();

    /**
     * Codes of active attributes, optionally for one category: the whitelist for JSON search paths.
     */
    @Query("SELECT DISTINCT a.attributeCode FROM ServiceAttributeDefinition a WHERE a.isActive = true " +
           "AND (:categoryId IS NULL OR a.serviceCategory.id = :categoryId)")
    List<String> findActiveCodes(@Param("categoryId") Integer categoryId);
}
