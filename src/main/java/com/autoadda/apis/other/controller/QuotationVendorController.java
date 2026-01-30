package com.autoadda.apis.other.controller;

import com.autoadda.apis.other.entity.QuotationRequest;
import com.autoadda.apis.other.entity.VendorQuotation;
import com.autoadda.apis.other.entity.VendorQuotationLineItem;
import com.autoadda.apis.other.request.VendorQuotationSubmission;
import com.autoadda.apis.other.response.OsGenericResponse;
import com.autoadda.apis.other.service.QuotationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Vendor-facing controller for quotation operations.
 * Handles viewing requests, submitting quotes, and managing quotations.
 */
@RestController
@RequestMapping("/api/quotations/vendor")
@RequiredArgsConstructor
public class QuotationVendorController {

    private final QuotationService quotationService;

    // ==================== View Open Requests ====================

    /**
     * Find open quotation requests for vendor to quote on.
     * Filters by vendor's service categories and location.
     */
    @GetMapping("/requests/open")
    public ResponseEntity<OsGenericResponse> findOpenRequests(
            @RequestParam Integer vendorId,
            @RequestParam Integer categoryId) {
        List<QuotationRequest> requests = quotationService.findOpenRequestsForVendor(vendorId, categoryId);
        return ResponseEntity.ok(OsGenericResponse.success(requests));
    }

    /**
     * Get details of a specific quotation request.
     */
    @GetMapping("/request/{requestId}")
    public ResponseEntity<OsGenericResponse> getRequestDetails(@PathVariable Integer requestId) {
        QuotationRequest request = quotationService.getRequestById(requestId);
        return ResponseEntity.ok(OsGenericResponse.success(request));
    }

    /**
     * Get field values for a quotation request.
     */
    @GetMapping("/request/{requestId}/fields")
    public ResponseEntity<OsGenericResponse> getRequestFieldValues(@PathVariable Integer requestId) {
        return ResponseEntity.ok(OsGenericResponse.success(
                quotationService.getRequestFieldValues(requestId)));
    }

    // ==================== Submit & Manage Quotations ====================

    /**
     * Submit a quotation for a request.
     */
    @PostMapping("/quote")
    public ResponseEntity<OsGenericResponse> submitQuote(
            @RequestBody VendorQuotationSubmission submission) {
        VendorQuotation quote = quotationService.submitQuote(submission);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(OsGenericResponse.success("Quotation submitted", quote));
    }

    /**
     * Get vendor's submitted quotations.
     */
    @GetMapping("/quotes")
    public ResponseEntity<OsGenericResponse> getMyQuotations(@RequestParam Integer vendorId) {
        List<VendorQuotation> quotes = quotationService.getVendorQuotations(vendorId);
        return ResponseEntity.ok(OsGenericResponse.success(quotes));
    }

    /**
     * Get vendor's quotations with pagination.
     */
    @GetMapping("/quotes/page")
    public ResponseEntity<OsGenericResponse> getMyQuotationsPaginated(
            @RequestParam Integer vendorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<VendorQuotation> quotes = quotationService.getVendorQuotationsPaginated(vendorId, page, size);
        return ResponseEntity.ok(OsGenericResponse.success(quotes));
    }

    /**
     * Get a specific quotation by ID.
     */
    @GetMapping("/quote/{quotationId}")
    public ResponseEntity<OsGenericResponse> getQuotationById(@PathVariable Integer quotationId) {
        VendorQuotation quote = quotationService.getQuotationById(quotationId);
        return ResponseEntity.ok(OsGenericResponse.success(quote));
    }

    /**
     * Get line items for a quotation.
     */
    @GetMapping("/quote/{quotationId}/items")
    public ResponseEntity<OsGenericResponse> getQuotationLineItems(@PathVariable Integer quotationId) {
        List<VendorQuotationLineItem> items = quotationService.getQuotationLineItems(quotationId);
        return ResponseEntity.ok(OsGenericResponse.success(items));
    }

    /**
     * Withdraw a submitted quotation.
     */
    @PostMapping("/quote/{quotationId}/withdraw")
    public ResponseEntity<OsGenericResponse> withdrawQuote(
            @PathVariable Integer quotationId,
            @RequestParam Integer vendorId) {
        VendorQuotation withdrawn = quotationService.withdrawQuote(quotationId, vendorId);
        return ResponseEntity.ok(OsGenericResponse.success("Quotation withdrawn", withdrawn));
    }

    // ==================== Statistics ====================

    /**
     * Get summary statistics for vendor's quotations.
     * This would be expanded to include more detailed stats.
     */
    @GetMapping("/stats")
    public ResponseEntity<OsGenericResponse> getQuotationStats(@RequestParam Integer vendorId) {
        List<VendorQuotation> allQuotes = quotationService.getVendorQuotations(vendorId);

        long pending = allQuotes.stream()
                .filter(q -> q.getStatus().name().equals("SENT"))
                .count();
        long accepted = allQuotes.stream()
                .filter(q -> q.getStatus().name().equals("ACCEPTED"))
                .count();
        long rejected = allQuotes.stream()
                .filter(q -> q.getStatus().name().equals("REJECTED"))
                .count();

        return ResponseEntity.ok(OsGenericResponse.success(java.util.Map.of(
                "total", allQuotes.size(),
                "pending", pending,
                "accepted", accepted,
                "rejected", rejected
        )));
    }
}
