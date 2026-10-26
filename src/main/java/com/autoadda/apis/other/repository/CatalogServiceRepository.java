package com.autoadda.apis.other.repository;

import com.autoadda.apis.other.entity.CatalogService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CatalogServiceRepository extends JpaRepository<CatalogService, Integer> {

    Optional<CatalogService> findByCode(String code);

    boolean existsByCode(String code);

    List<CatalogService> findAllByOrderByDisplayOrderAsc();

    List<CatalogService> findByIsActiveTrueOrderByDisplayOrderAsc();

    List<CatalogService> findByServiceCategoryIdOrderByDisplayOrderAsc(Integer serviceCategoryId);

    List<CatalogService> findByServiceCategoryIdAndIsActiveTrueOrderByDisplayOrderAsc(Integer serviceCategoryId);

    /**
     * Active services available in a country, optionally within one category.
     */
    @Query("SELECT s FROM CatalogService s WHERE s.isActive = true " +
           "AND (:categoryId IS NULL OR s.serviceCategory.id = :categoryId) " +
           "AND EXISTS (SELECT m.id FROM ServiceMarketAvailability m " +
           "            WHERE m.catalogService = s AND m.countryCode = :countryCode AND m.isAvailable = true) " +
           "ORDER BY s.displayOrder ASC")
    List<CatalogService> findAvailableInCountry(@Param("categoryId") Integer categoryId,
                                                @Param("countryCode") String countryCode);
}
