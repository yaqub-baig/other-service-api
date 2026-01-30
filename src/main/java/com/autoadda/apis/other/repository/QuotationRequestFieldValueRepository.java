package com.autoadda.apis.other.repository;

import com.autoadda.apis.other.entity.QuotationRequestFieldValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuotationRequestFieldValueRepository extends JpaRepository<QuotationRequestFieldValue, Integer> {

    /**
     * Find all field values for a quotation request.
     */
    List<QuotationRequestFieldValue> findByQuotationRequestId(Integer quotationRequestId);

    /**
     * Find a specific field value by request and field code.
     */
    Optional<QuotationRequestFieldValue> findByQuotationRequestIdAndFieldCode(Integer quotationRequestId, String fieldCode);

    /**
     * Delete all field values for a quotation request.
     */
    void deleteByQuotationRequestId(Integer quotationRequestId);

    /**
     * Count field values for a request.
     */
    long countByQuotationRequestId(Integer quotationRequestId);
}
