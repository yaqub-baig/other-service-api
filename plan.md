# Implementation Plan: Dynamic Quotation Scope

This document outlines the phased implementation plan for introducing a "Scope of Quotation" feature into the `other-service-api`.

The goal is to allow administrators to dynamically define a set of fields for any service category that uses a `QUOTE_BASED` pricing model. This creates a structured form for customers to fill out when requesting a quote, and for vendors to respond to.

The implementation is divided into four main phases:
1.  **Phase 1: Foundational Enhancements.** Introduce the concepts of dynamic pricing models and specific service offerings, which are prerequisites for the quotation feature.
2.  **Phase 2: Quotation Scope Configuration.** Build the entities and APIs for administrators to configure the quotation forms.
3.  **Phase 3: Quotation Workflow Management.** Build the entities and APIs for the end-to-end quotation process between customers and vendors.
4.  **Phase 4: API Integration and Documentation.** Integrate the new services and expose them via new controller endpoints.

---

## Phase 1: Foundational Enhancements (Prerequisites)

The current entity structure is static. We must first introduce dynamic pricing and the concept of a `ServiceOffering` to align with the target architecture.

### 1.1. Create PricingType Enum

-   **Action:** Create a new Enum `PricingType`.
-   **File:** `src/main/java/com/autoadda/apis/other/util/PricingType.java`
-   **Content:**
    ```java
    public enum PricingType {
        FIXED,      // A single, fixed price
        RANGE,      // A price range (min/max)
        QUOTE_BASED // Requires a customer quotation
    }
    ```

### 1.2. Enhance `OsService` for Pricing

-   **Action:** Modify the `OsService` entity to include pricing information. This entity represents a vendor's capability to offer a certain type of service.
-   **File:** `src/main/java/com/autoadda/apis/other/entity/OsService.java`
-   **Modifications:**
    -   Add `pricingType` field.
    -   Add fields for different pricing models.

    ```java
    // ... existing fields ...

    @Enumerated(EnumType.STRING)
    @Column(name = "pricing_type", nullable = false)
    private PricingType pricingType;

    // For FIXED price
    @Column(name = "price")
    private Double price;

    // For RANGE price
    @Column(name = "min_price")
    private Double minPrice;

    @Column(name = "max_price")
    private Double maxPrice;
    ```

### 1.3. Create `ServiceOffering` Entity (Crucial Step)

-   **Action:** The current model lacks a specific, sellable "offering". We will create a new `ServiceOffering` entity to represent a concrete product or service a vendor sells. This is where dynamic, filterable attributes will be stored.
-   **File:** `src/main/java/com/autoadda/apis/other/entity/ServiceOffering.java`
-   **Content:**
    ```java
    @Entity
    @Getter
    @Setter
    public class ServiceOffering {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Integer id;

        @ManyToOne(optional = false)
        @JoinColumn(name = "os_service_id")
        private OsService osService; // Links to the vendor's service capability

        @Column(name = "offering_name", nullable = false)
        private String offeringName;

        @Column(name = "description", columnDefinition = "TEXT")
        private String description;
        
        @Column(name = "sku")
        private String sku;

        // Store dynamic filterable attributes as JSON
        @Column(name = "filterable_attributes", columnDefinition = "jsonb")
        private String filterableAttributes; // Store as JSON string
    }
    ```
-   **Repository:** Create `ServiceOfferingRepository.java`.

---

## Phase 2: Quotation Scope Configuration

### 2.1. Create `QuotationScopeField` Entity

-   **Action:** Create an entity to define a single field within a quotation request form. These fields are defined per `OsAvailableService` (Service Category).
-   **File:** `src/main/java/com/autoadda/apis/other/entity/QuotationScopeField.java`
-   **Content:**
    ```java
    @Entity
    @Getter
    @Setter
    public class QuotationScopeField {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Integer id;

        @ManyToOne(optional = false)
        @JoinColumn(name = "os_available_service_id")
        private OsAvailableService osAvailableService;

        @Column(name = "field_code", nullable = false)
        private String fieldCode; // e.g., "panel_type", "damage_severity"

        @Column(name = "field_label", nullable = false)
        private String fieldLabel; // e.g., "Panel Type", "Severity of Damage"

        @Column(name = "field_type", nullable = false)
        private String fieldType; // e.g., "TEXT", "DROPDOWN", "TEXTAREA", "NUMBER"

        @Column(name = "options", columnDefinition = "jsonb")
        private String options; // JSON for dropdown options

        @Column(name = "is_required", nullable = false)
        private boolean isRequired = true;
    }
    ```
-   **Repository:** Create `QuotationScopeFieldRepository.java`.

### 2.2. Create `QuotationScopeService`

-   **Action:** Create a service layer for managing `QuotationScopeField` entities. This will be used by an admin controller.
-   **File:** `src/main/java/com/autoadda/apis/other/service/QuotationScopeService.java`
-   **Methods:**
    -   `getScopeForServiceCategory(Integer categoryId)`: Returns all scope fields for a category.
    -   `createScopeField(Integer categoryId, QuotationScopeFieldRequest request)`: Adds a new field.
    -   `updateScopeField(Integer fieldId, QuotationScopeFieldRequest request)`: Updates an existing field.
    -   `deleteScopeField(Integer fieldId)`: Removes a field.

---

## Phase 3: Quotation Workflow Management

### 3.1. Create `QuotationRequest` and `QuotationResponse` Entities

-   **Action:** Create entities to manage the full lifecycle of a quotation.
-   **File:** `src/main/java/com/autoadda/apis/other/entity/QuotationRequest.java`
    ```java
    @Entity
    public class QuotationRequest {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Integer id;
        
        @Column(name = "customer_id", nullable = false)
        private Integer customerId;

        @ManyToOne(optional = false)
        @JoinColumn(name = "service_offering_id")
        private ServiceOffering serviceOffering;

        // Customer's input based on QuotationScopeFields
        @Column(name = "request_details", columnDefinition = "jsonb")
        private String requestDetails;

        @Column(name = "status") // e.g., PENDING, SUBMITTED, EXPIRED
        private String status;
        
        @CreationTimestamp
        private LocalDateTime createdAt;
    }
    ```
-   **File:** `src/main/java/com/autoadda/apis/other/entity/QuotationResponse.java`
    ```java
    @Entity
    public class QuotationResponse {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Integer id;

        @OneToOne(optional = false)
        @JoinColumn(name = "quotation_request_id")
        private QuotationRequest quotationRequest;

        // Vendor's detailed quote
        @Column(name = "line_items", columnDefinition = "jsonb")
        private String lineItems; // e.g., "[{'item': 'Part A', 'cost': 100}, ...]"

        @Column(name = "total_amount", nullable = false)
        private Double totalAmount;
        
        @Column(name = "valid_until")
        private LocalDate validUntil;

        @Column(name = "status") // e.g., DRAFT, SENT, ACCEPTED, REJECTED
        private String status;

        @CreationTimestamp
        private LocalDateTime createdAt;
    }
    ```
-   **Repositories:** Create `QuotationRequestRepository.java` and `QuotationResponseRepository.java`.

### 3.2. Create `QuotationWorkflowService`

-   **Action:** Create a service to handle the business logic of the quotation process.
-   **File:** `src/main/java/com/autoadda/apis/other/service/QuotationWorkflowService.java`
-   **Methods:**
    -   `requestQuote(Integer customerId, Integer offeringId, String requestDetails)`
    -   `getOpenRequestsForVendor(Integer vendorId)`
    -   `submitQuote(Integer vendorId, Integer requestId, String lineItems, Double totalAmount)`
    -   `getQuotesForCustomer(Integer customerId)`
    -   `acceptQuote(Integer customerId, Integer quoteId)`: This could trigger the creation of an `OsAppointmentDetails`.
    -   `rejectQuote(Integer customerId, Integer quoteId)`

---

## Phase 4: API Integration

### 4.1. Create `QuotationAdminController`

-   **Action:** Expose CRUD operations for `QuotationScopeField`.
-   **Base Path:** `/admin/quotation-scopes`
-   **Endpoints:**
    -   `GET /category/{categoryId}`: Get all scope fields for a service category.
    -   `POST /category/{categoryId}`: Create a new scope field for a category.
    -   `PUT /{fieldId}`: Update a scope field.
    -   `DELETE /{fieldId}`: Delete a scope field.

### 4.2. Create `QuotationController`

-   **Action:** Expose endpoints for the customer and vendor quotation workflow.
-   **Base Path:** `/api/quotations`
-   **Endpoints:**
    -   **Customer:**
        -   `POST /request`: Customer submits a new quote request.
        -   `GET /my-quotes`: Customer views all their active and past quotes.
        -   `POST /{quoteId}/accept`: Customer accepts a vendor's quote.
        -   `POST /{quoteId}/reject`: Customer rejects a vendor's quote.
    -   **Vendor:**
        -   `GET /vendor/requests`: Vendor views all open quote requests.
        -   `GET /vendor/request/{requestId}`: Vendor views the details of a single request.
        -   `POST /vendor/submit`: Vendor submits their priced quote.

This plan provides a clear path to delivering the requested functionality while also strengthening the application's core architecture to be more dynamic and scalable.
