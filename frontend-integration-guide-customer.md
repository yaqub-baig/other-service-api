# Frontend Integration Guide - Customer App

> **Base URL:** `{API_BASE_URL}` (e.g., `http://localhost:8080`)
> **Authentication Header:** `X-Auto-Adda-Id: {userId}` (used in booking-related endpoints)
> **Standard Response Wrapper:**
> ```json
> {
>   "status": "success",
>   "code": "200",
>   "message": "success",
>   "data": { ... }
> }
> ```

---

## Table of Contents

1. [Browse Service Categories](#1-browse-service-categories)
2. [Search & Discover Offerings](#2-search--discover-offerings)
3. [View Vendor Profiles](#3-view-vendor-profiles)
4. [Booking Flow](#4-booking-flow)
5. [Quotation Flow](#5-quotation-flow)
6. [My Bookings Management](#6-my-bookings-management)
7. [Recommended Page Structure](#7-recommended-page-structure)
8. [Complete User Journeys](#8-complete-user-journeys)

---

## 1. Browse Service Categories

**Base Path:** `/service-categories`

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
      "name": "Tyres",
      "description": "Tyre sales and fitting",
      "imageId": 45,
      "parentId": null
    },
    {
      "id": 2,
      "name": "Car Wash",
      "description": "Professional car wash services",
      "imageId": 46,
      "parentId": null
    }
  ]
}
```

### 1.2 Get Sub-Categories

```
GET /service-categories/parent/{parentId}
```

### 1.3 Get Category Details

```
GET /service-categories/{id}
```

### Frontend Usage

```
Homepage -> Category grid (icons + names from root categories)
  -> Click category -> Show sub-categories if any
  -> Click leaf category -> Go to search/listings page
```

---

## 2. Search & Discover Offerings

**Base Path:** `/search`

This is the primary discovery mechanism for customers to find services and products.

### 2.1 Quick Search (GET - Simple)

```
GET /search/offerings?categoryId=1&q=MRF&minPrice=2000&maxPrice=10000&city=Dubai&sortBy=price&sortDir=asc&page=0&size=20
```

| Param      | Type    | Default    | Description                          |
|------------|---------|------------|--------------------------------------|
| categoryId | Integer | -          | Category to search in                |
| q          | String  | -          | Search query text                    |
| minPrice   | Double  | -          | Minimum price filter                 |
| maxPrice   | Double  | -          | Maximum price filter                 |
| city       | String  | -          | Filter by city                       |
| area       | String  | -          | Filter by area within city           |
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
  "categoryId": 1,
  "query": "MRF tyre",
  "minPrice": 2000,
  "maxPrice": 10000,
  "pricingTypes": ["FIXED", "RANGE"],
  "stockStatuses": ["IN_STOCK", "LIMITED_STOCK"],
  "attributes": {
    "brand": "MRF",
    "width": ["185", "195"],
    "rim_size": "15"
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

**Response:** `OfferingSearchResponse`

```json
{
  "results": [
    {
      "id": 101,
      "offeringName": "MRF ZLX 185/65 R15",
      "shortDescription": "Tubeless Car Tyre",
      "price": 3500.00,
      "originalPrice": 4000.00,
      "currency": "AED",
      "imageUrl": "https://...",
      "vendorName": "QuickFit Tyres",
      "vendorId": 1,
      "distance": 2.5,
      "rating": 4.5,
      "stockStatus": "IN_STOCK"
    }
  ],
  "facets": {
    "brand": [
      { "value": "MRF", "count": 15 },
      { "value": "Bridgestone", "count": 10 }
    ],
    "width": [
      { "value": "185", "count": 8 },
      { "value": "195", "count": 12 }
    ]
  },
  "totalResults": 45,
  "page": 0,
  "size": 20,
  "totalPages": 3
}
```

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
  "brand": [
    { "value": "MRF", "count": 15 },
    { "value": "Bridgestone", "count": 10 }
  ],
  "width": [
    { "value": "185", "count": 8 },
    { "value": "195", "count": 12 }
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
// filterOptions = { "brand": [...], "width": [...], "price_range": [...] }

// Step 3: When user applies filters, build search request
const searchRequest = {
  categoryId: categoryId,
  attributes: {
    brand: selectedBrands,     // from MULTI_SELECT
    width: selectedWidths      // from MULTI_SELECT
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

**Response:** `VendorProfileResponse`
```json
{
  "id": 1,
  "companyBrandName": "QuickFit Tyres",
  "webSite": "https://quickfit.ae",
  "email": "info@quickfit.ae",
  "phoneNumber": "+971501234567",
  "openingTime": "08:00",
  "closingTime": "20:00",
  "workingDays": "Mon-Sat",
  "companyLogo": {
    "id": 10,
    "fileUrl": "https://..."
  },
  "companyLocation": {
    "address": "Al Quoz Industrial Area",
    "city": "Dubai",
    "latitude": 25.2048,
    "longitude": 55.2708
  },
  "companyImages": [
    { "id": 13, "fileUrl": "https://..." }
  ]
}
```

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

**Base Path:** `/os-appointments`

### 4.1 Check Available Slots

```
GET /os-appointments/{otherServiceId}/available-slots?date=25/04/2026
```

| Param          | Type      | Format      | Description        |
|----------------|-----------|-------------|--------------------|
| otherServiceId | Integer   | path        | Vendor ID          |
| date           | LocalDate | dd/MM/yyyy  | Date to check      |

**Response:**
```json
{
  "garageId": 1,
  "date": "2026-04-25",
  "availableSlots": [
    {
      "slotId": 1,
      "startTime": "09:00",
      "endTime": "10:00",
      "available": true,
      "technicianId": 5
    },
    {
      "slotId": 2,
      "startTime": "10:00",
      "endTime": "11:00",
      "available": true,
      "technicianId": 5
    }
  ]
}
```

### 4.2 Book Appointment (Slot-Based)

```
POST /os-appointments/{serviceId}/book?vehicleId=10&time=2026-04-25T09:00:00&selectedOtherServiceIds=1,3
X-Auto-Adda-Id: {userId}
```

| Param                   | Type          | Description                         |
|-------------------------|---------------|-------------------------------------|
| serviceId               | Integer       | path - Vendor service ID            |
| vehicleId               | Integer       | query - Customer's vehicle ID       |
| time                    | LocalDateTime | query - ISO datetime                |
| selectedOtherServiceIds | String        | query - Comma-separated service IDs |

**Header:** `X-Auto-Adda-Id: {userId}`

**Response:** `ApiResponse<BookingDetailResponse>`

### 4.3 Create Appointment (Direct)

```
POST /os-appointments
Content-Type: application/json
```

**Request Body:**
```json
{
  "customerId": 100,
  "otherServiceId": 1,
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

### 4.4 Edit Appointment

```
PUT /os-appointments/edit/appointment?appointmentId=123&otherServiceId=1&vehicleId=10&time=2026-04-26T10:00:00
X-Auto-Adda-Id: {userId}
```

| Param          | Type          | Required | Description           |
|----------------|---------------|----------|-----------------------|
| appointmentId  | Integer       | Yes      | Booking ID to edit    |
| otherServiceId | Integer       | No       | Change vendor         |
| vehicleId      | Integer       | No       | Change vehicle        |
| time           | LocalDateTime | No       | Change date/time      |

### 4.5 Cancel Appointment

```
DELETE /os-appointments/{appointmentId}/cancel
X-Auto-Adda-Id: {userId}
```

---

## 5. Quotation Flow

This is a multi-step flow where a customer requests quotes from vendors.

### Step 1: Check if Quotation is Available

```
GET /quotations/customer/check/category/{categoryId}
```

**Response:** `ApiResponse<Boolean>`

### Step 2: Get Quotation Form Fields

```
GET /quotations/customer/form/category/{categoryId}
```

**Response:** `ApiResponse<List<QuotationFieldDefinitionResponse>>`

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
      "options": ["Toyota", "Honda", "BMW"],
      "validationRules": {},
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
      "dependsOnCondition": { "hasValue": true }
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
| serviceCategoryId   | Integer | Yes      | Category for the quotation                         |
| customerId          | Integer | Yes      | Customer ID                                        |
| serviceLocationType | Enum    | Yes      | `AT_VENDOR`, `AT_CUSTOMER`, `EITHER`               |
| city                | String  | No       | Customer's city                                    |
| area                | String  | No       | Customer's area                                    |
| address             | String  | No       | Full address (for AT_CUSTOMER)                     |
| latitude            | Double  | No       | Location lat                                       |
| longitude           | Double  | No       | Location lng                                       |
| customerNotes       | String  | No       | Additional notes                                   |
| fieldValues         | Map     | Yes      | Dynamic field values matching form field codes      |

**Response:** `ApiResponse<QuotationRequestResponse>`

```json
{
  "data": {
    "id": 50,
    "quotationNumber": "QR-20260425-001",
    "serviceCategory": { "id": 1, "name": "AC Service" },
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
          "total": 800.00
        },
        {
          "itemType": "PART",
          "title": "AC Filter",
          "quantity": 1,
          "unitPrice": 700.00,
          "total": 700.00
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

**Quotation Request Statuses:**
| Status           | Description                              |
|------------------|------------------------------------------|
| REQUESTED        | Initial - just submitted                 |
| SENT_TO_VENDORS  | Broadcast to matching vendors            |
| QUOTES_RECEIVED  | At least one vendor has responded        |
| ACCEPTED         | Customer accepted a vendor's quote       |
| EXPIRED          | Auto-expired without acceptance          |
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

**Base Path:** `/os-appointments`

### 6.1 Get My Bookings

```
GET /os-appointments/user/bookings
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
      "status": "CONFIRMED",
      "comments": "Check alignment too",
      "vendor": {
        "id": 1,
        "companyBrandName": "QuickFit Tyres"
      },
      "servicesResponse": [
        { "id": 1, "name": "Tyre Replacement" }
      ],
      "customerId": 100,
      "vehicleId": 10,
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
GET /os-appointments/{appointmentId}
```

### 6.3 Edit Booking

```
PUT /os-appointments/edit/appointment?appointmentId=123&time=2026-04-26T10:00:00
X-Auto-Adda-Id: {userId}
```

### 6.4 Cancel Booking

```
DELETE /os-appointments/{appointmentId}/cancel
X-Auto-Adda-Id: {userId}
```

---

## 7. Recommended Page Structure

```
/
  /home                         -- Root categories + featured offerings
  /category/:id                 -- Sub-categories or offerings listing
  /search                       -- Search results with filters
  /offering/:id                 -- Offering detail page
  /vendor/:id                   -- Vendor profile page
  /vendor/:id/offerings         -- Vendor's offerings
  /booking
    /select-slot/:vendorId      -- Slot selection calendar
    /confirm                    -- Booking confirmation
  /quotation
    /request/:categoryId        -- Quotation request form
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
2. Select category              GET /service-categories/parent/{id}
3. Load filter options           GET /search/filters/{categoryId}
4. Search offerings             POST /search/offerings
5. View offering details         GET /service-offerings/{id}
6. View vendor profile           GET /vendors/{vendorId}
7. Check available slots         GET /os-appointments/{vendorId}/available-slots?date=...
8. Book slot                     POST /os-appointments/{serviceId}/book?vehicleId=...&time=...
9. View my bookings              GET /os-appointments/user/bookings
```

### Journey 2: Request Quote & Book

```
1. Browse categories             GET /service-categories/root
2. Check quotation available     GET /quotations/customer/check/category/{id}
3. Load quotation form           GET /quotations/customer/form/category/{id}
4. Submit quotation request      POST /quotations/customer/request
5. Wait for vendor quotes...
6. View received quotes          GET /quotations/customer/request/{id}/quotes
7. Compare quotes (view items)   GET /quotations/customer/quote/{id}/items
8. Accept best quote             POST /quotations/customer/quote/{id}/accept
9. Book from accepted quote      POST /quotations/customer/quote/{id}/book
10. View my bookings             GET /os-appointments/user/bookings
```

### Journey 3: Nearby Search

```
1. Get user location (browser geolocation API)
2. Search nearby                 GET /search/nearby?lat=...&lng=...&radius=10
3. View offering/vendor          GET /service-offerings/{id}
4. Book appointment              POST /os-appointments/{serviceId}/book?...
```

### Frontend Code Example: Complete Quotation Flow

```javascript
const API = 'http://localhost:8080';

// 1. Check if quotation is available
const { data: enabled } = await fetch(
  `${API}/quotations/customer/check/category/${categoryId}`
).then(r => r.json());

if (!enabled) {
  // Redirect to direct booking flow
  return;
}

// 2. Load form fields
const { data: fields } = await fetch(
  `${API}/quotations/customer/form/category/${categoryId}`
).then(r => r.json());

// 3. Render dynamic form based on fields
// Handle field types: TEXT, NUMBER, DROPDOWN, MULTI_SELECT, BOOLEAN, 
//                     FILE_UPLOAD, DATE, TEXTAREA
// Handle conditional fields: check dependsOnFieldId + dependsOnCondition

// 4. Submit request
const { data: request } = await fetch(`${API}/quotations/customer/request`, {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({
    serviceCategoryId: categoryId,
    customerId: currentUser.id,
    serviceLocationType: 'AT_VENDOR',
    city: 'Dubai',
    customerNotes: 'Need it done ASAP',
    fieldValues: formValues  // { field_code: value, ... }
  })
}).then(r => r.json());

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
  baseURL: process.env.REACT_APP_API_BASE_URL || 'http://localhost:8080',
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 30000,
});

// Attach X-Auto-Adda-Id header for authenticated requests
export const setAuthHeader = (userId) => {
  api.defaults.headers.common['X-Auto-Adda-Id'] = userId;
};

// Response interceptor
api.interceptors.response.use(
  (response) => response,
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
```

---

### 9.3 Search & Discovery APIs

```javascript
// api/customer/searchApi.js
import api from '../axiosInstance';

const SEARCH_BASE = '/search';

// Quick search (GET with query params)
export const quickSearch = ({
  categoryId, q, minPrice, maxPrice, city, area,
  lat, lng, radius, sortBy = 'relevance', sortDir = 'asc',
  page = 0, size = 20,
} = {}) =>
  api.get(`${SEARCH_BASE}/offerings`, {
    params: { categoryId, q, minPrice, maxPrice, city, area, lat, lng, radius, sortBy, sortDir, page, size },
  });

// Advanced search (POST with full body)
export const advancedSearch = (searchRequest) =>
  api.post(`${SEARCH_BASE}/offerings`, searchRequest);
// searchRequest = {
//   categoryId, query, minPrice, maxPrice, pricingTypes, stockStatuses,
//   attributes: { brand: "MRF", width: ["185","195"] },
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

// Get vendors by service type
export const getVendorsByServiceType = (serviceType) =>
  api.get(`${VENDOR_BASE}/service/${serviceType}`);
```

---

### 9.6 Booking APIs

```javascript
// api/customer/bookingApi.js
import api from '../axiosInstance';

const BOOKING_BASE = '/os-appointments';

// Get available slots for a vendor on a date
export const getAvailableSlots = (otherServiceId, date) =>
  api.get(`${BOOKING_BASE}/${otherServiceId}/available-slots`, {
    params: { date }, // format: dd/MM/yyyy
  });

// Book appointment (slot-based)
export const bookSlot = (serviceId, vehicleId, time, userId, selectedOtherServiceIds = null) =>
  api.post(`${BOOKING_BASE}/${serviceId}/book`, null, {
    params: { vehicleId, time, selectedOtherServiceIds },
    headers: { 'X-Auto-Adda-Id': userId },
  });

// Create appointment (direct)
export const createAppointment = (data) =>
  api.post(BOOKING_BASE, data);
// data = {
//   customerId, otherServiceId, vehicleId, serviceIds,
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
export const editAppointment = (appointmentId, userId, { otherServiceId, vehicleId, time } = {}) =>
  api.put(`${BOOKING_BASE}/edit/appointment`, null, {
    params: { appointmentId, otherServiceId, vehicleId, time },
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

// Check if quotation is available for a category
export const checkQuotationAvailable = (categoryId) =>
  api.get(`${QUOTATION_BASE}/check/category/${categoryId}`);

// Get quotation form fields for a category
export const getQuotationFormFields = (categoryId) =>
  api.get(`${QUOTATION_BASE}/form/category/${categoryId}`);

// --- Submit & Track Requests ---

// Submit a quotation request
export const submitQuotationRequest = (data) =>
  api.post(`${QUOTATION_BASE}/request`, data);
// data = {
//   serviceCategoryId, customerId, serviceLocationType,
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

const BookingSlotPicker = ({ vendorId, serviceId, userId, vehicleId }) => {
  const [date, setDate] = useState('');
  const [slots, setSlots] = useState([]);

  const loadSlots = async (selectedDate) => {
    // API expects dd/MM/yyyy
    const formatted = selectedDate.split('-').reverse().join('/');
    const res = await getAvailableSlots(vendorId, formatted);
    setSlots(res.data.availableSlots || []);
  };

  const handleBook = async (slot) => {
    const isoTime = `${date}T${slot.startTime}:00`;
    try {
      const res = await bookSlot(serviceId, vehicleId, isoTime, userId);
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
        {slots.filter((s) => s.available).map((s) => (
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

const QuotationRequestForm = ({ categoryId, customerId }) => {
  const [enabled, setEnabled] = useState(false);
  const [fields, setFields] = useState([]);
  const [formValues, setFormValues] = useState({});

  useEffect(() => {
    const init = async () => {
      const checkRes = await checkQuotationAvailable(categoryId);
      setEnabled(checkRes.data.data);
      if (checkRes.data.data) {
        const fieldRes = await getQuotationFormFields(categoryId);
        setFields(fieldRes.data.data);
      }
    };
    init();
  }, [categoryId]);

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
                {field.options?.map((opt) => (
                  <option key={opt} value={opt}>{opt}</option>
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
