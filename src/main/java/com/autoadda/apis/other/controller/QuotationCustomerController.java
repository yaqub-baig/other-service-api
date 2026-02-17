package com.autoadda.apis.other.controller;


import com.autoadda.apis.other.request.BookingFromQuotationRequest;
import com.autoadda.apis.other.request.QuotationRequestSubmission;
import com.autoadda.apis.other.request.QuoteAcceptRejectRequest;
import com.autoadda.apis.other.response.*;
import com.autoadda.apis.other.service.BookingService;
import com.autoadda.apis.other.service.QuotationAdminService;
import com.autoadda.apis.other.service.QuotationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Customer-facing controller for quotation requests.
 * Handles creating requests, viewing quotes, and accepting/rejecting.
 */
@RestController
@RequestMapping("/api/quotations/customer")
@RequiredArgsConstructor
public class QuotationCustomerController {

    private final QuotationService quotationService;
    private final QuotationAdminService adminService;
    private final BookingService bookingService;

    // ==================== Quotation Request Operations ====================

    /**
     * Submit a new quotation request.
     */
    @PostMapping("/request")
    public ResponseEntity<ApiResponse<QuotationRequestResponse>> createRequest(
            @RequestBody QuotationRequestSubmission submission) {
        QuotationRequestResponse request = quotationService.createRequest(submission);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Quotation request submitted", request));
    }

    /**
     * Get customer's quotation requests.
     */
    @GetMapping("/requests")
    public ResponseEntity<ApiResponse<List<QuotationRequestResponse>>> getMyRequests(
            @RequestParam Integer customerId) {
        List<QuotationRequestResponse> requests = quotationService.getCustomerRequests(customerId);
        return ResponseEntity.ok(ApiResponse.success(requests));
    }

    /**
     * Get customer's quotation requests with pagination.
     */
    @GetMapping("/requests/page")
    public ResponseEntity<ApiResponse<Page<QuotationRequestResponse>>> getMyRequestsPaginated(
            @RequestParam Integer customerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<QuotationRequestResponse> requests = quotationService.getCustomerRequestsPaginated(customerId, page, size);
        return ResponseEntity.ok(ApiResponse.success(requests));
    }

    /**
     * Get quotation request details by ID.
     */
    @GetMapping("/request/{requestId}")
    public ResponseEntity<ApiResponse<QuotationRequestResponse>> getRequestById(@PathVariable Integer requestId) {
        QuotationRequestResponse request = quotationService.getRequestById(requestId);
        return ResponseEntity.ok(ApiResponse.success(request));
    }

    /**
     * Get quotation request by quotation number.
     */
    @GetMapping("/request/number/{quotationNumber}")
    public ResponseEntity<ApiResponse<QuotationRequestResponse>> getRequestByNumber(@PathVariable String quotationNumber) {
        QuotationRequestResponse request = quotationService.getRequestByNumber(quotationNumber);
        return ResponseEntity.ok(ApiResponse.success(request));
    }

    /**
     * Get field values for a quotation request.
     */
    @GetMapping("/request/{requestId}/fields")
    public ResponseEntity<ApiResponse<List<QuotationRequestFieldValueResponse>>> getRequestFieldValues(@PathVariable Integer requestId) {
        List<QuotationRequestFieldValueResponse> fieldValues = quotationService.getRequestFieldValues(requestId);
        return ResponseEntity.ok(ApiResponse.success(fieldValues));
    }

    /**
     * Cancel a quotation request.
     */
    @PostMapping("/request/{requestId}/cancel")
    public ResponseEntity<ApiResponse<QuotationRequestResponse>> cancelRequest(
            @PathVariable Integer requestId,
            @RequestParam Integer customerId) {
        QuotationRequestResponse cancelled = quotationService.cancelRequest(requestId, customerId);
        return ResponseEntity.ok(ApiResponse.success("Request cancelled", cancelled));
    }

    // ==================== View & Respond to Quotes ====================

    /**
     * Get all vendor quotations received for a request.
     */
    @GetMapping("/request/{requestId}/quotes")
    public ResponseEntity<ApiResponse<List<VendorQuotationResponse>>> getQuotesForRequest(@PathVariable Integer requestId) {
        List<VendorQuotationResponse> quotes = quotationService.getQuotationsForRequest(requestId);
        return ResponseEntity.ok(ApiResponse.success(quotes));
    }

    /**
     * Get vendor quotation details.
     */
    @GetMapping("/quote/{quotationId}")
    public ResponseEntity<ApiResponse<VendorQuotationResponse>> getQuoteDetails(@PathVariable Integer quotationId) {
        VendorQuotationResponse quote = quotationService.getQuotationById(quotationId);
        return ResponseEntity.ok(ApiResponse.success(quote));
    }

    /**
     * Get line items for a vendor quotation.
     */
    @GetMapping("/quote/{quotationId}/items")
    public ResponseEntity<ApiResponse<List<VendorQuotationLineItemResponse>>> getQuoteLineItems(@PathVariable Integer quotationId) {
        return ResponseEntity.ok(ApiResponse.success(
                quotationService.getQuotationLineItems(quotationId)));
    }

    /**
     * Accept a vendor's quotation.
     */
    @PostMapping("/quote/{quotationId}/accept")
    public ResponseEntity<ApiResponse<VendorQuotationResponse>> acceptQuote(
            @PathVariable Integer quotationId,
            @RequestBody QuoteAcceptRejectRequest request) {
        VendorQuotationResponse accepted = quotationService.acceptQuote(quotationId, request.getCustomerId());
        return ResponseEntity.ok(ApiResponse.success("Quotation accepted", accepted));
    }

    /**
     * Reject a vendor's quotation.
     */
    @PostMapping("/quote/{quotationId}/reject")
    public ResponseEntity<ApiResponse<VendorQuotationResponse>> rejectQuote(
            @PathVariable Integer quotationId,
            @RequestBody QuoteAcceptRejectRequest request) {
        VendorQuotationResponse rejected = quotationService.rejectQuote(
                quotationId, request.getCustomerId(), request.getReason());
        return ResponseEntity.ok(ApiResponse.success("Quotation rejected", rejected));
    }

    // ==================== Helper Endpoints ====================

    /**
     * Get quotation form fields for a service category.
     * Used by frontend to render the quotation request form.
     */
    @GetMapping("/form/category/{categoryId}")
    public ResponseEntity<ApiResponse<List<QuotationFieldDefinitionResponse>>> getQuotationFormFields(@PathVariable Integer categoryId) {
        List<QuotationFieldDefinitionResponse> fields = adminService.getFieldsForCategory(categoryId);
        return ResponseEntity.ok(ApiResponse.success(fields));
    }

    /**
     * Check if quotation is available for a category.
     */
    @GetMapping("/check/category/{categoryId}")
    public ResponseEntity<ApiResponse<Boolean>> checkQuotationAvailable(@PathVariable Integer categoryId) {
        boolean enabled = adminService.isQuotationEnabled(categoryId);
        return ResponseEntity.ok(ApiResponse.success(enabled));
    }

    // ==================== Booking from Quotation ====================

    /**
     * Create a booking from an accepted quotation.
     * The quotation must be in ACCEPTED status to create a booking.
     */
    @PostMapping("/quote/{quotationId}/book")
    public ResponseEntity<ApiResponse<?>> createBookingFromQuote(
            @PathVariable Integer quotationId,
            @RequestBody BookingFromQuotationRequest request) {
        request.setVendorQuotationId(quotationId);
        BookingResponse booking = bookingService.createBookingFromQuotation(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Booking created from quotation", booking));
    }
}
