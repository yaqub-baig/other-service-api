package com.autoadda.apis.other.repository;

import com.autoadda.apis.other.entity.VendorQuotationLineItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface VendorQuotationLineItemRepository extends JpaRepository<VendorQuotationLineItem, Integer> {

    /**
     * Find all line items for a vendor quotation ordered by display order.
     */
    List<VendorQuotationLineItem> findByVendorQuotationIdOrderByDisplayOrderAsc(Integer vendorQuotationId);

    /**
     * Find non-optional line items for a quotation.
     */
    List<VendorQuotationLineItem> findByVendorQuotationIdAndIsOptionalFalseOrderByDisplayOrderAsc(Integer vendorQuotationId);

    /**
     * Find optional line items for a quotation.
     */
    List<VendorQuotationLineItem> findByVendorQuotationIdAndIsOptionalTrueOrderByDisplayOrderAsc(Integer vendorQuotationId);

    /**
     * Find line items by type.
     */
    List<VendorQuotationLineItem> findByVendorQuotationIdAndItemTypeOrderByDisplayOrderAsc(
            Integer vendorQuotationId, String itemType);

    /**
     * Delete all line items for a quotation.
     */
    void deleteByVendorQuotationId(Integer vendorQuotationId);

    /**
     * Calculate total for non-optional items.
     */
    @Query("SELECT COALESCE(SUM(li.totalPrice), 0) FROM VendorQuotationLineItem li " +
           "WHERE li.vendorQuotation.id = :quotationId AND li.isOptional = false")
    BigDecimal calculateRequiredTotal(@Param("quotationId") Integer quotationId);

    /**
     * Calculate total for all items.
     */
    @Query("SELECT COALESCE(SUM(li.totalPrice), 0) FROM VendorQuotationLineItem li " +
           "WHERE li.vendorQuotation.id = :quotationId")
    BigDecimal calculateTotal(@Param("quotationId") Integer quotationId);

    /**
     * Count line items for a quotation.
     */
    long countByVendorQuotationId(Integer vendorQuotationId);
}
