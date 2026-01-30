package com.autoadda.apis.other.controller;

import com.autoadda.apis.other.request.VendorQuotationSubmission;
import com.autoadda.apis.other.response.*;
import com.autoadda.apis.other.service.QuotationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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
    public ResponseEntity<ApiResponse<List<QuotationRequestResponse>>> findOpenRequests(
            @RequestParam Integer vendorId,
            @RequestParam Integer categoryId) {
        List<QuotationRequestResponse> requests = quotationService.findOpenRequestsForVendor(vendorId, categoryId);
        return ResponseEntity.ok(ApiResponse.success(requests));
    }

    /**
     * Get details of a specific quotation request.
     */
    @GetMapping("/request/{requestId}")
    public ResponseEntity<ApiResponse<QuotationRequestResponse>> getRequestDetails(@PathVariable Integer requestId) {
        QuotationRequestResponse request = quotationService.getRequestById(requestId);
        return ResponseEntity.ok(ApiResponse.success(request));
    }

    /**
     * Get field values for a quotation request.
     */
    @GetMapping("/request/{requestId}/fields")
    public ResponseEntity<ApiResponse<List<QuotationRequestFieldValueResponse>>> getRequestFieldValues(@PathVariable Integer requestId) {
        return ResponseEntity.ok(ApiResponse.success(
                quotationService.getRequestFieldValues(requestId)));
    }

    // ==================== Submit & Manage Quotations ====================

    /**
     * Submit a quotation for a request.
     */
    @PostMapping("/quote")
    public ResponseEntity<ApiResponse<VendorQuotationResponse>> submitQuote(
            @RequestBody VendorQuotationSubmission submission) {
        VendorQuotationResponse quote = quotationService.submitQuote(submission);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Quotation submitted", quote));
    }

    /**
     * Get vendor's submitted quotations.
     */
    @GetMapping("/quotes")
    public ResponseEntity<ApiResponse<List<VendorQuotationResponse>>> getMyQuotations(@RequestParam Integer vendorId) {
        List<VendorQuotationResponse> quotes = quotationService.getVendorQuotations(vendorId);
        return ResponseEntity.ok(ApiResponse.success(quotes));
    }

    /**
     * Get vendor's quotations with pagination.
     */
    @GetMapping("/quotes/page")
    public ResponseEntity<ApiResponse<Page<VendorQuotationResponse>>> getMyQuotationsPaginated(
            @RequestParam Integer vendorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<VendorQuotationResponse> quotes = quotationService.getVendorQuotationsPaginated(vendorId, page, size);
        return ResponseEntity.ok(ApiResponse.success(quotes));
    }

    /**
     * Get a specific quotation by ID.
     */
    @GetMapping("/quote/{quotationId}")
    public ResponseEntity<ApiResponse<VendorQuotationResponse>> getQuotationById(@PathVariable Integer quotationId) {
        VendorQuotationResponse quote = quotationService.getQuotationById(quotationId);
        return ResponseEntity.ok(ApiResponse.success(quote));
    }

    /**
     * Get line items for a quotation.
     */
    @GetMapping("/quote/{quotationId}/items")
    public ResponseEntity<ApiResponse<List<VendorQuotationLineItemResponse>>> getQuotationLineItems(@PathVariable Integer quotationId) {
        List<VendorQuotationLineItemResponse> items = quotationService.getQuotationLineItems(quotationId);
        return ResponseEntity.ok(ApiResponse.success(items));
    }

    /**
     * Withdraw a submitted quotation.
     */
    @PostMapping("/quote/{quotationId}/withdraw")
    public ResponseEntity<ApiResponse<VendorQuotationResponse>> withdrawQuote(
            @PathVariable Integer quotationId,
            @RequestParam Integer vendorId) {
        VendorQuotationResponse withdrawn = quotationService.withdrawQuote(quotationId, vendorId);
        return ResponseEntity.ok(ApiResponse.success("Quotation withdrawn", withdrawn));
    }

    // ==================== Statistics ====================

    /**
     * Get summary statistics for vendor's quotations.
     * This would be expanded to include more detailed stats.
     */
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getQuotationStats(@RequestParam Integer vendorId) {
     List<VendorQuotationResponse> allQuotes = quotationService.getVendorQuotations(vendorId);

        long pending = allQuotes.stream()
                .filter(q -> q.getStatus().name().equals("SENT"))
                .count();
        long accepted = allQuotes.stream()
                .filter(q -> q.getStatus().name().equals("ACCEPTED"))
                .count();
        long rejected = allQuotes.stream()
                .filter(q -> q.getStatus().name().equals("REJECTED"))
                .count();

        return ResponseEntity.ok(ApiResponse.success(java.util.Map.of(
                "total", (long)allQuotes.size(),
                "pending", pending,
                "accepted", accepted,
                "rejected", rejected
        )));
    }
}
