package com.autoadda.apis.other.controller;

import com.autoadda.apis.other.entity.Booking;
import com.autoadda.apis.other.entity.QuotationFieldDefinition;
import com.autoadda.apis.other.entity.QuotationRequest;
import com.autoadda.apis.other.entity.QuotationRequestFieldValue;
import com.autoadda.apis.other.entity.VendorQuotation;
import com.autoadda.apis.other.request.BookingFromQuotationRequest;
import com.autoadda.apis.other.request.QuotationRequestSubmission;
import com.autoadda.apis.other.request.QuoteAcceptRejectRequest;
import com.autoadda.apis.other.response.OsGenericResponse;
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
    public ResponseEntity<OsGenericResponse> createRequest(
            @RequestBody QuotationRequestSubmission submission) {
        QuotationRequest request = quotationService.createRequest(submission);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(OsGenericResponse.success("Quotation request submitted", request));
    }

    /**
     * Get customer's quotation requests.
     */
    @GetMapping("/requests")
    public ResponseEntity<OsGenericResponse> getMyRequests(
            @RequestParam Integer customerId) {
        List<QuotationRequest> requests = quotationService.getCustomerRequests(customerId);
        return ResponseEntity.ok(OsGenericResponse.success(requests));
    }

    /**
     * Get customer's quotation requests with pagination.
     */
    @GetMapping("/requests/page")
    public ResponseEntity<OsGenericResponse> getMyRequestsPaginated(
            @RequestParam Integer customerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<QuotationRequest> requests = quotationService.getCustomerRequestsPaginated(customerId, page, size);
        return ResponseEntity.ok(OsGenericResponse.success(requests));
    }

    /**
     * Get quotation request details by ID.
     */
    @GetMapping("/request/{requestId}")
    public ResponseEntity<OsGenericResponse> getRequestById(@PathVariable Integer requestId) {
        QuotationRequest request = quotationService.getRequestById(requestId);
        return ResponseEntity.ok(OsGenericResponse.success(request));
    }

    /**
     * Get quotation request by quotation number.
     */
    @GetMapping("/request/number/{quotationNumber}")
    public ResponseEntity<OsGenericResponse> getRequestByNumber(@PathVariable String quotationNumber) {
        QuotationRequest request = quotationService.getRequestByNumber(quotationNumber);
        return ResponseEntity.ok(OsGenericResponse.success(request));
    }

    /**
     * Get field values for a quotation request.
     */
    @GetMapping("/request/{requestId}/fields")
    public ResponseEntity<OsGenericResponse> getRequestFieldValues(@PathVariable Integer requestId) {
        List<QuotationRequestFieldValue> fieldValues = quotationService.getRequestFieldValues(requestId);
        return ResponseEntity.ok(OsGenericResponse.success(fieldValues));
    }

    /**
     * Cancel a quotation request.
     */
    @PostMapping("/request/{requestId}/cancel")
    public ResponseEntity<OsGenericResponse> cancelRequest(
            @PathVariable Integer requestId,
            @RequestParam Integer customerId) {
        QuotationRequest cancelled = quotationService.cancelRequest(requestId, customerId);
        return ResponseEntity.ok(OsGenericResponse.success("Request cancelled", cancelled));
    }

    // ==================== View & Respond to Quotes ====================

    /**
     * Get all vendor quotations received for a request.
     */
    @GetMapping("/request/{requestId}/quotes")
    public ResponseEntity<OsGenericResponse> getQuotesForRequest(@PathVariable Integer requestId) {
        List<VendorQuotation> quotes = quotationService.getQuotationsForRequest(requestId);
        return ResponseEntity.ok(OsGenericResponse.success(quotes));
    }

    /**
     * Get vendor quotation details.
     */
    @GetMapping("/quote/{quotationId}")
    public ResponseEntity<OsGenericResponse> getQuoteDetails(@PathVariable Integer quotationId) {
        VendorQuotation quote = quotationService.getQuotationById(quotationId);
        return ResponseEntity.ok(OsGenericResponse.success(quote));
    }

    /**
     * Get line items for a vendor quotation.
     */
    @GetMapping("/quote/{quotationId}/items")
    public ResponseEntity<OsGenericResponse> getQuoteLineItems(@PathVariable Integer quotationId) {
        return ResponseEntity.ok(OsGenericResponse.success(
                quotationService.getQuotationLineItems(quotationId)));
    }

    /**
     * Accept a vendor's quotation.
     */
    @PostMapping("/quote/{quotationId}/accept")
    public ResponseEntity<OsGenericResponse> acceptQuote(
            @PathVariable Integer quotationId,
            @RequestBody QuoteAcceptRejectRequest request) {
        VendorQuotation accepted = quotationService.acceptQuote(quotationId, request.getCustomerId());
        return ResponseEntity.ok(OsGenericResponse.success("Quotation accepted", accepted));
    }

    /**
     * Reject a vendor's quotation.
     */
    @PostMapping("/quote/{quotationId}/reject")
    public ResponseEntity<OsGenericResponse> rejectQuote(
            @PathVariable Integer quotationId,
            @RequestBody QuoteAcceptRejectRequest request) {
        VendorQuotation rejected = quotationService.rejectQuote(
                quotationId, request.getCustomerId(), request.getReason());
        return ResponseEntity.ok(OsGenericResponse.success("Quotation rejected", rejected));
    }

    // ==================== Helper Endpoints ====================

    /**
     * Get quotation form fields for a service category.
     * Used by frontend to render the quotation request form.
     */
    @GetMapping("/form/category/{categoryId}")
    public ResponseEntity<OsGenericResponse> getQuotationFormFields(@PathVariable Integer categoryId) {
        List<QuotationFieldDefinition> fields = adminService.getFieldsForCategory(categoryId);
        return ResponseEntity.ok(OsGenericResponse.success(fields));
    }

    /**
     * Check if quotation is available for a category.
     */
    @GetMapping("/check/category/{categoryId}")
    public ResponseEntity<OsGenericResponse> checkQuotationAvailable(@PathVariable Integer categoryId) {
        boolean enabled = adminService.isQuotationEnabled(categoryId);
        return ResponseEntity.ok(OsGenericResponse.success(enabled));
    }

    // ==================== Booking from Quotation ====================

    /**
     * Create a booking from an accepted quotation.
     * The quotation must be in ACCEPTED status to create a booking.
     */
    @PostMapping("/quote/{quotationId}/book")
    public ResponseEntity<OsGenericResponse> createBookingFromQuote(
            @PathVariable Integer quotationId,
            @RequestBody BookingFromQuotationRequest request) {
        request.setVendorQuotationId(quotationId);
        Booking booking = bookingService.createBookingFromQuotation(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(OsGenericResponse.success("Booking created from quotation", booking));
    }
}
