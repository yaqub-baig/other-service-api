package com.autoadda.apis.other.repository;

import com.autoadda.apis.other.entity.ServiceOffering;
import com.autoadda.apis.other.util.StockStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ServiceOfferingRepository extends JpaRepository<ServiceOffering, Long> {

    /**
     * Find all active offerings for a vendor service.
     */
    List<ServiceOffering> findByVendorServiceIdAndIsActiveTrueOrderByDisplayOrderAsc(Integer vendorServiceId);

    /**
     * Find all offerings for a vendor service (including inactive).
     */
    List<ServiceOffering> findByVendorServiceIdOrderByDisplayOrderAsc(Integer vendorServiceId);

    /**
     * Find all active offerings for a vendor (across all their services).
     */
    @Query("SELECT o FROM ServiceOffering o WHERE o.vendorService.vendor.id = :vendorId AND o.isActive = true ORDER BY o.displayOrder ASC")
    List<ServiceOffering> findByVendorIdAndIsActiveTrue(@Param("vendorId") Integer vendorId);

    /**
     * Find all offerings for a vendor (including inactive).
     */
    @Query("SELECT o FROM ServiceOffering o WHERE o.vendorService.vendor.id = :vendorId ORDER BY o.displayOrder ASC")
    List<ServiceOffering> findByVendorId(@Param("vendorId") Integer vendorId);

    /**
     * Find offerings by service category.
     */
    @Query("SELECT o FROM ServiceOffering o WHERE o.vendorService.serviceCategory.id = :categoryId AND o.isActive = true ORDER BY o.displayOrder ASC")
    List<ServiceOffering> findByCategoryIdAndIsActiveTrue(@Param("categoryId") Integer categoryId);

    /**
     * Paginated search for offerings by category.
     */
    @Query("SELECT o FROM ServiceOffering o WHERE o.vendorService.serviceCategory.id = :categoryId AND o.isActive = true")
    Page<ServiceOffering> findByCategoryIdAndIsActiveTrue(@Param("categoryId") Integer categoryId, Pageable pageable);

    /**
     * Find offering by SKU.
     */
    Optional<ServiceOffering> findBySku(String sku);

    /**
     * Check if SKU exists.
     */
    boolean existsBySku(String sku);

    /**
     * Find offerings by stock status.
     */
    List<ServiceOffering> findByStockStatusAndIsActiveTrueOrderByDisplayOrderAsc(StockStatus stockStatus);

    /**
     * Find featured offerings.
     */
    List<ServiceOffering> findByIsFeaturedTrueAndIsActiveTrueOrderByDisplayOrderAsc();

    /**
     * Find featured offerings by category.
     */
    @Query("SELECT o FROM ServiceOffering o WHERE o.vendorService.serviceCategory.id = :categoryId AND o.isFeatured = true AND o.isActive = true ORDER BY o.displayOrder ASC")
    List<ServiceOffering> findFeaturedByCategoryId(@Param("categoryId") Integer categoryId);

    /**
     * Find offerings in price range.
     */
    @Query("SELECT o FROM ServiceOffering o WHERE o.vendorService.serviceCategory.id = :categoryId AND o.price BETWEEN :minPrice AND :maxPrice AND o.isActive = true ORDER BY o.price ASC")
    List<ServiceOffering> findByCategoryIdAndPriceRange(
            @Param("categoryId") Integer categoryId,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice);

    /**
     * Search offerings by name (case-insensitive).
     */
    @Query("SELECT o FROM ServiceOffering o WHERE LOWER(o.offeringName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) AND o.isActive = true ORDER BY o.displayOrder ASC")
    List<ServiceOffering> searchByName(@Param("searchTerm") String searchTerm);

    /**
     * Search offerings by name within a category.
     */
    @Query("SELECT o FROM ServiceOffering o WHERE o.vendorService.serviceCategory.id = :categoryId AND LOWER(o.offeringName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) AND o.isActive = true ORDER BY o.displayOrder ASC")
    List<ServiceOffering> searchByNameInCategory(
            @Param("categoryId") Integer categoryId,
            @Param("searchTerm") String searchTerm);

    /**
     * Find offerings with JSON attribute filter using MySQL JSON_EXTRACT.
     * Example: Find all tyres with brand = "MRF"
     */
    @Query(value = "SELECT * FROM service_offerings o WHERE o.is_active = true AND JSON_EXTRACT(o.filterable_attributes, :jsonPath) = :value ORDER BY o.display_order ASC", nativeQuery = true)
    List<ServiceOffering> findByFilterableAttribute(
            @Param("jsonPath") String jsonPath,
            @Param("value") String value);

    /**
     * Find offerings with multiple JSON attribute filters.
     * Uses JSON_CONTAINS for checking if JSON contains a value.
     */
    @Query(value = "SELECT * FROM service_offerings o WHERE o.is_active = true AND JSON_CONTAINS(o.filterable_attributes, :jsonFragment) ORDER BY o.display_order ASC", nativeQuery = true)
    List<ServiceOffering> findByFilterableAttributesContaining(@Param("jsonFragment") String jsonFragment);

    /**
     * Count offerings for a vendor service.
     */
    long countByVendorServiceIdAndIsActiveTrue(Integer vendorServiceId);

    /**
     * Count offerings for a vendor.
     */
    @Query("SELECT COUNT(o) FROM ServiceOffering o WHERE o.vendorService.vendor.id = :vendorId AND o.isActive = true")
    long countByVendorIdAndIsActiveTrue(@Param("vendorId") Integer vendorId);

    /**
     * Count offerings for a category.
     */
    @Query("SELECT COUNT(o) FROM ServiceOffering o WHERE o.vendorService.serviceCategory.id = :categoryId AND o.isActive = true")
    long countByCategoryIdAndIsActiveTrue(@Param("categoryId") Integer categoryId);

    /**
     * Find top-rated offerings.
     */
    @Query("SELECT o FROM ServiceOffering o WHERE o.isActive = true AND o.totalRatings > 0 ORDER BY o.averageRating DESC")
    List<ServiceOffering> findTopRated(Pageable pageable);

    /**
     * Find most popular offerings (by order count).
     */
    @Query("SELECT o FROM ServiceOffering o WHERE o.isActive = true ORDER BY o.totalOrders DESC")
    List<ServiceOffering> findMostPopular(Pageable pageable);

    /**
     * Find offerings by vendor and category.
     */
    @Query("SELECT o FROM ServiceOffering o WHERE o.vendorService.vendor.id = :vendorId AND o.vendorService.serviceCategory.id = :categoryId AND o.isActive = true ORDER BY o.displayOrder ASC")
    List<ServiceOffering> findByVendorIdAndCategoryId(
            @Param("vendorId") Integer vendorId,
            @Param("categoryId") Integer categoryId);
}
