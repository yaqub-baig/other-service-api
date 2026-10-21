# Other Service API - Complete Implementation Summary

This document provides comprehensive documentation of all API endpoints, operational flows, and entity relationships for the Other Service API microservice.

---

## Table of Contents

1. [Overview](#1-overview)
2. [Entity Relationships](#2-entity-relationships)
3. [API Reference by Role](#3-api-reference-by-role)
   - [Admin APIs](#31-admin-apis)
   - [Vendor APIs](#32-vendor-apis)
   - [Customer APIs](#33-customer-apis)
   - [Shared APIs](#34-shared-apis)
4. [Operational Flows](#4-operational-flows)
5. [Status Enumerations](#5-status-enumerations)

---

## 1. Overview

### Application Purpose
The Other Service API is a dynamic marketplace backend that enables:
- **Admins** to configure service categories, attributes, filters, and quotation workflows
- **Vendors** to onboard, create service offerings, respond to quotation requests, and manage bookings
- **Customers** to search services, request quotations, and book appointments

### Technology Stack
- **Framework:** Spring Boot 3.x
- **ORM:** Hibernate 6.x
- **Database:** MySQL
- **Build Tool:** Maven

### Architecture Summary
```
┌─────────────────────────────────────────────────────────────────┐
│                        Controllers (12)                         │
├─────────────────────────────────────────────────────────────────┤
│  Admin: ServiceCategory, Attributes, Filters, QuotationAdmin    │
│  Vendor: Vendor, ServiceOffering, VendorServiceBridge,          │
│          QuotationVendor, Booking                               │
│  Customer: Search, QuotationCustomer, Booking                   │
│  Shared: MediaFile                                              │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                         Services                                 │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                     Repositories (JPA)                          │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                       MySQL Database                            │
└─────────────────────────────────────────────────────────────────┘
```

---

## 2. Entity Relationships

```
ServiceCategory (hierarchical)
    │
    ├── ServiceAttributeDefinition (defines measurable characteristics)
    │
    ├── ServiceFilterDefinition (defines searchable filters)
    │       └── FilterOption (predefined values for filters)
    │
    ├── QuotationScopeDefinition (quotation configuration per category)
    │
    └── QuotationFieldDefinition (form fields for quotation requests)

Vendor
    │
    ├── VendorServiceBridge (subscription to categories)
    │       │
    │       └── ServiceOffering (sellable items with attributes)
    │
    ├── VendorQuotation (responses to customer requests)
    │       └── VendorQuotationLineItem (price breakdown)
    │
    └── OsAppointment (bookings/appointments)
            └── JobCard (service execution tracking)

QuotationRequest (customer inquiry)
    │
    ├── QuotationRequestFieldValue (form field responses)
    │
    └── VendorQuotation (vendor responses)
```

---

## 3. API Reference by Role

### 3.1 Admin APIs

#### 3.1.1 ServiceCategoryController
**Base Path:** `/service-categories`
**Purpose:** Manage hierarchical service categories (e.g., Tyre Services, Car Detailing)

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/service-categories` | Get all service categories |
| `POST` | `/service-categories` | Create a new service category |
| `GET` | `/service-categories/{id}` | Get category by ID |
| `PUT` | `/service-categories/{id}` | Update a service category |
| `DELETE` | `/service-categories/{id}` | Delete a service category |
| `GET` | `/service-categories/parent/{parentId}` | Get child categories by parent ID |
| `GET` | `/service-categories/root` | Get root-level categories (no parent) |

**Total: 7 endpoints**

---

#### 3.1.2 ServiceAttributeDefinitionController
**Base Path:** `/admin/service-attributes`
**Purpose:** Define measurable characteristics for service offerings (e.g., tread depth, brand, size)

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/admin/service-attributes` | Create a new attribute definition |
| `GET` | `/admin/service-attributes` | Get all active attributes |
| `GET` | `/admin/service-attributes/{id}` | Get attribute by ID |
| `PUT` | `/admin/service-attributes/{id}` | Update an attribute definition |
| `DELETE` | `/admin/service-attributes/{id}` | Soft delete an attribute |
| `DELETE` | `/admin/service-attributes/{id}/hard` | Permanently delete an attribute |
| `POST` | `/admin/service-attributes/{id}/reactivate` | Reactivate a soft-deleted attribute |
| `GET` | `/admin/service-attributes/category/{categoryId}` | Get active attributes for a category |
| `GET` | `/admin/service-attributes/category/{categoryId}/all` | Get all attributes for a category (including inactive) |
| `GET` | `/admin/service-attributes/category/{categoryId}/filterable` | Get filterable attributes for a category |
| `GET` | `/admin/service-attributes/category/{categoryId}/required` | Get required attributes for a category |
| `GET` | `/admin/service-attributes/category/{categoryId}/code/{code}` | Get attribute by code within a category |

**Total: 12 endpoints**

---

#### 3.1.3 ServiceFilterDefinitionController
**Base Path:** `/admin/service-filters`
**Purpose:** Define searchable/filterable fields for categories (e.g., brand dropdown, price range)

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/admin/service-filters` | Create a new filter definition |
| `GET` | `/admin/service-filters` | Get all active filters |
| `GET` | `/admin/service-filters/{id}` | Get filter by ID |
| `PUT` | `/admin/service-filters/{id}` | Update a filter definition |
| `DELETE` | `/admin/service-filters/{id}` | Soft delete a filter |
| `DELETE` | `/admin/service-filters/{id}/hard` | Permanently delete a filter |
| `POST` | `/admin/service-filters/{id}/reactivate` | Reactivate a soft-deleted filter |
| `GET` | `/admin/service-filters/category/{categoryId}` | Get active filters for a category |
| `GET` | `/admin/service-filters/category/{categoryId}/all` | Get all filters for a category (including inactive) |
| `GET` | `/admin/service-filters/category/{categoryId}/required` | Get required filters for a category |
| `GET` | `/admin/service-filters/category/{categoryId}/dependent/{parentFilterCode}` | Get dependent filters |
| `GET` | `/admin/service-filters/category/{categoryId}/code/{code}` | Get filter by code |
| `POST` | `/admin/service-filters/{filterId}/options` | Add an option to a filter |
| `GET` | `/admin/service-filters/{filterId}/options` | Get options for a filter |
| `PUT` | `/admin/service-filters/options/{optionId}` | Update a filter option |
| `DELETE` | `/admin/service-filters/options/{optionId}` | Delete a filter option |

**Total: 16 endpoints**

---

#### 3.1.4 QuotationAdminController
**Base Path:** `/admin/quotation`
**Purpose:** Configure quotation workflows - enable/disable quotations per category and define form fields

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/admin/quotation/scope` | Create/update quotation scope for a category |
| `GET` | `/admin/quotation/scope/{id}` | Get quotation scope by ID |
| `DELETE` | `/admin/quotation/scope/{id}` | Delete scope definition (soft delete) |
| `GET` | `/admin/quotation/scope/category/{categoryId}` | Get scope for a category |
| `GET` | `/admin/quotation/scope/category/{categoryId}/enabled` | Check if quotation is enabled |
| `POST` | `/admin/quotation/scope/category/{categoryId}/disable` | Disable quotation for a category |
| `GET` | `/admin/quotation/scope/enabled` | Get all quotation-enabled categories |
| `POST` | `/admin/quotation/field` | Create a field definition |
| `PUT` | `/admin/quotation/field/{fieldId}` | Update a field definition |
| `GET` | `/admin/quotation/field/{fieldId}` | Get field by ID |
| `DELETE` | `/admin/quotation/field/{fieldId}` | Delete a field definition |
| `GET` | `/admin/quotation/field/category/{categoryId}` | Get all fields for a category |
| `GET` | `/admin/quotation/field/category/{categoryId}/required` | Get required fields for a category |
| `POST` | `/admin/quotation/field/category/{categoryId}/reorder` | Reorder fields within a category |

**Total: 14 endpoints**

---

### 3.2 Vendor APIs

#### 3.2.1 VendorController
**Base Path:** `/vendors`
**Purpose:** Vendor profile management - onboarding, updates, and search

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/vendors` | Create a new vendor (onboard) |
| `GET` | `/vendors` | Get vendors by owner ID (via X-Auto-Adda-Id header) |
| `GET` | `/vendors/all` | Get all vendors |
| `GET` | `/vendors/{id}` | Get vendor by ID |
| `PUT` | `/vendors/{id}` | Update vendor profile |
| `DELETE` | `/vendors/{id}` | Delete a vendor |
| `GET` | `/vendors/brandname/{name}` | Search vendors by brand name |
| `GET` | `/vendors/service/{serviceType}` | Get vendors by service type |
| `GET` | `/vendors/search` | Search vendors by city, zip, name, or category IDs |

**Total: 9 endpoints**

---

#### 3.2.2 VendorServiceBridgeController
**Base Path:** `/vendor-service-bridges`
**Purpose:** Link vendors to service categories (subscription management)

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/vendor-service-bridges` | Create a vendor-service subscription |
| `GET` | `/vendor-service-bridges` | Get all vendor-service bridges |
| `GET` | `/vendor-service-bridges/{id}` | Get bridge by ID |
| `PUT` | `/vendor-service-bridges/{id}` | Update a vendor-service bridge |
| `DELETE` | `/vendor-service-bridges/{id}` | Delete a vendor-service bridge |
| `GET` | `/vendor-service-bridges/vendor/{vendorId}` | Get all bridges for a vendor |

**Total: 6 endpoints**

---

#### 3.2.3 ServiceOfferingController
**Base Path:** `/service-offerings`
**Purpose:** Manage sellable items/services with pricing and attributes

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/service-offerings` | Create a new service offering |
| `GET` | `/service-offerings/{id}` | Get offering by ID |
| `GET` | `/service-offerings/sku/{sku}` | Get offering by SKU |
| `PUT` | `/service-offerings/{id}` | Update an offering |
| `DELETE` | `/service-offerings/{id}` | Soft delete an offering |
| `DELETE` | `/service-offerings/{id}/hard` | Permanently delete an offering |
| `POST` | `/service-offerings/{id}/reactivate` | Reactivate a soft-deleted offering |
| `GET` | `/service-offerings/vendor-service/{vendorServiceId}` | Get offerings by vendor service |
| `GET` | `/service-offerings/vendor-service/{vendorServiceId}/all` | Get all offerings by vendor service (including inactive) |
| `GET` | `/service-offerings/vendor/{vendorId}` | Get offerings by vendor |
| `GET` | `/service-offerings/vendor/{vendorId}/all` | Get all offerings by vendor (including inactive) |
| `GET` | `/service-offerings/vendor/{vendorId}/category/{categoryId}` | Get offerings by vendor and category |
| `GET` | `/service-offerings/category/{categoryId}` | Get offerings by category |
| `GET` | `/service-offerings/category/{categoryId}/page` | Get paginated offerings by category |
| `GET` | `/service-offerings/search` | Search offerings by name |
| `GET` | `/service-offerings/category/{categoryId}/search` | Search offerings by name in category |
| `GET` | `/service-offerings/category/{categoryId}/price-range` | Get offerings by price range |
| `GET` | `/service-offerings/featured` | Get all featured offerings |
| `GET` | `/service-offerings/category/{categoryId}/featured` | Get featured offerings by category |
| `GET` | `/service-offerings/top-rated` | Get top-rated offerings |
| `GET` | `/service-offerings/popular` | Get most popular offerings |
| `PATCH` | `/service-offerings/{id}/stock` | Update stock status |
| `PATCH` | `/service-offerings/{id}/featured` | Toggle featured status |
| `GET` | `/service-offerings/filter` | Filter offerings by single attribute |
| `POST` | `/service-offerings/filter` | Filter offerings by multiple attributes |
| `GET` | `/service-offerings/category/{categoryId}/attributes` | Get attribute definitions for a category |
| `GET` | `/service-offerings/category/{categoryId}/attributes/required` | Get required attribute definitions |

**Total: 27 endpoints**

---

#### 3.2.4 QuotationVendorController
**Base Path:** `/quotations/vendor`
**Purpose:** View open requests, submit quotations, and manage vendor's quotes

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/quotations/vendor/requests/open` | Find open quotation requests for vendor |
| `GET` | `/quotations/vendor/request/{requestId}` | Get quotation request details |
| `GET` | `/quotations/vendor/request/{requestId}/fields` | Get field values for a request |
| `POST` | `/quotations/vendor/quote` | Submit a quotation |
| `GET` | `/quotations/vendor/quotes` | Get vendor's submitted quotations |
| `GET` | `/quotations/vendor/quotes/page` | Get vendor's quotations (paginated) |
| `GET` | `/quotations/vendor/quote/{quotationId}` | Get quotation by ID |
| `GET` | `/quotations/vendor/quote/{quotationId}/items` | Get line items for a quotation |
| `POST` | `/quotations/vendor/quote/{quotationId}/withdraw` | Withdraw a submitted quotation |
| `GET` | `/quotations/vendor/stats` | Get quotation statistics |

**Total: 10 endpoints**

---

#### 3.2.5 BookingController (Vendor Endpoints)
**Base Path:** `/bookings`
**Purpose:** Manage appointments and bookings

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/bookings/other-service/{otherServiceId}` | Get appointments for a vendor/service |
| `PUT` | `/bookings/status` | Update appointment status |
| `GET` | `/bookings/{appointmentId}` | Get appointment details |

**Total: 3 vendor-specific endpoints**

---

### 3.3 Customer APIs

#### 3.3.1 SearchController
**Base Path:** `/search`
**Purpose:** Dynamic search and discovery of service offerings

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/search/offerings` | Advanced search with filters and location |
| `GET` | `/search/offerings` | Quick search with query parameters |
| `POST` | `/search/category/{categoryId}` | Search offerings by category |
| `GET` | `/search/nearby` | Search offerings near a location |
| `GET` | `/search/autocomplete` | Get autocomplete suggestions |
| `GET` | `/search/filters/{categoryId}` | Get filter options for a category |
| `GET` | `/search/vendor/{vendorId}` | Search offerings by vendor |
| `GET` | `/search/featured` | Search featured offerings |
| `POST` | `/search/multi` | Multi-category search |

**Total: 9 endpoints**

---

#### 3.3.2 QuotationCustomerController
**Base Path:** `/quotations/customer`
**Purpose:** Submit quotation requests, view quotes, and accept/reject

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/quotations/customer/request` | Submit a new quotation request |
| `GET` | `/quotations/customer/requests` | Get customer's quotation requests |
| `GET` | `/quotations/customer/requests/page` | Get requests (paginated) |
| `GET` | `/quotations/customer/request/{requestId}` | Get request by ID |
| `GET` | `/quotations/customer/request/number/{quotationNumber}` | Get request by quotation number |
| `GET` | `/quotations/customer/request/{requestId}/fields` | Get field values for a request |
| `POST` | `/quotations/customer/request/{requestId}/cancel` | Cancel a quotation request |
| `GET` | `/quotations/customer/request/{requestId}/quotes` | Get vendor quotes for a request |
| `GET` | `/quotations/customer/quote/{quotationId}` | Get quote details |
| `GET` | `/quotations/customer/quote/{quotationId}/items` | Get line items for a quote |
| `POST` | `/quotations/customer/quote/{quotationId}/accept` | Accept a vendor's quotation |
| `POST` | `/quotations/customer/quote/{quotationId}/reject` | Reject a vendor's quotation |
| `GET` | `/quotations/customer/form/category/{categoryId}` | Get quotation form fields |
| `GET` | `/quotations/customer/check/category/{categoryId}` | Check if quotation is available |
| `POST` | `/quotations/customer/quote/{quotationId}/book` | Create booking from accepted quote |

**Total: 15 endpoints**

---

#### 3.3.3 BookingController (Customer Endpoints)
**Base Path:** `/bookings`
**Purpose:** Book appointments and manage bookings

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/bookings` | Create a customer appointment |
| `POST` | `/bookings/{serviceId}/book` | Book an appointment slot |
| `GET` | `/bookings/{otherServiceId}/available-slots` | Get available slots |
| `GET` | `/bookings/user/bookings` | Get user's bookings |
| `DELETE` | `/bookings/{appointmentId}/cancel` | Cancel an appointment |
| `PUT` | `/bookings/edit/appointment` | Edit an appointment |

**Total: 6 customer-specific endpoints** (9 total in BookingController)

---

### 3.4 Shared APIs

#### 3.4.1 MediaFileController
**Base Path:** `/mediafiles`
**Purpose:** File upload and management for images/documents

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/mediafiles/upload` | Upload files (max 10MB per file) |
| `DELETE` | `/mediafiles/delete-image/{id}` | Delete an uploaded file |

**Total: 2 endpoints**

---

### API Summary

| Role | Controllers | Endpoints |
|------|-------------|-----------|
| Admin | 4 | 49 |
| Vendor | 5 | 55 |
| Customer | 3 | 30 |
| Shared | 1 | 2 |
| **Total** | **12** | **127** |

*Note: Some endpoints in BookingController serve both vendors and customers.*

---

## 4. Operational Flows

### 4.1 Admin Flow: Marketplace Setup

```
┌─────────────────────────────────────────────────────────────────┐
│                    ADMIN SETUP WORKFLOW                         │
└─────────────────────────────────────────────────────────────────┘

Step 1: Create Service Categories (Hierarchical)
──────────────────────────────────────────────────
    POST /service-categories
    {
      "name": "Tyre Services",
      "code": "TYRE_SERVICES",
      "parentId": null
    }
                │
                ▼
    POST /service-categories
    {
      "name": "New Tyres",
      "code": "NEW_TYRES",
      "parentId": 1  // Parent: Tyre Services
    }

Step 2: Define Attributes for Category
──────────────────────────────────────────────────
    POST /admin/service-attributes
    {
      "serviceCategoryId": 2,
      "attributeCode": "brand",
      "attributeLabel": "Brand",
      "dataType": "STRING",
      "isRequired": true,
      "isFilterable": true
    }
                │
                ▼
    POST /admin/service-attributes
    {
      "serviceCategoryId": 2,
      "attributeCode": "width",
      "attributeLabel": "Tyre Width",
      "dataType": "NUMBER",
      "unitOfMeasure": "mm"
    }

Step 3: Configure Search Filters
──────────────────────────────────────────────────
    POST /admin/service-filters
    {
      "serviceCategoryId": 2,
      "filterCode": "brand",
      "filterLabel": "Brand",
      "filterType": "DROPDOWN",
      "isRequired": false
    }
                │
                ▼
    POST /admin/service-filters/{filterId}/options
    {
      "value": "MRF",
      "label": "MRF Tyres",
      "displayOrder": 1
    }

Step 4: Enable Quotation System (if needed)
──────────────────────────────────────────────────
    POST /admin/quotation/scope
    {
      "serviceCategoryId": 3,
      "isQuotationEnabled": true,
      "quotationType": "MANDATORY"
    }

Step 5: Define Quotation Form Fields
──────────────────────────────────────────────────
    POST /admin/quotation/field
    {
      "serviceCategoryId": 3,
      "fieldCode": "problem_description",
      "fieldLabel": "Describe the Problem",
      "fieldType": "TEXTAREA",
      "isRequired": true,
      "displayOrder": 1
    }
```

---

### 4.2 Vendor Flow: Onboarding to Service Delivery

```
┌─────────────────────────────────────────────────────────────────┐
│                    VENDOR WORKFLOW                              │
└─────────────────────────────────────────────────────────────────┘

Step 1: Onboard (Create Vendor Profile)
──────────────────────────────────────────────────
    POST /vendors
    Headers: X-Auto-Adda-Id: {ownerUserId}
    {
      "companyBrandName": "AutoCare Hub",
      "email": "contact@autocare.com",
      "phoneNumber": "9876543210",
      "openingTime": "09:00",
      "closingTime": "18:00",
      "workingDays": "MON,TUE,WED,THU,FRI,SAT"
    }
                │
                ▼

Step 2: Upload Media (Logo, Images, Documents)
──────────────────────────────────────────────────
    POST /mediafiles/upload
    Content-Type: multipart/form-data
    files: [logo.png, shop-image-1.jpg]
                │
                ▼
    Response:
    [
      { "id": 101, "fileName": "logo.png", "fileUrl": "https://..." },
      { "id": 102, "fileName": "shop-image-1.jpg", "fileUrl": "https://..." }
    ]

Step 3: Subscribe to Service Categories
──────────────────────────────────────────────────
    POST /vendor-service-bridges
    {
      "vendorId": 1,
      "serviceCategoryId": 2  // New Tyres
    }
                │
                ▼

Step 4: Create Service Offerings with Pricing
──────────────────────────────────────────────────
    POST /service-offerings
    {
      "vendorServiceId": 1,
      "name": "MRF ZVTV 185/65 R15",
      "sku": "MRF-ZVTV-185-65-R15",
      "description": "Premium touring tyre",
      "basePrice": 4500.00,
      "attributes": {
        "brand": "MRF",
        "width": 185,
        "aspectRatio": 65,
        "rimSize": 15
      }
    }
                │
                ▼

Step 5: View Open Quotation Requests
──────────────────────────────────────────────────
    GET /quotations/vendor/requests/open?vendorId=1&categoryId=3
                │
                ▼

Step 6: Submit Quotation Response
──────────────────────────────────────────────────
    POST /quotations/vendor/quote
    {
      "quotationRequestId": 123,
      "vendorId": 1,
      "pricingType": "BREAKUP",
      "totalAmount": 8500.00,
      "validUntil": "2024-12-31",
      "notes": "Includes free alignment",
      "lineItems": [
        { "title": "MRF Tyre x 4", "quantity": 4, "unitPrice": 2000.00 },
        { "title": "Installation", "quantity": 1, "unitPrice": 500.00 }
      ]
    }
                │
                ▼

Step 7: Manage Bookings
──────────────────────────────────────────────────
    GET /bookings/other-service/{vendorServiceId}?status=PENDING
                │
                ▼

Step 8: Update Booking Status & Complete Service
──────────────────────────────────────────────────
    PUT /bookings/status
    {
      "appointmentId": 456,
      "status": "IN_PROGRESS"
    }
                │
                ▼
    PUT /bookings/status
    {
      "appointmentId": 456,
      "status": "COMPLETED"
    }
```

---

### 4.3 Customer Flow: Search to Booking

```
┌─────────────────────────────────────────────────────────────────┐
│                    CUSTOMER WORKFLOW                            │
└─────────────────────────────────────────────────────────────────┘

Step 1: Search Services by Category/Filters/Location
──────────────────────────────────────────────────
    POST /search/offerings
    {
      "categoryId": 2,
      "query": "MRF tyre",
      "minPrice": 2000,
      "maxPrice": 10000,
      "attributes": {
        "brand": "MRF",
        "width": ["185", "195"]
      },
      "latitude": 19.0760,
      "longitude": 72.8777,
      "radiusKm": 10,
      "sortBy": "price",
      "sortDirection": "asc",
      "page": 0,
      "size": 20
    }
                │
                ▼
    Response:
    {
      "results": [...offerings with vendor details...],
      "facets": { "brand": [...], "priceRange": [...] },
      "totalElements": 45,
      "totalPages": 3
    }

Step 2: Browse Vendors and Offerings
──────────────────────────────────────────────────
    GET /search/vendor/{vendorId}
    GET /service-offerings/{offeringId}
                │
                ▼

Step 3: Check if Quotation is Required
──────────────────────────────────────────────────
    GET /quotations/customer/check/category/{categoryId}
                │
                ▼

Step 4A: Request Quotation (for quote-based services)
──────────────────────────────────────────────────
    GET /quotations/customer/form/category/{categoryId}
    // Returns form fields to fill
                │
                ▼
    POST /quotations/customer/request
    {
      "customerId": 100,
      "serviceCategoryId": 3,
      "latitude": 19.0760,
      "longitude": 72.8777,
      "fieldValues": [
        { "fieldId": 1, "value": "Rattling noise from front wheel" },
        { "fieldId": 2, "value": "https://...photo-url..." }
      ]
    }
                │
                ▼

Step 5: Review Vendor Quotes
──────────────────────────────────────────────────
    GET /quotations/customer/request/{requestId}/quotes
                │
                ▼

Step 6: Accept/Reject Quotes
──────────────────────────────────────────────────
    POST /quotations/customer/quote/{quotationId}/accept
    {
      "customerId": 100
    }

    // OR reject:
    POST /quotations/customer/quote/{quotationId}/reject
    {
      "customerId": 100,
      "reason": "Found better price elsewhere"
    }
                │
                ▼

Step 7: Create Booking (from quote or direct)
──────────────────────────────────────────────────
    // From accepted quote:
    POST /quotations/customer/quote/{quotationId}/book
    {
      "vehicleId": 55,
      "preferredDate": "2024-12-15",
      "preferredTime": "10:00"
    }

    // OR direct booking:
    POST /bookings
    {
      "customerId": 100,
      "vendorId": 1,
      "vehicleId": 55,
      "appointmentDate": "2024-12-15",
      "appointmentTime": "10:00",
      "notes": "Regular service"
    }
                │
                ▼

Step 8: Track Booking Status
──────────────────────────────────────────────────
    GET /bookings/user/bookings
    GET /bookings/{appointmentId}
                │
                ▼

Step 9: Cancel Booking if Needed
──────────────────────────────────────────────────
    DELETE /bookings/{appointmentId}/cancel
```

---

### 4.4 Quotation Lifecycle Sequence

```
┌────────────┐     ┌────────────┐     ┌────────────┐
│  Customer  │     │   System   │     │   Vendor   │
└─────┬──────┘     └─────┬──────┘     └─────┬──────┘
      │                  │                  │
      │ Submit Request   │                  │
      │─────────────────>│                  │
      │                  │                  │
      │                  │ Notify Vendors   │
      │                  │─────────────────>│
      │                  │                  │
      │                  │    View Request  │
      │                  │<─────────────────│
      │                  │                  │
      │                  │   Submit Quote   │
      │                  │<─────────────────│
      │                  │                  │
      │  Notify Customer │                  │
      │<─────────────────│                  │
      │                  │                  │
      │ Review & Accept  │                  │
      │─────────────────>│                  │
      │                  │                  │
      │                  │ Create Booking   │
      │                  │─────────────────>│
      │                  │                  │
      │ Booking Confirmed│                  │
      │<─────────────────│                  │
      │                  │                  │
```

---

## 5. Status Enumerations

### QuotationRequestStatus
| Status | Description |
|--------|-------------|
| `OPEN` | Request is open for vendor quotes |
| `QUOTED` | At least one vendor has submitted a quote |
| `ACCEPTED` | Customer accepted a quote |
| `EXPIRED` | Request expired without acceptance |
| `CANCELLED` | Customer cancelled the request |

### VendorQuotationStatus
| Status | Description |
|--------|-------------|
| `DRAFT` | Quote is being prepared |
| `SENT` | Quote submitted to customer |
| `ACCEPTED` | Customer accepted this quote |
| `REJECTED` | Customer rejected this quote |
| `WITHDRAWN` | Vendor withdrew the quote |
| `EXPIRED` | Quote validity period ended |

### BookingStatus / AppointmentStatus
| Status | Description |
|--------|-------------|
| `PENDING` | Booking created, awaiting confirmation |
| `CONFIRMED` | Vendor confirmed the booking |
| `IN_PROGRESS` | Service is being performed |
| `COMPLETED` | Service completed successfully |
| `CANCELLED` | Booking was cancelled |
| `NO_SHOW` | Customer did not show up |

### StockStatus
| Status | Description |
|--------|-------------|
| `IN_STOCK` | Item is available |
| `LOW_STOCK` | Limited quantity available |
| `OUT_OF_STOCK` | Item is not available |
| `DISCONTINUED` | Item no longer offered |

### FilterType
| Type | Description |
|------|-------------|
| `DROPDOWN` | Single selection from options |
| `MULTI_SELECT` | Multiple selection from options |
| `RANGE` | Numeric range (min-max) |
| `TEXT` | Free text input |
| `BOOLEAN` | Yes/No toggle |

### AttributeDataType
| Type | Description |
|------|-------------|
| `STRING` | Text value |
| `NUMBER` | Numeric value |
| `BOOLEAN` | True/False |
| `DATE` | Date value |
| `ENUM` | Predefined options |

---

## Appendix: Request/Response Examples

### Search Request Example
```json
POST /search/offerings
{
  "categoryId": 2,
  "query": "MRF",
  "minPrice": 3000,
  "maxPrice": 8000,
  "attributes": {
    "brand": "MRF",
    "width": ["185", "195"]
  },
  "latitude": 19.0760,
  "longitude": 72.8777,
  "radiusKm": 15,
  "sortBy": "price",
  "sortDirection": "asc",
  "page": 0,
  "size": 20,
  "includeFacets": true,
  "includeVendorDetails": true
}
```

### Vendor Quotation Submission Example
```json
POST /quotations/vendor/quote
{
  "quotationRequestId": 123,
  "vendorId": 1,
  "pricingType": "BREAKUP",
  "totalAmount": 8500.00,
  "validUntil": "2024-12-31T23:59:59",
  "notes": "Price includes installation and balancing",
  "lineItems": [
    {
      "title": "MRF ZVTV 185/65 R15",
      "description": "Premium touring tyre",
      "quantity": 4,
      "unitPrice": 2000.00,
      "totalPrice": 8000.00
    },
    {
      "title": "Installation & Balancing",
      "description": "Professional fitting service",
      "quantity": 1,
      "unitPrice": 500.00,
      "totalPrice": 500.00
    }
  ]
}
```

### Booking Creation Example
```json
POST /bookings
{
  "customerId": 100,
  "vendorId": 1,
  "vehicleId": 55,
  "appointmentDate": "2024-12-15",
  "appointmentTime": "10:00:00",
  "serviceType": "TYRE_REPLACEMENT",
  "notes": "4 tyres to be replaced, alignment needed"
}
```

---

*Document last updated: Auto-generated from source code analysis*
