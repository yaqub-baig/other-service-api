package com.autoadda.apis.other.repository;

import com.autoadda.apis.other.entity.VendorQuotation;
import com.autoadda.apis.other.util.VendorQuotationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface VendorQuotationRepository extends JpaRepository<VendorQuotation, Integer> {

    /**
     * Find by quote number.
     */
    Optional<VendorQuotation> findByQuoteNumber(String quoteNumber);

    /**
     * Find all quotations for a request.
     */
    List<VendorQuotation> findByQuotationRequestIdOrderByCreatedAtDesc(Integer quotationRequestId);

    /**
     * Find quotations for a request by status.
     */
    List<VendorQuotation> findByQuotationRequestIdAndStatusOrderByCreatedAtDesc(
            Integer quotationRequestId, VendorQuotationStatus status);

    /**
     * Find all quotations by vendor.
     */
    List<VendorQuotation> findByVendorIdOrderByCreatedAtDesc(Integer vendorId);

    /**
     * Find quotations by vendor with pagination.
     */
    Page<VendorQuotation> findByVendorId(Integer vendorId, Pageable pageable);

    /**
     * Find quotations by vendor and status.
     */
    List<VendorQuotation> findByVendorIdAndStatusOrderByCreatedAtDesc(Integer vendorId, VendorQuotationStatus status);

    /**
     * Check if vendor has already quoted for a request.
     */
    boolean existsByQuotationRequestIdAndVendorId(Integer quotationRequestId, Integer vendorId);

    /**
     * Find vendor's quotation for a specific request.
     */
    Optional<VendorQuotation> findByQuotationRequestIdAndVendorId(Integer quotationRequestId, Integer vendorId);

    /**
     * Count quotes for a request.
     */
    long countByQuotationRequestId(Integer quotationRequestId);

    /**
     * Count quotes by status for a request.
     */
    long countByQuotationRequestIdAndStatus(Integer quotationRequestId, VendorQuotationStatus status);

    /**
     * Find expired quotations that need status update.
     */
    @Query("SELECT vq FROM VendorQuotation vq WHERE vq.status = :status AND vq.validUntil < :today")
    List<VendorQuotation> findExpiredQuotations(
            @Param("status") VendorQuotationStatus status,
            @Param("today") LocalDate today);

    /**
     * Find accepted quotation for a request (should be only one).
     */
    Optional<VendorQuotation> findByQuotationRequestIdAndStatus(Integer quotationRequestId, VendorQuotationStatus status);

    /**
     * Check if quote number exists.
     */
    boolean existsByQuoteNumber(String quoteNumber);

    /**
     * Count pending quotations for a vendor.
     */
    long countByVendorIdAndStatus(Integer vendorId, VendorQuotationStatus status);
}
