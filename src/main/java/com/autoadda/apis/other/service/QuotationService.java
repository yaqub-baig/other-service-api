package com.autoadda.apis.other.service;

import com.autoadda.apis.other.validation.AttributeValidator;
import com.autoadda.apis.other.validation.FieldSpecFactory;
import com.autoadda.apis.other.entity.*;
import com.autoadda.apis.other.exception.BusinessException;
import com.autoadda.apis.other.exception.ResourceNotFoundException;
import com.autoadda.apis.other.mapper.QuotationRequestFieldValueMapper;
import com.autoadda.apis.other.mapper.QuotationRequestMapper;
import com.autoadda.apis.other.mapper.VendorQuotationLineItemMapper;
import com.autoadda.apis.other.mapper.VendorQuotationMapper;
import com.autoadda.apis.other.repository.*;
import com.autoadda.apis.other.request.QuotationRequestSubmission;
import com.autoadda.apis.other.request.VendorQuotationSubmission;
import com.autoadda.apis.other.response.QuotationRequestFieldValueResponse;
import com.autoadda.apis.other.response.QuotationRequestResponse;
import com.autoadda.apis.other.response.VendorQuotationLineItemResponse;
import com.autoadda.apis.other.response.VendorQuotationResponse;
import com.autoadda.apis.other.util.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Main service for quotation workflows.
 * Handles customer request creation, vendor quotation submission, and acceptance flows.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class QuotationService {

    private final QuotationRequestRepository requestRepository;
    private final QuotationRequestFieldValueRepository fieldValueRepository;
    private final VendorQuotationRepository vendorQuotationRepository;
    private final VendorQuotationLineItemRepository lineItemRepository;
    private final QuotationScopeDefinitionRepository scopeRepository;
    private final QuotationFieldDefinitionRepository fieldDefinitionRepository;
    private final ServiceCategoryRepository categoryRepository;
    private final VendorRepository vendorRepository;
    private final ObjectMapper objectMapper;
    private final QuotationRequestMapper quotationRequestMapper; // Inject mapper
    private final QuotationRequestFieldValueMapper quotationRequestFieldValueMapper; // Inject mapper
    private final VendorQuotationMapper vendorQuotationMapper; // Inject mapper
    private final VendorQuotationLineItemMapper vendorQuotationLineItemMapper; // Inject mapper
    private final QuotationAdminService quotationAdminService;
    private final ServiceCatalogService serviceCatalogService;
    private final AttributeValidator attributeValidator;
    private final FieldSpecFactory fieldSpecFactory;

    // ==================== Customer Request Operations ====================

    /**
     * Customer creates a new quotation request.
     */
    @Transactional
    public QuotationRequestResponse createRequest(QuotationRequestSubmission submission) {
        // Validate category
        ServiceCategory category = categoryRepository.findById(submission.getServiceCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Service category not found with id: " + submission.getServiceCategoryId()));

        CatalogService catalogService = submission.getServiceId() != null
                ? serviceCatalogService.getForCategory(submission.getServiceId(), submission.getServiceCategoryId())
                : null;

        // Service-level scope wins over the category default
        QuotationScopeDefinition scope = quotationAdminService.resolveScope(submission.getServiceCategoryId(), submission.getServiceId())
                .orElseThrow(() -> new BusinessException("Quotation is not configured for this service category"));

        if (!scope.getIsQuotationEnabled()) {
            throw new BusinessException("Quotation is not enabled for this service category");
        }

        // Validate required fields
        List<QuotationFieldDefinition> formFields =
                quotationAdminService.resolveFields(submission.getServiceCategoryId(), submission.getServiceId());
        attributeValidator.validateOrThrow(fieldSpecFactory.forQuotation(formFields), submission.getFieldValues());

        // Generate unique quotation number
        String quotationNumber = generateQuotationNumber();

        // Calculate expiry time
        LocalDateTime expiresAt = LocalDateTime.now().plusHours(scope.getAutoExpiryHours());

        // Create the request
        QuotationRequest request = QuotationRequest.builder()
                .quotationNumber(quotationNumber)
                .serviceCategory(category)
                .catalogService(catalogService)
                .customerId(submission.getCustomerId())
                .serviceLocationType(submission.getServiceLocationType())
                .city(submission.getCity())
                .area(submission.getArea())
                .address(submission.getAddress())
                .latitude(submission.getLatitude())
                .longitude(submission.getLongitude())
                .customerNotes(submission.getCustomerNotes())
                .status(QuotationRequestStatus.REQUESTED)
                .expiresAt(expiresAt)
                .build();

        QuotationRequest savedRequest = requestRepository.save(request);

        // Save field values
        if (submission.getFieldValues() != null) {
            for (Map.Entry<String, Object> entry : submission.getFieldValues().entrySet()) {
                QuotationFieldDefinition fieldDef = formFields.stream()
                        .filter(f -> f.getFieldCode().equals(entry.getKey()))
                        .findFirst()
                        .orElse(null);

                QuotationRequestFieldValue fieldValue = QuotationRequestFieldValue.builder()
                        .quotationRequest(savedRequest)
                        .fieldCode(entry.getKey())
                        .fieldLabel(fieldDef != null ? fieldDef.getLabel() : entry.getKey())
                        .fieldValue(convertToJson(entry.getValue()))
                        .build();

                fieldValueRepository.save(fieldValue);
            }
        }

        log.info("Created quotation request: {} for customer: {}", quotationNumber, submission.getCustomerId());
        return quotationRequestMapper.toResponse(savedRequest);
    }

    /**
     * Get customer's quotation requests.
     */
    @Transactional(readOnly = true)
    public List<QuotationRequestResponse> getCustomerRequests(Integer customerId) {
        return quotationRequestMapper.toResponseList(requestRepository.findByCustomerIdOrderByCreatedAtDesc(customerId));
    }

    /**
     * Get customer's quotation requests with pagination.
     */
    @Transactional(readOnly = true)
    public Page<QuotationRequestResponse> getCustomerRequestsPaginated(Integer customerId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return requestRepository.findByCustomerId(customerId, pageable)
                .map(quotationRequestMapper::toResponse);
    }

    /**
     * Get quotation request by ID.
     */
    @Transactional(readOnly = true)
    public QuotationRequestResponse getRequestById(Integer requestId) {
        return requestRepository.findById(requestId)
                .map(quotationRequestMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Quotation request not found with id: " + requestId));
    }
    public QuotationRequest getRequestById1(Integer requestId) {
        return requestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Quotation request not found with id: " + requestId));
    }
    /**
     * Get quotation request by quotation number.
     */
    @Transactional(readOnly = true)
    public QuotationRequestResponse getRequestByNumber(String quotationNumber) {
        return requestRepository.findByQuotationNumber(quotationNumber)
                .map(quotationRequestMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Quotation request not found with number: " + quotationNumber));
    }

    /**
     * Get field values for a quotation request.
     */
    @Transactional(readOnly = true)
    public List<QuotationRequestFieldValueResponse> getRequestFieldValues(Integer requestId) {
        return quotationRequestFieldValueMapper.toResponseList(fieldValueRepository.findByQuotationRequestId(requestId));
    }

    /**
     * Customer cancels their quotation request.
     */
    @Transactional
    public QuotationRequestResponse cancelRequest(Integer requestId, Integer customerId) {
        QuotationRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Quotation request not found with id: " + requestId));

        if (!request.getCustomerId().equals(customerId)) {
            throw new BusinessException("You can only cancel your own requests");
        }

        if (request.getStatus() == QuotationRequestStatus.ACCEPTED ||
            request.getStatus() == QuotationRequestStatus.CANCELLED ||
            request.getStatus() == QuotationRequestStatus.EXPIRED) {
            throw new BusinessException("Cannot cancel a request with status: " + request.getStatus());
        }

        request.setStatus(QuotationRequestStatus.CANCELLED);
        log.info("Customer {} cancelled quotation request: {}", customerId, requestId);
        QuotationRequest savedRequest = requestRepository.save(request);
        return quotationRequestMapper.toResponse(savedRequest);
    }

    // ==================== Vendor Quotation Operations ====================

    /**
     * Find open quotation requests for vendors (based on category and location).
     */
    @Transactional(readOnly = true)
    public List<QuotationRequestResponse> findOpenRequestsForVendor(Integer vendorId, Integer categoryId) {
        List<QuotationRequest> requests = requestRepository.findOpenRequestsByCategory(
                categoryId,
                QuotationRequestStatus.SENT_TO_VENDORS,
                LocalDateTime.now());
        return quotationRequestMapper.toResponseList(requests);
    }

    /**
     * Vendor submits a quotation for a request.
     */
    @Transactional
    public VendorQuotationResponse submitQuote(VendorQuotationSubmission submission) {
        // Validate request exists and is open
        QuotationRequest request = getRequestById1(submission.getQuotationRequestId());

        if (request.getStatus() != QuotationRequestStatus.REQUESTED &&
            request.getStatus() != QuotationRequestStatus.SENT_TO_VENDORS &&
            request.getStatus() != QuotationRequestStatus.QUOTES_RECEIVED) {
            throw new BusinessException("Cannot submit quote for request with status: " + request.getStatus());
        }

        if (request.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BusinessException("This quotation request has expired");
        }

        // Validate vendor exists
        Vendor vendor = vendorRepository.findById(submission.getVendorId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Vendor not found with id: " + submission.getVendorId()));

        // Check if vendor already submitted a quote
        if (vendorQuotationRepository.existsByQuotationRequestIdAndVendorId(
                submission.getQuotationRequestId(), submission.getVendorId())) {
            throw new BusinessException("You have already submitted a quote for this request");
        }

        // Generate quote number
        String quoteNumber = generateQuoteNumber();

        // Create vendor quotation
        VendorQuotation quote = VendorQuotation.builder()
                .quoteNumber(quoteNumber)
                .quotationRequest(request)
                .vendor(vendor)
                .pricingType(submission.getPricingType())
                .totalAmount(submission.getTotalAmount())
                .minAmount(submission.getMinAmount())
                .maxAmount(submission.getMaxAmount())
                .inspectionFee(submission.getInspectionFee())
                .estimatedDuration(submission.getEstimatedDuration())
                .warrantyInfo(submission.getWarrantyInfo())
                .validUntil(submission.getValidUntil() != null ? submission.getValidUntil() : LocalDate.now().plusDays(7))
                .vendorNotes(submission.getVendorNotes())
                .status(VendorQuotationStatus.SENT)
                .build();

        VendorQuotation savedQuote = vendorQuotationRepository.save(quote);

        // Save line items if provided
        if (submission.getLineItems() != null) {
            int order = 0;
            for (VendorQuotationSubmission.LineItemSubmission item : submission.getLineItems()) {
                VendorQuotationLineItem lineItem = VendorQuotationLineItem.builder()
                        .vendorQuotation(savedQuote)
                        .itemType(item.getItemType())
                        .title(item.getTitle())
                        .description(item.getDescription())
                        .quantity(item.getQuantity() != null ? item.getQuantity() : 1)
                        .unit(item.getUnit())
                        .unitPrice(item.getUnitPrice())
                        .discount(item.getDiscount())
                        .taxRate(item.getTaxRate())
                        .displayOrder(order++)
                        .isOptional(item.getIsOptional() != null ? item.getIsOptional() : false)
                        .build();

                lineItem.calculateTotalPrice();
                lineItemRepository.save(lineItem);
            }
        }

        // Update request status and quote count
        request.setQuotesReceivedCount(request.getQuotesReceivedCount() + 1);
        if (request.getStatus() == QuotationRequestStatus.REQUESTED ||
            request.getStatus() == QuotationRequestStatus.SENT_TO_VENDORS) {
            request.setStatus(QuotationRequestStatus.QUOTES_RECEIVED);
        }
        requestRepository.save(request);

        log.info("Vendor {} submitted quote {} for request {}",
                submission.getVendorId(), quoteNumber, submission.getQuotationRequestId());
        return vendorQuotationMapper.toResponse(savedQuote);
    }

    /**
     * Get vendor's submitted quotations.
     */
    @Transactional(readOnly = true)
    public List<VendorQuotationResponse> getVendorQuotations(Integer vendorId) {
        return vendorQuotationMapper.toResponseList(vendorQuotationRepository.findByVendorIdOrderByCreatedAtDesc(vendorId));
    }

    /**
     * Get vendor's quotations with pagination.
     */
    @Transactional(readOnly = true)
    public Page<VendorQuotationResponse> getVendorQuotationsPaginated(Integer vendorId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return vendorQuotationRepository.findByVendorId(vendorId, pageable)
                .map(vendorQuotationMapper::toResponse);
    }

    /**
     * Get quotations received for a request.
     */
    @Transactional(readOnly = true)
    public List<VendorQuotationResponse> getQuotationsForRequest(Integer requestId) {
        return vendorQuotationMapper.toResponseList(vendorQuotationRepository.findByQuotationRequestIdOrderByCreatedAtDesc(requestId));
    }

    /**
     * Get vendor quotation by ID.
     */
    @Transactional(readOnly = true)
    public VendorQuotationResponse getQuotationById(Integer quotationId) {
        return vendorQuotationRepository.findById(quotationId)
                .map(vendorQuotationMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Vendor quotation not found with id: " + quotationId));
    }

    /**
     * Get line items for a vendor quotation.
     */
    @Transactional(readOnly = true)
    public List<VendorQuotationLineItemResponse> getQuotationLineItems(Integer quotationId) {
        return vendorQuotationLineItemMapper.toResponseList(lineItemRepository.findByVendorQuotationIdOrderByDisplayOrderAsc(quotationId));
    }

    /**
     * Vendor withdraws their quotation.
     */
    @Transactional
    public VendorQuotationResponse withdrawQuote(Integer quotationId, Integer vendorId) {
        VendorQuotation quote = vendorQuotationRepository.findById(quotationId)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor quotation not found with id: " + quotationId));

        if (!quote.getVendor().getId().equals(vendorId)) {
            throw new BusinessException("You can only withdraw your own quotations");
        }

        if (quote.getStatus() != VendorQuotationStatus.SENT &&
            quote.getStatus() != VendorQuotationStatus.DRAFT) {
            throw new BusinessException("Cannot withdraw quotation with status: " + quote.getStatus());
        }

        quote.setStatus(VendorQuotationStatus.WITHDRAWN);
        log.info("Vendor {} withdrew quotation: {}", vendorId, quotationId);
        VendorQuotation savedQuote = vendorQuotationRepository.save(quote);
        return vendorQuotationMapper.toResponse(savedQuote);
    }

    // ==================== Quote Acceptance/Rejection ====================

    /**
     * Customer accepts a vendor's quotation.
     */
    @Transactional
    public VendorQuotationResponse acceptQuote(Integer quotationId, Integer customerId) {
        VendorQuotation quote = vendorQuotationRepository.findById(quotationId)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor quotation not found with id: " + quotationId));
        QuotationRequest request = quote.getQuotationRequest();

        // Validate customer owns the request
        if (!request.getCustomerId().equals(customerId)) {
            throw new BusinessException("You can only accept quotes for your own requests");
        }

        // Validate quote is still valid
        if (quote.getStatus() != VendorQuotationStatus.SENT) {
            throw new BusinessException("Cannot accept quotation with status: " + quote.getStatus());
        }

        if (quote.getValidUntil() != null && quote.getValidUntil().isBefore(LocalDate.now())) {
            throw new BusinessException("This quotation has expired");
        }

        // Accept this quote
        quote.setStatus(VendorQuotationStatus.ACCEPTED);
        quote.setRespondedAt(LocalDateTime.now());
        vendorQuotationRepository.save(quote);

        // Reject all other quotes for this request
        List<VendorQuotation> otherQuotes = vendorQuotationRepository
                .findByQuotationRequestIdAndStatusOrderByCreatedAtDesc(request.getId(), VendorQuotationStatus.SENT);
        for (VendorQuotation otherQuote : otherQuotes) {
            if (!otherQuote.getId().equals(quotationId)) {
                otherQuote.setStatus(VendorQuotationStatus.REJECTED);
                otherQuote.setRejectionReason("Another quote was accepted");
                otherQuote.setRespondedAt(LocalDateTime.now());
                vendorQuotationRepository.save(otherQuote);
            }
        }

        // Update request status
        request.setStatus(QuotationRequestStatus.ACCEPTED);
        requestRepository.save(request);

        log.info("Customer {} accepted quotation {} for request {}",
                customerId, quotationId, request.getId());
        return vendorQuotationMapper.toResponse(quote);
    }

    /**
     * Customer rejects a vendor's quotation.
     */
    @Transactional
    public VendorQuotationResponse rejectQuote(Integer quotationId, Integer customerId, String reason) {
        VendorQuotation quote = vendorQuotationRepository.findById(quotationId)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor quotation not found with id: " + quotationId));
        QuotationRequest request = quote.getQuotationRequest();

        // Validate customer owns the request
        if (!request.getCustomerId().equals(customerId)) {
            throw new BusinessException("You can only reject quotes for your own requests");
        }

        if (quote.getStatus() != VendorQuotationStatus.SENT) {
            throw new BusinessException("Cannot reject quotation with status: " + quote.getStatus());
        }

        quote.setStatus(VendorQuotationStatus.REJECTED);
        quote.setRejectionReason(reason);
        quote.setRespondedAt(LocalDateTime.now());

        log.info("Customer {} rejected quotation {} for request {}",
                customerId, quotationId, request.getId());
        VendorQuotation savedQuote = vendorQuotationRepository.save(quote);
        return vendorQuotationMapper.toResponse(savedQuote);
    }

    // ==================== Broadcast & Matching ====================

    /**
     * Broadcast a quotation request to matching vendors.
     * This would typically be called by a scheduled job or immediately after request creation.
     */
    @Transactional
    public int broadcastToVendors(Integer requestId) {
        QuotationRequest request = getRequestById1(requestId);

        if (request.getStatus() != QuotationRequestStatus.REQUESTED) {
            throw new BusinessException("Can only broadcast requests with REQUESTED status");
        }

        QuotationScopeDefinition scope = quotationAdminService
                .resolveScope(request.getServiceCategory().getId(),
                        request.getCatalogService() != null ? request.getCatalogService().getId() : null)
                .orElseThrow(() -> new BusinessException("Quotation scope not configured"));

        // Find matching vendors
        // In a real implementation, this would use location-based queries
        List<Vendor> matchingVendors = findMatchingVendors(
                request.getServiceCategory().getId(),
                request.getCity(),
                request.getLatitude(),
                request.getLongitude(),
                scope.getMaxBroadcastVendors());

        // Update request status
        request.setStatus(QuotationRequestStatus.SENT_TO_VENDORS);
        request.setBroadcastCount(matchingVendors.size());
        requestRepository.save(request);

        // In a real implementation, send notifications to vendors here
        log.info("Broadcast request {} to {} vendors", requestId, matchingVendors.size());
        return matchingVendors.size();
    }

    // ==================== Expiry Processing ====================

    /**
     * Process expired quotation requests (scheduled job).
     */
    @Transactional
    public int processExpiredRequests() {
        List<QuotationRequestStatus> activeStatuses = Arrays.asList(
                QuotationRequestStatus.REQUESTED,
                QuotationRequestStatus.SENT_TO_VENDORS,
                QuotationRequestStatus.QUOTES_RECEIVED);

        List<QuotationRequest> expiredRequests = requestRepository
                .findExpiredRequests(activeStatuses, LocalDateTime.now());

        for (QuotationRequest request : expiredRequests) {
            request.setStatus(QuotationRequestStatus.EXPIRED);
            requestRepository.save(request);

            // Expire pending vendor quotations
            List<VendorQuotation> pendingQuotes = vendorQuotationRepository
                    .findByQuotationRequestIdAndStatusOrderByCreatedAtDesc(
                            request.getId(), VendorQuotationStatus.SENT);
            for (VendorQuotation quote : pendingQuotes) {
                quote.setStatus(VendorQuotationStatus.EXPIRED);
                vendorQuotationRepository.save(quote);
            }
        }

        log.info("Processed {} expired quotation requests", expiredRequests.size());
        return expiredRequests.size();
    }

    /**
     * Process expired vendor quotations (scheduled job).
     */
    @Transactional
    public int processExpiredQuotations() {
        List<VendorQuotation> expiredQuotes = vendorQuotationRepository
                .findExpiredQuotations(VendorQuotationStatus.SENT, LocalDate.now());

        for (VendorQuotation quote : expiredQuotes) {
            quote.setStatus(VendorQuotationStatus.EXPIRED);
            vendorQuotationRepository.save(quote);
        }

        log.info("Processed {} expired vendor quotations", expiredQuotes.size());
        return expiredQuotes.size();
    }

    // ==================== Helper Methods ====================

    private List<Vendor> findMatchingVendors(Integer categoryId, String city, Double lat, Double lng, Integer maxVendors) {
        // Simplified vendor matching - in production, use proper geo-spatial queries
        // This would typically query vendors that:
        // 1. Offer services in the specified category
        // 2. Are located in or near the specified city/coordinates
        // 3. Are active and have quotation enabled
        return vendorRepository.findAll().stream()
                .filter(v -> v.isActive())
                .limit(maxVendors != null ? maxVendors : 10)
                .toList();
    }

    private String generateQuotationNumber() {
        String prefix = "QR";
        String timestamp = String.valueOf(System.currentTimeMillis()).substring(5);
        String random = String.format("%04d", new Random().nextInt(10000));
        return prefix + timestamp + random;
    }

    private String generateQuoteNumber() {
        String prefix = "VQ";
        String timestamp = String.valueOf(System.currentTimeMillis()).substring(5);
        String random = String.format("%04d", new Random().nextInt(10000));
        return prefix + timestamp + random;
    }

    private String convertToJson(Object obj) {
        if (obj == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to convert to JSON", e);
        }
    }
}
