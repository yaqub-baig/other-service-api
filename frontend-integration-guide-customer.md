# Frontend Integration Guide - Customer App

> **Base URL:** `{API_BASE_URL}` (e.g., `http://localhost:9010/api/v1`)
> **Authentication Header:** `X-Auto-Adda-Id: {userId}` (used in booking-related endpoints)
> **Language:** send `Accept-Language: ar` to get Arabic names, labels and help text; English is used when no Arabic exists.
> **Standard Response Wrapper:**
> ```json
> {
>   "status": "success",
>   "code": "200",
>   "message": "success",
>   "data": { ... }
> }
> ```
> Not wrapped: `/search/**`, `/vendors/**` and `GET /bookings/{vendorServiceId}/available-slots` return the DTO directly.
>
> **Errors** come back as **HTTP 201** with `"status": "failed"` and the real status in `code`, so check the body, not the HTTP status:
> ```json
> { "status": "failed", "code": "400", "message": "Invalid attribute values: 1 field(s) failed validation",
>   "body": [ { "field": "vehicle_size", "message": "Vehicle size is required" } ] }
> ```

---

## Table of Contents

1. [Browse Categories & Services](#1-browse-categories--services)
2. [Search & Discover Offerings](#2-search--discover-offerings)
3. [View Vendor Profiles](#3-view-vendor-profiles)
4. [Booking Flow](#4-booking-flow)
5. [Quotation Flow](#5-quotation-flow)
6. [My Bookings Management](#6-my-bookings-management)
7. [Recommended Page Structure](#7-recommended-page-structure)
8. [Complete User Journeys](#8-complete-user-journeys)

---

## 1. Browse Categories & Services

The catalog has three levels: **category** (Washing & Detailing) → **service** (Car Wash) → **offering** (a vendor's priced listing for that service). A customer picks a category, then a service, then either books an offering or asks for quotes, depending on the service's quotation mode.

**Base Paths:** `/service-categories`, `/catalog/services`

### 1.1 Get Root Categories (Homepage)

```
GET /service-categories/root
```

**Response:** `ApiResponse<List<AvailableServiceResponse>>`

```json
{
  "data": [
    {
      "id": 1,
      "code": "MAINTENANCE",
      "name": "Routine Maintenance",
      "nameAr": "الصيانة الدورية",
      "description": null,
      "displayOrder": 10,
      "parentId": null,
      "image": null,
      "isActive": true
    },
    {
      "id": 2,
      "code": "CLEANING_DETAILING",
      "name": "Washing & Detailing",
      "nameAr": "الغسيل والتلميع",
      "displayOrder": 20,
      "parentId": null,
      "isActive": true
    }
  ]
}
```

The starter catalog has 10 categories, all at the root (no sub-categories). Sort by `displayOrder`.

### 1.2 Get Sub-Categories

```
GET /service-categories/parent/{parentId}
```

### 1.3 Get Category Details

```
GET /service-categories/{id}
```

### 1.4 Get Services in a Category

```
GET /catalog/services?categoryId=2&country=AE
```

| Param      | Type    | Required | Description                                          |
|------------|---------|----------|------------------------------------------------------|
| categoryId | Integer | No       | Only services in this category                       |
| country    | String  | No       | ISO country; only services available in that market  |

**Response:** `ApiResponse<List<CatalogServiceResponse>>`

```json
{
  "data": [
    {
      "id": 3,
      "serviceCategoryId": 2,
      "serviceCategoryName": "Washing & Detailing",
      "code": "CAR_WASH",
      "name": "Car Wash",
      "nameAr": "غسيل السيارة",
      "description": null,
      "quotationMode": null,
      "effectiveQuotationMode": "DIRECT",
      "fulfillmentMode": "BOTH",
      "estimatedDurationMinutes": 45,
      "displayOrder": 10,
      "isActive": true
    }
  ]
}
```

### 1.5 Get a Service

```
GET /catalog/services/{id}
```

### 1.6 Get a Service's Attributes

```
GET /catalog/services/{id}/attributes
```

The live form for the service: its published attributes plus the category defaults. Each item has `attributeCode`, `attributeName`, `dataType` (`STRING`, `INTEGER`, `DECIMAL`, `BOOLEAN`, `DATE`, `SINGLE_SELECT`, `MULTI_SELECT`, ...), `inputContext`, `isRequired`, `isFilterable`, `unit` and `options` (`[{ "value", "label", "labelAr" }]`). Attributes with `inputContext` `CUSTOMER` or `BOTH` are the ones a customer chooses, such as `vehicle_size` for a car wash; use them as search filters.

### Quotation Mode: Book or Quote?

`effectiveQuotationMode` decides which flow a service uses:

| Mode       | Customer can                                   | Flow                                    |
|------------|------------------------------------------------|-----------------------------------------|
| `DIRECT`   | Book an offering directly                      | Section 2 → Section 4                   |
| `OPTIONAL` | Book directly, or ask for quotes first         | Either                                  |
| `REQUIRED` | Only ask for quotes; direct booking is refused | Section 5, then book from the accepted quote |

### Frontend Usage

```
Homepage -> Category grid (root categories)
  -> Click category -> Service list (GET /catalog/services?categoryId=&country=)
  -> Click service  -> DIRECT / OPTIONAL: offerings for that service (search with serviceId)
                    -> REQUIRED / OPTIONAL: quotation request form
```

---

## 2. Search & Discover Offerings

**Base Path:** `/search`

This is the primary discovery mechanism for customers to find services and products.

### 2.1 Quick Search (GET - Simple)

```
GET /search/offerings?categoryId=3&serviceId=5&country=AE&q=MRF&minPrice=200&maxPrice=1000&city=Dubai&sortBy=price&sortDir=asc&page=0&size=20
```

| Param      | Type    | Default    | Description                          |
|------------|---------|------------|--------------------------------------|
| categoryId | Integer | -          | Category to search in                |
| q          | String  | -          | Search query text                    |
| minPrice   | Double  | -          | Minimum price filter                 |
| maxPrice   | Double  | -          | Maximum price filter                 |
| city       | String  | -          | Filter by city                       |
| area       | String  | -          | Filter by area within city           |
| country    | String  | -          | ISO country of the vendor's market   |
| serviceId  | Integer | -          | Catalog service (from 1.4)           |
| lat        | Double  | -          | User latitude (for distance sort)    |
| lng        | Double  | -          | User longitude (for distance sort)   |
| radius     | Double  | -          | Max distance in km                   |
| sortBy     | String  | "relevance"| `price`, `rating`, `popularity`, `distance`, `newest`, `name` |
| sortDir    | String  | "asc"      | `asc` or `desc`                      |
| page       | Integer | 0          | Page number (0-based)                |
| size       | Integer | 20         | Results per page                     |

### 2.2 Advanced Search (POST - Full Filtering)

```
POST /search/offerings
Content-Type: application/json
```

**Request Body:**
```json
{
  "categoryId": 3,
  "serviceId": 5,
  "countryCode": "AE",
  "query": "MRF tyre",
  "minPrice": 200,
  "maxPrice": 1000,
  "pricingTypes": ["FIXED", "RANGE"],
  "stockStatuses": ["IN_STOCK", "LIMITED_STOCK"],
  "attributes": {
    "tyre_brand": "MRF",
    "rim_size": ["15", "16"]
  },
  "latitude": 25.2048,
  "longitude": 55.2708,
  "radiusKm": 10,
  "city": "Dubai",
  "area": "Al Quoz",
  "minRating": 4.0,
  "sortBy": "price",
  "sortDirection": "asc",
  "page": 0,
  "size": 20,
  "includeVendorDetails": true,
  "includeDistance": true,
  "includeFacets": true
}
```

| Field        | Description                                                                 |
|--------------|-----------------------------------------------------------------------------|
| serviceId    | Catalog service; narrows results to offerings for that service             |
| countryCode  | ISO country of the vendor's market                                         |
| attributes   | Attribute code → value, or a list of values for "any of". Codes come from 1.6 or `GET /search/filters/{categoryId}`; an unknown code is refused with `code: "400"` |
| offeringType | `SERVICE` or `PRODUCT`                                                       |
| vendorId, skus, featuredOnly | Further optional filters                                    |
| sortBy       | `relevance` (default), `price`, `rating`, `popularity`, `distance`, `newest`, `name` |

**Response:** `OfferingSearchResponse`

```json
{
  "results": [
    {
      "id": 101,
      "sku": "TIR-0001-8F2A11C3",
      "offeringName": "MRF ZLX 185/65 R15",
      "shortDescription": "Tubeless Car Tyre",
      "pricingType": "FIXED",
      "price": 350.00,
      "minPrice": null,
      "maxPrice": null,
      "currency": "AED",
      "stockStatus": "IN_STOCK",
      "imageUrl": "https://...",
      "averageRating": 4.5,
      "totalRatings": 12,
      "totalOrders": 40,
      "isFeatured": false,
      "attributes": { "tyre_brand": "MRF", "rim_size": "15" },
      "vendor": {
        "id": 1,
        "companyBrandName": "QuickFit Tyres",
        "companyLogoUrl": "https://...",
        "city": "Dubai",
        "area": "Al Quoz",
        "averageRating": 4.6,
        "totalRatings": 80
      },
      "distanceKm": 2.5,
      "score": 1.0
    }
  ],
  "totalResults": 45,
  "totalPages": 3,
  "currentPage": 0,
  "pageSize": 20,
  "hasMore": true,
  "facets": {
    "tyre_brand": [
      { "value": "MRF", "label": "MRF", "count": 15, "selected": true },
      { "value": "Bridgestone", "label": "Bridgestone", "count": 10, "selected": false }
    ]
  },
  "priceRange": { "min": 180.00, "max": 950.00 },
  "metadata": { "executionTimeMs": 35, "query": "MRF tyre", "categoryId": 3, "geoSearchApplied": true, "searchRadiusKm": 10 }
}
```

Search results don't include the `vendorServiceId` needed to book. Get it from the offering: `GET /service-offerings/{id}` returns `vendorServiceId` and `serviceId`.

### 2.3 Search by Category

```
POST /search/category/{categoryId}
Content-Type: application/json
```

Body is optional `OfferingSearchRequest` (category is set from path).

### 2.4 Search Nearby

```
GET /search/nearby?lat=25.2048&lng=55.2708&radius=10&categoryId=1&limit=20
```

Returns `List<OfferingSearchResult>` (no pagination wrapper).

### 2.5 Autocomplete

```
GET /search/autocomplete?q=MRF&categoryId=1&limit=10
```

Returns `List<String>` of suggestions.

### 2.6 Get Filter Options for Category

```
GET /search/filters/{categoryId}
```

Returns `Map<String, List<FacetValue>>` - use this to build the filter sidebar dynamically.

```json
{
  "tyre_brand": [
    { "value": "MRF", "label": "MRF", "count": 15, "selected": false },
    { "value": "Bridgestone", "label": "Bridgestone", "count": 10, "selected": false }
  ],
  "rim_size": [
    { "value": "15", "label": "15", "count": 8, "selected": false },
    { "value": "16", "label": "16", "count": 12, "selected": false }
  ]
}
```

### 2.7 Search by Vendor

```
GET /search/vendor/{vendorId}?q=tyre&page=0&size=20
```

### 2.8 Search Featured

```
GET /search/featured?categoryId=1&page=0&size=20
```

### 2.9 Multi-Category Search

```
POST /search/multi
Content-Type: application/json

[
  { "categoryId": 1, "query": "tyres", "size": 5 },
  { "categoryId": 2, "query": "wash", "size": 5 }
]
```

Returns `Map<Integer, OfferingSearchResponse>` keyed by category ID.

### Frontend Integration for Dynamic Filters

```javascript
// Step 1: Load filter definitions for category
const filterOptions = await fetch(`/search/filters/${categoryId}`).then(r => r.json());

// Step 2: Render filter UI based on response
// filterOptions = { "tyre_brand": [...], "rim_size": [...] }

// Step 3: When user applies filters, build search request
const searchRequest = {
  categoryId: categoryId,
  serviceId: serviceId,        // the service picked in section 1.4
  countryCode: 'AE',
  attributes: {
    tyre_brand: selectedBrands,  // list = any of these
    rim_size: selectedRimSizes
  },
  minPrice: priceRange[0],     // from RANGE slider
  maxPrice: priceRange[1],
  sortBy: selectedSort,
  page: currentPage,
  size: 20,
  includeFacets: true          // to update filter counts
};

const results = await fetch('/search/offerings', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify(searchRequest)
}).then(r => r.json());
```

---

## 3. View Vendor Profiles

**Base Path:** `/vendors`

### 3.1 Get Vendor Details

```
GET /vendors/{id}
```

**Response:** `VendorProfileResponse` (not wrapped)
```json
{
  "id": 1,
  "companyBrandName": "QuickFit Tyres",
  "companyArabicName": "كويك فيت",
  "name": "QuickFit Tyres LLC",
  "webSite": "https://quickfit.ae",
  "email": "info@quickfit.ae",
  "phoneNumber": "+971-4-1234567",
  "mobileNumber": "+971501234567",
  "accountStatus": "ACTIVE",
  "countryCode": "AE",
  "companyLogo": { "id": 10, "fileUrl": "https://..." },
  "companyLocation": {
    "address": "Al Quoz Industrial Area",
    "city": "Dubai",
    "country": "AE",
    "latitude": 25.2048,
    "longitude": 55.2708
  },
  "services": [
    {
      "id": 5,
      "serviceType": "TYRE_AND_BALANCING",
      "serviceCategoryId": 3,
      "serviceCategoryName": "Tires & Wheels",
      "openingTime": "08:00",
      "closingTime": "20:00",
      "workingDays": "MONDAY,TUESDAY,WEDNESDAY,THURSDAY,FRIDAY,SATURDAY",
      "serviceLocation": { "address": "Shop 4", "city": "Dubai" },
      "serviceImages": [ { "id": 13, "fileUrl": "https://..." } ],
      "attributes": {}
    }
  ]
}
```

A vendor can run several services (branches), each with its own hours, address, photos and booking slots. `services[].id` is the **`vendorServiceId`** used for slots and bookings.

### 3.2 Search Vendors

```
GET /vendors/search?city=Dubai&serviceCategoryIds=1,2
```

### 3.3 Get Vendor's Offerings

```
GET /service-offerings/vendor/{vendorId}
```

### 3.4 Get Vendor's Offerings by Category

```
GET /service-offerings/vendor/{vendorId}/category/{categoryId}
```

---

## 4. Booking Flow

**Base Path:** `/bookings`

### 4.1 Check Available Slots

```
GET /bookings/{vendorServiceId}/available-slots?date=25/04/2026
```

| Param          | Type      | Format      | Description        |
|----------------|-----------|-------------|--------------------|
| vendorServiceId | Integer   | path        | Vendor service (branch) ID |
| date           | LocalDate | dd/MM/yyyy  | Date to check      |

**Response** (not wrapped):
```json
{
  "vendorServiceId": 1,
  "date": "2026-04-25",
  "availableSlots": [
    {
      "slotId": 1,
      "startTime": "09:00",
      "endTime": "10:00",
      "maxBookings": 3,
      "currentBookings": 1,
      "availableBookings": 2,
      "isAvailable": true
    },
    {
      "slotId": 2,
      "startTime": "10:00",
      "endTime": "11:00",
      "maxBookings": 3,
      "currentBookings": 0,
      "availableBookings": 3,
      "isAvailable": true
    }
  ]
}
```

### 4.2 Book Appointment (Slot-Based)

```
POST /bookings/{vendorServiceId}/book?vehicleId=10&time=2026-04-25T09:00:00&catalogServiceId=3
X-Auto-Adda-Id: {userId}
```

| Param                   | Type          | Description                         |
|-------------------------|---------------|-------------------------------------|
| vendorServiceId          | Integer       | path - Vendor service (branch) ID   |
| vehicleId                | Integer       | query - Customer's vehicle ID       |
| time                     | LocalDateTime | query - ISO datetime of the slot start |
| catalogServiceId         | Integer       | query, optional - the catalog service being booked (from 1.4) |
| selectedVendorServiceIds | String        | query, optional - comma-separated extra vendor service IDs |

**Header:** `X-Auto-Adda-Id: {userId}`

**Response:** `ApiResponse<BookingDetailResponse>`

When `catalogServiceId` is sent, the booking is checked against the catalog: a service whose mode is `REQUIRED` can't be booked directly (book from an accepted quote instead), and the vendor must offer the service's category.

### 4.3 Create Appointment (Direct)

```
POST /bookings
Content-Type: application/json
```

**Request Body:**
```json
{
  "customerId": 100,
  "vendorServiceId": 1,
  "catalogServiceId": 3,
  "vehicleId": 10,
  "serviceIds": [1, 3],
  "scheduleDate": "2026-04-25",
  "serviceStartTime": "09:00:00",
  "serviceEndTime": "10:00:00",
  "deliveryTime": "12:00:00",
  "comments": "Please check alignment too",
  "memberId": 100,
  "memberType": "CUSTOMER"
}
```

`catalogServiceId` is optional and follows the same rules as in 4.2.

### 4.4 Edit Appointment

```
PUT /bookings/edit?bookingId=123&serviceId=1&vehicleId=10&time=2026-04-26T10:00:00
X-Auto-Adda-Id: {userId}
```

| Param          | Type          | Required | Description           |
|----------------|---------------|----------|-----------------------|
| bookingId      | Integer       | Yes      | Booking ID to edit    |
| serviceId      | Integer       | No       | Change vendor service |
| vehicleId      | Integer       | No       | Change vehicle        |
| time           | LocalDateTime | No       | Change date/time      |

### 4.5 Cancel Appointment

```
DELETE /bookings/{appointmentId}/cancel
X-Auto-Adda-Id: {userId}
```

---

## 5. Quotation Flow

This is a multi-step flow where a customer requests quotes from vendors.

Use this flow when the service's `effectiveQuotationMode` (section 1.4) is `REQUIRED` or `OPTIONAL`. Requests for a `DIRECT` service are refused.

Pass `serviceId` on steps 1–3: a service can have its own quotation scope and fields, which override the category's.

### Step 1: Check if Quotation is Available

```
GET /quotations/customer/check/category/{categoryId}?serviceId={serviceId}
```

**Response:** `ApiResponse<Boolean>`

### Step 2: Get Quotation Form Fields

```
GET /quotations/customer/form/category/{categoryId}?serviceId={serviceId}
```

**Response:** `ApiResponse<List<QuotationFieldDefinitionResponse>>`

`options`, `validationRules` and `dependsOnCondition` are **JSON strings**; parse them with `JSON.parse`. `options` is a list of plain values or of `{ "value", "label", "labelAr" }` objects. `label`, `placeholder` and `helpText` follow `Accept-Language`.

```json
{
  "data": [
    {
      "id": 1,
      "fieldCode": "vehicle_make",
      "label": "Vehicle Make",
      "fieldType": "DROPDOWN",
      "isRequired": true,
      "displayOrder": 1,
      "placeholder": "Select make",
      "helpText": "Select your vehicle manufacturer",
      "options": "[\"Toyota\", \"Honda\", \"BMW\"]",
      "validationRules": null,
      "dependsOnFieldId": null,
      "dependsOnCondition": null
    },
    {
      "id": 2,
      "fieldCode": "vehicle_model",
      "label": "Vehicle Model",
      "fieldType": "DROPDOWN",
      "isRequired": true,
      "displayOrder": 2,
      "dependsOnFieldId": 1,
      "dependsOnCondition": "{\"hasValue\": true}"
    },
    {
      "id": 3,
      "fieldCode": "issue_description",
      "label": "Describe the Issue",
      "fieldType": "TEXTAREA",
      "isRequired": true,
      "displayOrder": 3
    },
    {
      "id": 4,
      "fieldCode": "photos",
      "label": "Upload Photos",
      "fieldType": "FILE_UPLOAD",
      "isRequired": false,
      "displayOrder": 4
    }
  ]
}
```

### Step 3: Submit Quotation Request

```
POST /quotations/customer/request
Content-Type: application/json
```

**Request Body:**
```json
{
  "serviceCategoryId": 1,
  "serviceId": 4,
  "customerId": 100,
  "serviceLocationType": "AT_VENDOR",
  "city": "Dubai",
  "area": "Al Quoz",
  "address": "Near Mall of the Emirates",
  "latitude": 25.2048,
  "longitude": 55.2708,
  "customerNotes": "Need it done urgently",
  "fieldValues": {
    "vehicle_make": "Toyota",
    "vehicle_model": "Camry",
    "issue_description": "AC not cooling properly",
    "photos": [45, 46]
  }
}
```

| Field               | Type    | Required | Description                                        |
|---------------------|---------|----------|----------------------------------------------------|
| serviceCategoryId   | Integer | Yes*     | Category for the quotation (*taken from the service when `serviceId` is sent) |
| serviceId           | Integer | No       | Catalog service; recommended                       |
| customerId          | Integer | Yes      | Customer ID                                        |
| serviceLocationType | Enum    | Yes      | `AT_VENDOR`, `AT_CUSTOMER`, `EITHER`               |
| city                | String  | No       | Customer's city                                    |
| area                | String  | No       | Customer's area                                    |
| address             | String  | No       | Full address (for AT_CUSTOMER)                     |
| latitude            | Double  | No       | Location lat                                       |
| longitude           | Double  | No       | Location lng                                       |
| customerNotes       | String  | No       | Additional notes                                   |
| fieldValues         | Map     | Yes      | Field code → value. Required fields, types and options are checked; unknown codes are refused |

**Response:** `ApiResponse<QuotationRequestResponse>`

```json
{
  "data": {
    "id": 50,
    "quotationNumber": "QR-20260425-001",
    "serviceCategory": { "id": 5, "code": "REPAIRS_DIAGNOSTICS", "name": "Repairs & Diagnostics" },
    "serviceId": 4,
    "serviceCode": "AC_REPAIR",
    "serviceName": "AC Repair",
    "schemaVersionId": 1,
    "customerId": 100,
    "serviceLocationType": "AT_VENDOR",
    "status": "REQUESTED",
    "expiresAt": "2026-04-28T14:30:00",
    "broadcastCount": 0,
    "quotesReceivedCount": 0,
    "fieldValues": [ ... ],
    "vendorQuotations": [],
    "createdAt": "2026-04-25T14:30:00"
  }
}
```

### Step 4: Track My Quotation Requests

```
GET /quotations/customer/requests?customerId=100
```

**With Pagination:**
```
GET /quotations/customer/requests/page?customerId=100&page=0&size=10
```

### Step 5: Get Request Details

```
GET /quotations/customer/request/{requestId}
```

### Step 6: Get Request by Quotation Number

```
GET /quotations/customer/request/number/{quotationNumber}
```

### Step 7: View Field Values for a Request

```
GET /quotations/customer/request/{requestId}/fields
```

### Step 8: View Vendor Quotes Received

```
GET /quotations/customer/request/{requestId}/quotes
```

**Response:** `ApiResponse<List<VendorQuotationResponse>>`

```json
{
  "data": [
    {
      "id": 10,
      "quoteNumber": "VQ-20260425-001",
      "quotationRequestId": 50,
      "vendor": {
        "id": 1,
        "companyBrandName": "QuickFit Tyres",
        "companyLogo": { ... }
      },
      "pricingType": "BREAKUP",
      "totalAmount": 1500.00,
      "minAmount": null,
      "maxAmount": null,
      "inspectionFee": 0,
      "estimatedDuration": "2-3 hours",
      "warrantyInfo": "6 months warranty",
      "validUntil": "2026-05-05",
      "status": "SENT",
      "vendorNotes": "We can do it same day",
      "lineItems": [
        {
          "itemType": "SERVICE",
          "title": "AC Gas Refill",
          "quantity": 1,
          "unitPrice": 800.00,
          "discount": 0,
          "taxRate": 5,
          "totalPrice": 800.00,
          "isOptional": false
        },
        {
          "itemType": "PART",
          "title": "AC Filter",
          "quantity": 1,
          "unitPrice": 700.00,
          "totalPrice": 700.00
        }
      ],
      "createdAt": "2026-04-25T16:00:00"
    }
  ]
}
```

### Step 9: View Single Quote Details

```
GET /quotations/customer/quote/{quotationId}
```

### Step 10: View Quote Line Items

```
GET /quotations/customer/quote/{quotationId}/items
```

### Step 11: Accept a Quote

```
POST /quotations/customer/quote/{quotationId}/accept
Content-Type: application/json
```

```json
{
  "customerId": 100
}
```

### Step 12: Reject a Quote

```
POST /quotations/customer/quote/{quotationId}/reject
Content-Type: application/json
```

```json
{
  "customerId": 100,
  "reason": "Price too high"
}
```

### Step 13: Cancel Quotation Request

```
POST /quotations/customer/request/{requestId}/cancel?customerId=100
```

### Step 14: Book from Accepted Quotation

```
POST /quotations/customer/quote/{quotationId}/book
Content-Type: application/json
```

```json
{
  "customerId": 100,
  "scheduleDate": "2026-04-28",
  "vehicleId": 10,
  "comments": "Morning preferred"
}
```

Booking from a quote doesn't need a slot or vendor service id; they come from the accepted quote. Each quote can be booked once.

**Vendor quote `pricingType`:** `FIXED`, `RANGE` (`minAmount`–`maxAmount`), `BREAKUP` (line items), `INSPECTION_REQUIRED` (`inspectionFee`, final price after inspection).

**Quotation Request Statuses:**
| Status           | Description                              |
|------------------|------------------------------------------|
| REQUESTED        | Initial - just submitted                 |
| SENT_TO_VENDORS  | Broadcast to matching vendors            |
| QUOTES_RECEIVED  | At least one vendor has responded        |
| ACCEPTED         | Customer accepted a vendor's quote       |
| EXPIRED          | Auto-expired without acceptance (checked hourly) |
| CANCELLED        | Customer cancelled                       |

**Vendor Quotation Statuses:**
| Status    | Description                    |
|-----------|--------------------------------|
| DRAFT     | Vendor preparing               |
| SENT      | Sent to customer               |
| ACCEPTED  | Customer accepted this quote   |
| REJECTED  | Customer rejected this quote   |
| EXPIRED   | Validity period passed         |
| WITHDRAWN | Vendor withdrew the quote      |

---

## 6. My Bookings Management

**Base Path:** `/bookings`

### 6.1 Get My Bookings

```
GET /bookings/user/bookings
X-Auto-Adda-Id: {userId}
```

**Response:** `ApiResponse<List<BookingDetailResponse>>`

```json
{
  "data": [
    {
      "id": 123,
      "appointmentReferenceId": "APT-20260425-001",
      "scheduleDate": "2026-04-25",
      "serviceStartTime": "09:00:00",
      "serviceEndTime": "10:00:00",
      "deliveryTime": "11:00:00",
      "status": "CONFIRMED",
      "comments": "Check alignment too",
      "vendor": {
        "id": 1,
        "companyBrandName": "QuickFit Tyres"
      },
      "selectedServiceIds": "5",
      "servicesResponse": [
        { "id": 3, "code": "TIRES_WHEELS", "name": "Tires & Wheels" }
      ],
      "customerId": 100,
      "vehicleId": 10,
      "technicianId": null,
      "vehicle": {
        "make": "Toyota",
        "model": "Camry"
      },
      "slot": {
        "startTime": "09:00",
        "endTime": "10:00"
      }
    }
  ]
}
```

### 6.2 Get Booking Details

```
GET /bookings/{appointmentId}
```

### 6.3 Edit Booking

```
PUT /bookings/edit?bookingId=123&time=2026-04-26T10:00:00
X-Auto-Adda-Id: {userId}
```

### 6.4 Cancel Booking

```
DELETE /bookings/{appointmentId}/cancel
X-Auto-Adda-Id: {userId}
```

---

## 7. Recommended Page Structure

```
/
  /home                         -- Root categories + featured offerings
  /category/:id                 -- Services in the category
  /service/:id                  -- Offerings for the service, or the quote form (by quotation mode)
  /search                       -- Search results with filters
  /offering/:id                 -- Offering detail page
  /vendor/:id                   -- Vendor profile page
  /vendor/:id/offerings         -- Vendor's offerings
  /booking
    /select-slot/:vendorServiceId -- Slot selection calendar
    /confirm                    -- Booking confirmation
  /quotation
    /request/:serviceId         -- Quotation request form
    /my-requests                -- List of my quotation requests
    /request/:requestId         -- Request detail + received quotes
    /quote/:quotationId         -- Quote detail + accept/reject
  /my-bookings                  -- List of my bookings
    /:bookingId                 -- Booking detail
```

---

## 8. Complete User Journeys

### Journey 1: Search & Book

```
1. Browse categories            GET /service-categories/root
2. Pick a service               GET /catalog/services?categoryId={id}&country=AE
   (effectiveQuotationMode is DIRECT or OPTIONAL)
3. Load filter options          GET /catalog/services/{serviceId}/attributes  or  GET /search/filters/{categoryId}
4. Search offerings             POST /search/offerings  { categoryId, serviceId, countryCode, attributes }
5. View offering details        GET /service-offerings/{id}  -> vendorServiceId
6. View vendor profile          GET /vendors/{vendorId}
7. Check available slots        GET /bookings/{vendorServiceId}/available-slots?date=...
8. Book slot                    POST /bookings/{vendorServiceId}/book?vehicleId=...&time=...&catalogServiceId={serviceId}
9. View my bookings             GET /bookings/user/bookings
```

### Journey 2: Request Quote & Book

```
1. Browse categories             GET /service-categories/root
2. Pick a service                GET /catalog/services?categoryId={id}&country=AE
   (effectiveQuotationMode is REQUIRED or OPTIONAL)
3. Check quotation available     GET /quotations/customer/check/category/{categoryId}?serviceId={serviceId}
4. Load quotation form           GET /quotations/customer/form/category/{categoryId}?serviceId={serviceId}
5. Submit quotation request      POST /quotations/customer/request  { serviceId, fieldValues, ... }
6. Wait for vendor quotes...
7. View received quotes          GET /quotations/customer/request/{id}/quotes
8. Compare quotes (view items)   GET /quotations/customer/quote/{id}/items
9. Accept best quote             POST /quotations/customer/quote/{id}/accept
10. Book from accepted quote     POST /quotations/customer/quote/{id}/book
11. View my bookings             GET /bookings/user/bookings
```

### Journey 3: Nearby Search

```
1. Get user location (browser geolocation API)
2. Search nearby                 GET /search/nearby?lat=...&lng=...&radius=10
3. View offering/vendor          GET /service-offerings/{id}
4. Book appointment              POST /bookings/{vendorServiceId}/book?...
```

### Frontend Code Example: Complete Quotation Flow

```javascript
const API = 'http://localhost:9010/api/v1';

// 0. The service the customer picked (GET /catalog/services?categoryId=...)
//    has effectiveQuotationMode REQUIRED or OPTIONAL

// 1. Check if quotation is available
const { data: enabled } = await fetch(
  `${API}/quotations/customer/check/category/${categoryId}?serviceId=${serviceId}`
).then(r => r.json());

if (!enabled) {
  // Redirect to direct booking flow
  return;
}

// 2. Load form fields
const { data: fields } = await fetch(
  `${API}/quotations/customer/form/category/${categoryId}?serviceId=${serviceId}`
).then(r => r.json());
// options / validationRules / dependsOnCondition are JSON strings: JSON.parse them

// 3. Render dynamic form based on fields
// Handle field types: TEXT, NUMBER, DROPDOWN, MULTI_SELECT, BOOLEAN, 
//                     FILE_UPLOAD, DATE, TEXTAREA
// Handle conditional fields: check dependsOnFieldId + dependsOnCondition

// 4. Submit request
const submitted = await fetch(`${API}/quotations/customer/request`, {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({
    serviceCategoryId: categoryId,
    serviceId,
    customerId: currentUser.id,
    serviceLocationType: 'AT_VENDOR',
    city: 'Dubai',
    customerNotes: 'Need it done ASAP',
    fieldValues: formValues  // { field_code: value, ... }
  })
}).then(r => r.json());
if (submitted.status === 'failed') {
  // errors are HTTP 201 too; body lists each failing field: [{ field, message }]
  showErrors(submitted.body);
  return;
}
const request = submitted.data;

// 5. Poll for quotes or use push notification
const { data: quotes } = await fetch(
  `${API}/quotations/customer/request/${request.id}/quotes`
).then(r => r.json());

// 6. Accept best quote
await fetch(`${API}/quotations/customer/quote/${selectedQuoteId}/accept`, {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({ customerId: currentUser.id })
});

// 7. Book from quotation
await fetch(`${API}/quotations/customer/quote/${selectedQuoteId}/book`, {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({
    customerId: currentUser.id,
    scheduleDate: '2026-04-28',
    vehicleId: myVehicleId
  })
});
```

---

## 9. Axios API Reference

### 9.1 Axios Setup

```javascript
// api/axiosInstance.js
import axios from 'axios';

const api = axios.create({
  baseURL: process.env.REACT_APP_API_BASE_URL || 'http://localhost:9010/api/v1',
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 30000,
});

// Attach X-Auto-Adda-Id header for authenticated requests
export const setAuthHeader = (userId) => {
  api.defaults.headers.common['X-Auto-Adda-Id'] = userId;
};

// Errors come back as HTTP 201 with status "failed", so turn them into rejections
api.interceptors.response.use(
  (response) => {
    if (response.data?.status === 'failed') {
      return Promise.reject({ response, message: response.data.message });
    }
    return response;
  },
  (error) => {
    const message = error.response?.data?.message || error.message;
    console.error('API Error:', message);
    return Promise.reject(error);
  }
);

export default api;
```

---

### 9.2 Service Category APIs

```javascript
// api/customer/categoryApi.js
import api from '../axiosInstance';

const CATEGORY_BASE = '/service-categories';

// Get root categories (for homepage)
export const getRootCategories = () =>
  api.get(`${CATEGORY_BASE}/root`);

// Get sub-categories
export const getSubCategories = (parentId) =>
  api.get(`${CATEGORY_BASE}/parent/${parentId}`);

// Get category by ID
export const getCategoryById = (id) =>
  api.get(`${CATEGORY_BASE}/${id}`);

// Get all categories
export const getAllCategories = () =>
  api.get(CATEGORY_BASE);

// --- Catalog services ---
const CATALOG_BASE = '/catalog/services';

// Services in a category (optionally only those available in a country)
export const getCatalogServices = ({ categoryId, country } = {}) =>
  api.get(CATALOG_BASE, { params: { categoryId, country } });
// each item has effectiveQuotationMode: DIRECT | OPTIONAL | REQUIRED

// One service
export const getCatalogService = (id) =>
  api.get(`${CATALOG_BASE}/${id}`);

// A service's attributes (filters / form fields)
export const getCatalogServiceAttributes = (id) =>
  api.get(`${CATALOG_BASE}/${id}/attributes`);
```

---

### 9.3 Search & Discovery APIs

```javascript
// api/customer/searchApi.js
import api from '../axiosInstance';

const SEARCH_BASE = '/search';

// Quick search (GET with query params)
export const quickSearch = ({
  categoryId, serviceId, country, q, minPrice, maxPrice, city, area,
  lat, lng, radius, sortBy = 'relevance', sortDir = 'asc',
  page = 0, size = 20,
} = {}) =>
  api.get(`${SEARCH_BASE}/offerings`, {
    params: { categoryId, serviceId, country, q, minPrice, maxPrice, city, area, lat, lng, radius, sortBy, sortDir, page, size },
  });

// Advanced search (POST with full body)
export const advancedSearch = (searchRequest) =>
  api.post(`${SEARCH_BASE}/offerings`, searchRequest);
// searchRequest = {
//   categoryId, serviceId, countryCode, offeringType, query,
//   minPrice, maxPrice, pricingTypes, stockStatuses,
//   attributes: { tyre_brand: "MRF", rim_size: ["15","16"] },
//   latitude, longitude, radiusKm, city, area, minRating,
//   sortBy, sortDirection, page, size,
//   includeVendorDetails, includeDistance, includeFacets
// }

// Search by category
export const searchByCategory = (categoryId, searchRequest = null) =>
  api.post(`${SEARCH_BASE}/category/${categoryId}`, searchRequest);

// Search nearby
export const searchNearby = (lat, lng, radius = 10, categoryId = null, limit = 20) =>
  api.get(`${SEARCH_BASE}/nearby`, {
    params: { lat, lng, radius, categoryId, limit },
  });

// Autocomplete suggestions
export const autocomplete = (q, categoryId = null, limit = 10) =>
  api.get(`${SEARCH_BASE}/autocomplete`, {
    params: { q, categoryId, limit },
  });

// Get filter options for a category (for building filter sidebar)
export const getFilterOptions = (categoryId) =>
  api.get(`${SEARCH_BASE}/filters/${categoryId}`);

// Search by vendor
export const searchByVendor = (vendorId, q = null, page = 0, size = 20) =>
  api.get(`${SEARCH_BASE}/vendor/${vendorId}`, {
    params: { q, page, size },
  });

// Search featured offerings
export const searchFeatured = (categoryId = null, page = 0, size = 20) =>
  api.get(`${SEARCH_BASE}/featured`, {
    params: { categoryId, page, size },
  });

// Multi-category search
export const multiCategorySearch = (requests) =>
  api.post(`${SEARCH_BASE}/multi`, requests);
// requests = [{ categoryId: 1, query: "tyres", size: 5 }, ...]
```

---

### 9.4 Service Offering APIs

```javascript
// api/customer/offeringApi.js
import api from '../axiosInstance';

const OFFERING_BASE = '/service-offerings';

// Get offering by ID
export const getOfferingById = (id) =>
  api.get(`${OFFERING_BASE}/${id}`);

// Get offering by SKU
export const getOfferingBySku = (sku) =>
  api.get(`${OFFERING_BASE}/sku/${sku}`);

// Get offerings by category
export const getOfferingsByCategory = (categoryId) =>
  api.get(`${OFFERING_BASE}/category/${categoryId}`);

// Get offerings by category (paginated)
export const getOfferingsByCategoryPaginated = (categoryId, page = 0, size = 20) =>
  api.get(`${OFFERING_BASE}/category/${categoryId}/page`, {
    params: { page, size },
  });

// Search offerings by name
export const searchOfferingsByName = (q) =>
  api.get(`${OFFERING_BASE}/search`, { params: { q } });

// Search within a category
export const searchInCategory = (categoryId, q) =>
  api.get(`${OFFERING_BASE}/category/${categoryId}/search`, { params: { q } });

// Get by price range
export const getByPriceRange = (categoryId, minPrice, maxPrice) =>
  api.get(`${OFFERING_BASE}/category/${categoryId}/price-range`, {
    params: { minPrice, maxPrice },
  });

// Get featured offerings
export const getFeaturedOfferings = () =>
  api.get(`${OFFERING_BASE}/featured`);

// Get featured by category
export const getFeaturedByCategory = (categoryId) =>
  api.get(`${OFFERING_BASE}/category/${categoryId}/featured`);

// Get top-rated offerings
export const getTopRated = (limit = 10) =>
  api.get(`${OFFERING_BASE}/top-rated`, { params: { limit } });

// Get most popular offerings
export const getMostPopular = (limit = 10) =>
  api.get(`${OFFERING_BASE}/popular`, { params: { limit } });

// Get offerings by vendor
export const getOfferingsByVendor = (vendorId) =>
  api.get(`${OFFERING_BASE}/vendor/${vendorId}`);

// Get offerings by vendor and category
export const getOfferingsByVendorAndCategory = (vendorId, categoryId) =>
  api.get(`${OFFERING_BASE}/vendor/${vendorId}/category/${categoryId}`);

// Filter by single attribute
export const filterByAttribute = (attribute, value) =>
  api.get(`${OFFERING_BASE}/filter`, { params: { attribute, value } });

// Filter by multiple attributes
export const filterByAttributes = (attributes) =>
  api.post(`${OFFERING_BASE}/filter`, attributes);
// attributes = { brand: "MRF", width: "185" }

// Get attribute definitions for a category (for understanding offering specs)
export const getAttributeDefinitions = (categoryId) =>
  api.get(`${OFFERING_BASE}/category/${categoryId}/attributes`);
```

---

### 9.5 Vendor Profile APIs

```javascript
// api/customer/vendorApi.js
import api from '../axiosInstance';

const VENDOR_BASE = '/vendors';

// Get vendor by ID
export const getVendorById = (id) =>
  api.get(`${VENDOR_BASE}/${id}`);

// Search vendors
export const searchVendors = ({ city, zip, vendorName, serviceCategoryIds } = {}) =>
  api.get(`${VENDOR_BASE}/search`, {
    params: { city, zip, vendorName, serviceCategoryIds: serviceCategoryIds?.join(',') },
  });

// Search by brand name
export const searchByBrandName = (name) =>
  api.get(`${VENDOR_BASE}/brandname/${name}`);

// Vendor services (branches) of one type, e.g. CAR_WASH
export const getVendorServicesByType = (serviceType) =>
  api.get(`${VENDOR_BASE}/services/all`, { params: { serviceType } });

// A vendor's services (branches)
export const getVendorServices = (vendorId) =>
  api.get(`${VENDOR_BASE}/${vendorId}/services`);
```

---

### 9.6 Booking APIs

```javascript
// api/customer/bookingApi.js
import api from '../axiosInstance';

const BOOKING_BASE = '/bookings';

// Get available slots for a vendor on a date
export const getAvailableSlots = (vendorServiceId, date) =>
  api.get(`${BOOKING_BASE}/${vendorServiceId}/available-slots`, {
    params: { date }, // format: dd/MM/yyyy
  });

// Book appointment (slot-based)
export const bookSlot = (vendorServiceId, vehicleId, time, userId, { catalogServiceId, selectedVendorServiceIds } = {}) =>
  api.post(`${BOOKING_BASE}/${vendorServiceId}/book`, null, {
    params: { vehicleId, time, catalogServiceId, selectedVendorServiceIds },
    headers: { 'X-Auto-Adda-Id': userId },
  });

// Create appointment (direct)
export const createAppointment = (data) =>
  api.post(BOOKING_BASE, data);
// data = {
//   customerId, vendorServiceId, catalogServiceId, vehicleId, serviceIds,
//   scheduleDate, serviceStartTime, serviceEndTime,
//   deliveryTime, comments, memberId, memberType
// }

// Get my bookings
export const getMyBookings = (userId) =>
  api.get(`${BOOKING_BASE}/user/bookings`, {
    headers: { 'X-Auto-Adda-Id': userId },
  });

// Get booking details
export const getBookingDetails = (appointmentId) =>
  api.get(`${BOOKING_BASE}/${appointmentId}`);

// Edit appointment
export const editAppointment = (bookingId, userId, { serviceId, vehicleId, time } = {}) =>
  api.put(`${BOOKING_BASE}/edit`, null, {
    params: { bookingId, serviceId, vehicleId, time },
    headers: { 'X-Auto-Adda-Id': userId },
  });

// Cancel appointment
export const cancelAppointment = (appointmentId, userId) =>
  api.delete(`${BOOKING_BASE}/${appointmentId}/cancel`, {
    headers: { 'X-Auto-Adda-Id': userId },
  });
```

---

### 9.7 Quotation APIs

```javascript
// api/customer/quotationApi.js
import api from '../axiosInstance';

const QUOTATION_BASE = '/quotations/customer';

// --- Check & Form ---

// Check if quotation is available for a category / service
export const checkQuotationAvailable = (categoryId, serviceId) =>
  api.get(`${QUOTATION_BASE}/check/category/${categoryId}`, { params: { serviceId } });

// Get quotation form fields for a category / service
export const getQuotationFormFields = (categoryId, serviceId) =>
  api.get(`${QUOTATION_BASE}/form/category/${categoryId}`, { params: { serviceId } });

// --- Submit & Track Requests ---

// Submit a quotation request
export const submitQuotationRequest = (data) =>
  api.post(`${QUOTATION_BASE}/request`, data);
// data = {
//   serviceCategoryId, serviceId, customerId, serviceLocationType,
//   city, area, address, latitude, longitude,
//   customerNotes, fieldValues: { field_code: value }
// }

// Get my quotation requests
export const getMyRequests = (customerId) =>
  api.get(`${QUOTATION_BASE}/requests`, { params: { customerId } });

// Get my requests (paginated)
export const getMyRequestsPaginated = (customerId, page = 0, size = 10) =>
  api.get(`${QUOTATION_BASE}/requests/page`, {
    params: { customerId, page, size },
  });

// Get request by ID
export const getRequestById = (requestId) =>
  api.get(`${QUOTATION_BASE}/request/${requestId}`);

// Get request by quotation number
export const getRequestByNumber = (quotationNumber) =>
  api.get(`${QUOTATION_BASE}/request/number/${quotationNumber}`);

// Get field values for a request
export const getRequestFieldValues = (requestId) =>
  api.get(`${QUOTATION_BASE}/request/${requestId}/fields`);

// Cancel a request
export const cancelRequest = (requestId, customerId) =>
  api.post(`${QUOTATION_BASE}/request/${requestId}/cancel`, null, {
    params: { customerId },
  });

// --- View & Respond to Quotes ---

// Get vendor quotes received for a request
export const getQuotesForRequest = (requestId) =>
  api.get(`${QUOTATION_BASE}/request/${requestId}/quotes`);

// Get quote details
export const getQuoteDetails = (quotationId) =>
  api.get(`${QUOTATION_BASE}/quote/${quotationId}`);

// Get quote line items
export const getQuoteLineItems = (quotationId) =>
  api.get(`${QUOTATION_BASE}/quote/${quotationId}/items`);

// Accept a quote
export const acceptQuote = (quotationId, customerId) =>
  api.post(`${QUOTATION_BASE}/quote/${quotationId}/accept`, { customerId });

// Reject a quote
export const rejectQuote = (quotationId, customerId, reason = null) =>
  api.post(`${QUOTATION_BASE}/quote/${quotationId}/reject`, { customerId, reason });

// --- Book from Quotation ---

// Create booking from accepted quotation
export const bookFromQuotation = (quotationId, data) =>
  api.post(`${QUOTATION_BASE}/quote/${quotationId}/book`, data);
// data = { customerId, scheduleDate, vehicleId, comments }
```

---

### 9.8 Media File APIs

```javascript
// api/customer/mediaApi.js
import api from '../axiosInstance';

const MEDIA_BASE = '/mediafiles';

// Upload files (for quotation request photos, etc.)
export const uploadFiles = (files) => {
  const formData = new FormData();
  files.forEach((file) => formData.append('files', file));
  return api.post(`${MEDIA_BASE}/upload`, formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
    timeout: 60000,
  });
};
```

---

### 9.9 Usage Examples (React Components)

#### Homepage - Category Grid + Featured

```javascript
import React, { useEffect, useState } from 'react';
import { getRootCategories } from '../api/customer/categoryApi';
import { searchFeatured } from '../api/customer/searchApi';

const HomePage = () => {
  const [categories, setCategories] = useState([]);
  const [featured, setFeatured] = useState([]);

  useEffect(() => {
    const load = async () => {
      const [catRes, featRes] = await Promise.all([
        getRootCategories(),
        searchFeatured(null, 0, 8),
      ]);
      setCategories(catRes.data.data);
      setFeatured(featRes.data.results || []);
    };
    load();
  }, []);

  return (
    <div>
      <h2>Services</h2>
      <div className="category-grid">
        {categories.map((cat) => (
          <div key={cat.id} onClick={() => navigate(`/category/${cat.id}`)}>
            {cat.name}
          </div>
        ))}
      </div>
      <h2>Featured</h2>
      <div className="offering-grid">
        {featured.map((item) => (
          <div key={item.id}>{item.offeringName} - {item.price} AED</div>
        ))}
      </div>
    </div>
  );
};
```

#### Search Page with Dynamic Filters

```javascript
import React, { useEffect, useState } from 'react';
import { advancedSearch, getFilterOptions } from '../api/customer/searchApi';

const SearchPage = ({ categoryId }) => {
  const [filters, setFilters] = useState({});
  const [results, setResults] = useState([]);
  const [selectedFilters, setSelectedFilters] = useState({});
  const [page, setPage] = useState(0);

  useEffect(() => {
    getFilterOptions(categoryId).then((res) => setFilters(res.data));
  }, [categoryId]);

  useEffect(() => {
    const search = async () => {
      const res = await advancedSearch({
        categoryId,
        attributes: selectedFilters,
        page,
        size: 20,
        includeFacets: true,
        includeVendorDetails: true,
      });
      setResults(res.data.results || []);
    };
    search();
  }, [categoryId, selectedFilters, page]);

  return (
    <div className="search-layout">
      <aside className="filter-sidebar">
        {Object.entries(filters).map(([key, values]) => (
          <div key={key}>
            <h4>{key}</h4>
            {values.map((v) => (
              <label key={v.value}>
                <input
                  type="checkbox"
                  onChange={() => {
                    setSelectedFilters((prev) => ({
                      ...prev,
                      [key]: prev[key]?.includes(v.value)
                        ? prev[key].filter((x) => x !== v.value)
                        : [...(prev[key] || []), v.value],
                    }));
                  }}
                />
                {v.value} ({v.count})
              </label>
            ))}
          </div>
        ))}
      </aside>
      <main>
        {results.map((r) => (
          <div key={r.id}>{r.offeringName} - {r.price} {r.currency}</div>
        ))}
        <button onClick={() => setPage((p) => p + 1)}>Next Page</button>
      </main>
    </div>
  );
};
```

#### Booking Flow

```javascript
import React, { useEffect, useState } from 'react';
import { getAvailableSlots, bookSlot } from '../api/customer/bookingApi';

// vendorServiceId comes from GET /service-offerings/{id}; catalogServiceId is the service picked in 1.4
const BookingSlotPicker = ({ vendorServiceId, catalogServiceId, userId, vehicleId }) => {
  const [date, setDate] = useState('');
  const [slots, setSlots] = useState([]);

  const loadSlots = async (selectedDate) => {
    // API expects dd/MM/yyyy
    const formatted = selectedDate.split('-').reverse().join('/');
    const res = await getAvailableSlots(vendorServiceId, formatted);
    setSlots(res.data.availableSlots || []);
  };

  const handleBook = async (slot) => {
    const isoTime = `${date}T${slot.startTime.slice(0, 5)}:00`;
    try {
      await bookSlot(vendorServiceId, vehicleId, isoTime, userId, { catalogServiceId });
      alert('Booked successfully!');
    } catch (err) {
      alert('Booking failed: ' + err.response?.data?.message);
    }
  };

  return (
    <div>
      <input
        type="date"
        value={date}
        onChange={(e) => { setDate(e.target.value); loadSlots(e.target.value); }}
      />
      <div className="slots">
        {slots.filter((s) => s.isAvailable).map((s) => (
          <button key={s.slotId} onClick={() => handleBook(s)}>
            {s.startTime} - {s.endTime}
          </button>
        ))}
      </div>
    </div>
  );
};
```

#### Quotation Request Flow

```javascript
import React, { useEffect, useState } from 'react';
import {
  checkQuotationAvailable,
  getQuotationFormFields,
  submitQuotationRequest,
  getQuotesForRequest,
  acceptQuote,
  bookFromQuotation,
} from '../api/customer/quotationApi';
import { uploadFiles } from '../api/customer/mediaApi';

const parseOptions = (json) =>
  (json ? JSON.parse(json) : []).map((o) => (typeof o === 'object' ? o : { value: o, label: o }));

const QuotationRequestForm = ({ categoryId, serviceId, customerId }) => {
  const [enabled, setEnabled] = useState(false);
  const [fields, setFields] = useState([]);
  const [formValues, setFormValues] = useState({});

  useEffect(() => {
    const init = async () => {
      const checkRes = await checkQuotationAvailable(categoryId, serviceId);
      setEnabled(checkRes.data.data);
      if (checkRes.data.data) {
        const fieldRes = await getQuotationFormFields(categoryId, serviceId);
        setFields(fieldRes.data.data);
      }
    };
    init();
  }, [categoryId, serviceId]);

  const handleSubmit = async () => {
    // Handle file uploads first
    const fieldValuesWithFiles = { ...formValues };
    for (const field of fields) {
      if (field.fieldType === 'FILE_UPLOAD' && formValues[field.fieldCode]) {
        const uploadRes = await uploadFiles(formValues[field.fieldCode]);
        fieldValuesWithFiles[field.fieldCode] = uploadRes.data.data.map((f) => f.id);
      }
    }

    const res = await submitQuotationRequest({
      serviceCategoryId: categoryId,
      serviceId,
      customerId,
      serviceLocationType: 'AT_VENDOR',
      city: 'Dubai',
      fieldValues: fieldValuesWithFiles,
    });

    alert(`Request submitted: ${res.data.data.quotationNumber}`);
    return res.data.data;
  };

  if (!enabled) return <p>Quotation not available for this category</p>;

  return (
    <form onSubmit={(e) => { e.preventDefault(); handleSubmit(); }}>
      {fields
        .sort((a, b) => a.displayOrder - b.displayOrder)
        .map((field) => (
          <div key={field.id}>
            <label>{field.label} {field.isRequired && '*'}</label>
            {field.fieldType === 'TEXT' && (
              <input
                placeholder={field.placeholder}
                required={field.isRequired}
                onChange={(e) => setFormValues((v) => ({ ...v, [field.fieldCode]: e.target.value }))}
              />
            )}
            {field.fieldType === 'TEXTAREA' && (
              <textarea
                placeholder={field.placeholder}
                required={field.isRequired}
                onChange={(e) => setFormValues((v) => ({ ...v, [field.fieldCode]: e.target.value }))}
              />
            )}
            {field.fieldType === 'DROPDOWN' && (
              <select
                required={field.isRequired}
                onChange={(e) => setFormValues((v) => ({ ...v, [field.fieldCode]: e.target.value }))}
              >
                <option value="">{field.placeholder || 'Select...'}</option>
                {parseOptions(field.options).map((opt) => (
                  <option key={opt.value} value={opt.value}>{opt.label}</option>
                ))}
              </select>
            )}
            {field.fieldType === 'NUMBER' && (
              <input
                type="number"
                placeholder={field.placeholder}
                required={field.isRequired}
                onChange={(e) => setFormValues((v) => ({ ...v, [field.fieldCode]: e.target.value }))}
              />
            )}
            {field.fieldType === 'FILE_UPLOAD' && (
              <input
                type="file"
                multiple
                onChange={(e) => setFormValues((v) => ({ ...v, [field.fieldCode]: Array.from(e.target.files) }))}
              />
            )}
            {field.fieldType === 'BOOLEAN' && (
              <input
                type="checkbox"
                onChange={(e) => setFormValues((v) => ({ ...v, [field.fieldCode]: e.target.checked }))}
              />
            )}
            {field.fieldType === 'DATE' && (
              <input
                type="date"
                required={field.isRequired}
                onChange={(e) => setFormValues((v) => ({ ...v, [field.fieldCode]: e.target.value }))}
              />
            )}
            {field.helpText && <small>{field.helpText}</small>}
          </div>
        ))}
      <button type="submit">Submit Quotation Request</button>
    </form>
  );
};
```
