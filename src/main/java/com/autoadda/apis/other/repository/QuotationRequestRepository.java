package com.autoadda.apis.other.repository;

import com.autoadda.apis.other.entity.QuotationRequest;
import com.autoadda.apis.other.util.QuotationRequestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface QuotationRequestRepository extends JpaRepository<QuotationRequest, Integer> {

    /**
     * Find by quotation number.
     */
    Optional<QuotationRequest> findByQuotationNumber(String quotationNumber);

    /**
     * Find all requests by customer ID.
     */
    List<QuotationRequest> findByCustomerIdOrderByCreatedAtDesc(Integer customerId);

    /**
     * Find all requests by customer ID with pagination.
     */
    Page<QuotationRequest> findByCustomerId(Integer customerId, Pageable pageable);

    /**
     * Find requests by customer and status.
     */
    List<QuotationRequest> findByCustomerIdAndStatusOrderByCreatedAtDesc(Integer customerId, QuotationRequestStatus status);

    /**
     * Find requests by service category.
     */
    List<QuotationRequest> findByServiceCategoryIdAndStatusOrderByCreatedAtDesc(Integer serviceCategoryId, QuotationRequestStatus status);

    /**
     * Find open requests in a city for vendor matching.
     */
    @Query("SELECT qr FROM QuotationRequest qr WHERE qr.status = :status AND qr.city = :city " +
           "AND qr.expiresAt > :now ORDER BY qr.createdAt DESC")
    List<QuotationRequest> findOpenRequestsInCity(
            @Param("status") QuotationRequestStatus status,
            @Param("city") String city,
            @Param("now") LocalDateTime now);

    /**
     * Find expired requests that need status update.
     */
    @Query("SELECT qr FROM QuotationRequest qr WHERE qr.status IN :statuses AND qr.expiresAt < :now")
    List<QuotationRequest> findExpiredRequests(
            @Param("statuses") List<QuotationRequestStatus> statuses,
            @Param("now") LocalDateTime now);

    /**
     * Find requests within radius for geo-matching (uses Haversine formula approximation).
     */
    @Query("SELECT qr FROM QuotationRequest qr WHERE qr.serviceCategory.id = :categoryId " +
           "AND qr.status = :status AND qr.expiresAt > :now " +
           "AND qr.latitude IS NOT NULL AND qr.longitude IS NOT NULL " +
           "ORDER BY qr.createdAt DESC")
    List<QuotationRequest> findOpenRequestsByCategory(
            @Param("categoryId") Integer categoryId,
            @Param("status") QuotationRequestStatus status,
            @Param("now") LocalDateTime now);

    /**
     * Count active requests by customer.
     */
    long countByCustomerIdAndStatusIn(Integer customerId, List<QuotationRequestStatus> statuses);

    /**
     * Check if quotation number exists.
     */
    boolean existsByQuotationNumber(String quotationNumber);
}
