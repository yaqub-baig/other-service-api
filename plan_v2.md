# Implementation Plan v2.0: Advanced Quotation Management

This document outlines the phased implementation plan for introducing an advanced, dynamic **Quotation Management** module. This plan is based on the comprehensive "Module #11" design, which provides exceptional flexibility and aligns perfectly with the project's core architecture.

The implementation is divided into four main phases:
1.  **Phase 1: Core Quotation Configuration.** Build the foundational entities and services for administrators to define how quotations work for each service category.
2.  **Phase 2: Customer Request Workflow.** Implement the entities and APIs for a customer to submit a detailed, dynamic quotation request.
3.  **Phase 3: Vendor Response Workflow.** Implement the entities and APIs for vendors to receive requests and submit detailed, structured quotations.
4.  **Phase 4: API Integration & Booking Conversion.** Expose all functionality through new API endpoints and implement the logic to convert an accepted quote into a formal booking.

---

## Phase 1: Core Quotation Configuration (Admin)

### 1.1. Create `QuotationScopeDefinition` Entity & Enum

-   **Action:** Create an entity to define the high-level quotation strategy for a service category.
-   **File:** `src/main/java/com/autoadda/apis/other/entity/QuotationScopeDefinition.java`
-   **Content:**
    ```java
    @Entity
    @Getter
    @Setter
    public class QuotationScopeDefinition {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Integer id;

        @OneToOne(optional = false)
        @JoinColumn(name = "os_available_service_id")
        private OsAvailableService osAvailableService; // Maps to ServiceCategory

        @Column(name = "is_quotation_enabled", nullable = false)
        private boolean isQuotationEnabled = false;

        @Enumerated(EnumType.STRING)
        @Column(name = "quotation_type", nullable = false)
        private QuotationType quotationType; // Use Enum below

        @Column(name = "auto_expiry_hours")
        private Integer autoExpiryHours; // e.g., 48 hours

        @Column(name = "allow_multi_vendor_broadcast", nullable = false)
        private boolean allowMultiVendorBroadcast = true;
    }
    ```
-   **New Enum:** `src/main/java/com/autoadda/apis/other/util/QuotationType.java`
    ```java
    public enum QuotationType {
        OPTIONAL,  // Vendor can have a fixed price listing AND customers can optionally request a quote.
        MANDATORY, // No price is shown; a quote is required to get a price.
        HYBRID     // A base price is shown, but customers can request a custom quote for additional work.
    }
    ```
-   **Repository:** Create `QuotationScopeDefinitionRepository.java`.

### 1.2. Create `QuotationFieldDefinition` Entity & Enums

-   **Action:** Create an entity to define each dynamic field in a quotation request form.
-   **File:** `src/main/java/com/autoadda/apis/other/entity/QuotationFieldDefinition.java`
-   **Content:**
    ```java
    @Entity
    @Getter
    @Setter
    public class QuotationFieldDefinition {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Integer id;

        @ManyToOne(optional = false)
        @JoinColumn(name = "os_available_service_id")
        private OsAvailableService osAvailableService;

        @Column(nullable = false)
        private String fieldCode;

        @Column(nullable = false)
        private String label;

        @Enumerated(EnumType.STRING)
        @Column(nullable = false)
        private FieldType fieldType;

        @Column(nullable = false)
        private boolean isRequired = true;

        @Column
        private Integer displayOrder;
        
        // For dependent fields (e.g., Model depends on Brand)
        @ManyToOne
        @JoinColumn(name = "depends_on_field_id")
        private QuotationFieldDefinition dependsOn;
    }
    ```
-   **New Enum:** `src/main/java/com/autoadda/apis/other/util/FieldType.java`
    ```java
    public enum FieldType {
        TEXT, NUMBER, DROPDOWN, MULTI_SELECT, BOOLEAN, FILE_UPLOAD, DATE, TEXTAREA
    }
    ```
-   **Repository:** Create `QuotationFieldDefinitionRepository.java`.

### 1.3. Create `QuotationAdminService`

-   **Action:** Create a service for admin-level operations on quotation configurations.
-   **File:** `src/main/java/com/autoadda/apis/other/service/QuotationAdminService.java`
-   **Methods:**
    -   `saveScopeDefinition(ScopeRequest request)`
    -   `getScopeDefinitionForCategory(Integer categoryId)`
    -   `createFieldDefinition(FieldRequest request)`
    -   `updateFieldDefinition(Integer fieldId, FieldRequest request)`
    -   `deleteFieldDefinition(Integer fieldId)`
    -   `getFieldsForCategory(Integer categoryId)`

---

## Phase 2: Customer Request Workflow

### 2.1. Create `QuotationRequest` & `QuotationRequestFieldValue` Entities

-   **Action:** Create entities to store the customer's submitted request and its dynamic values.
-   **File:** `src/main/java/com/autoadda/apis/other/entity/QuotationRequest.java`
    ```java
    @Entity
    public class QuotationRequest {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Integer id;

        @Column(unique = true, nullable = false)
        private String quotationNumber;

        @ManyToOne(optional = false)
        @JoinColumn(name = "os_available_service_id")
        private OsAvailableService osAvailableService;

        @Column(nullable = false)
        private Integer customerId;
        
        // Location details for geo-search
        private String city;
        private String area;
        // private Point location; // Requires PostGIS dependency

        @Enumerated(EnumType.STRING)
        private ServiceLocationType serviceLocationType; // AT_VENDOR, AT_CUSTOMER

        @Enumerated(EnumType.STRING)
        private QuotationRequestStatus status; // REQUESTED, SENT_TO_VENDORS, EXPIRED

        private LocalDateTime expiresAt;
        
        @CreationTimestamp
        private LocalDateTime createdAt;
    }
    ```
-   **File:** `src/main/java/com/autoadda/apis/other/entity/QuotationRequestFieldValue.java`
    ```java
    @Entity
    public class QuotationRequestFieldValue {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Integer id;

        @ManyToOne(optional = false)
        @JoinColumn(name = "quotation_request_id")
        private QuotationRequest quotationRequest;
        
        @Column(nullable = false)
        private String fieldCode;

        // Use JSONB to store any type of value (text, number, array for multi-select, file URL)
        @Column(columnDefinition = "jsonb")
        private String value;
    }
    ```
-   **Repositories:** Create repositories for both entities.

---

## Phase 3: Vendor Response Workflow

### 3.1. Create `VendorQuotation` and `VendorQuotationLineItem` Entities

-   **Action:** Create entities for the vendor's detailed response.
-   **File:** `src/main/java/com/autoadda/apis/other/entity/VendorQuotation.java`
    ```java
    @Entity
    public class VendorQuotation {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Integer id;
        
        @ManyToOne(optional = false)
        @JoinColumn(name = "quotation_request_id")
        private QuotationRequest quotationRequest;
        
        @ManyToOne(optional = false)
        @JoinColumn(name = "vendor_id") // This should be OtherService
        private OtherService vendor;
        
        @Enumerated(EnumType.STRING)
        private VendorPricingType pricingType; // FIXED, RANGE, BREAKUP, INSPECTION_REQUIRED
        
        private Double totalAmount;
        private Double minAmount;
        private Double maxAmount;
        private Double inspectionFee;
        private String estimatedDuration; // e.g., "2-4 hours"
        private LocalDate validUntil;
        
        @Enumerated(EnumType.STRING)
        private VendorQuotationStatus status; // SENT, ACCEPTED, REJECTED
        
        @OneToMany(mappedBy = "vendorQuotation", cascade = CascadeType.ALL, orphanRemoval = true)
        private List<VendorQuotationLineItem> lineItems = new ArrayList<>();
    }
    ```
-   **File:** `src/main/java/com/autoadda/apis/other/entity/VendorQuotationLineItem.java`
    ```java
    @Entity
    public class VendorQuotationLineItem {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Integer id;

        @ManyToOne(optional = false)
        @JoinColumn(name = "vendor_quotation_id")
        private VendorQuotation vendorQuotation;

        private String title;
        private String description;
        private Integer quantity;
        private Double unitPrice;
        private Double totalPrice;
    }
    ```
-   **Repositories:** Create repositories for both entities.

---

## Phase 4: API Integration & Booking Conversion

### 4.1. Create `QuotationService` and Controllers

-   **Action:** Create a primary service for the workflow and expose it via controllers.
-   **Service:** `src/main/java/com/autoadda/apis/other/service/QuotationService.java`
    -   **Customer Methods:** `createRequest`, `getMyRequests`, `acceptVendorQuote`.
    -   **Vendor Methods:** `findOpenRequests`, `submitQuote`.
    -   **System Method:** `findNearbyVendorsAndBroadcast(requestId)`.

-   **Controllers:**
    -   `QuotationAdminController.java`: For Phase 1 admin tasks.
    -   `QuotationCustomerController.java`: For customers to manage requests and responses.
    -   `QuotationVendorController.java`: For vendors to receive requests and submit quotes.

### 4.2. Implement Quote-to-Booking Conversion Logic

-   **Action:** Modify the `Booking` (or `OsAppointmentDetails`) entity and the booking creation logic.
-   **File:** `src/main/java/com/autoadda/apis/other/entity/OsAppointmentDetails.java`
-   **Modifications:**
    ```java
    // ... existing fields ...
    @Column(name = "reference_type") // e.g., "QUOTATION"
    private String referenceType;

    @Column(name = "reference_id") // e.g., the ID from VendorQuotation
    private Integer referenceId;
    ```
-   **Service Logic:** When a customer `accepts` a `VendorQuotation`, the `QuotationService` will call the `OsAppointmentService`'s `createAppointment` method, passing the details from the accepted quote (vendor, customer, price, line items) to pre-fill the booking. The `referenceType` and `referenceId` will be set to link back to the original quote.
