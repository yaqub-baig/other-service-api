# Frontend Integration Guide - Vendor Panel

> **Base URL:** `{API_BASE_URL}` (e.g., `http://localhost:8080`)
> **Authentication Header:** `X-Auto-Adda-Id: {vendorOwnerUserId}` (used in vendor registration)
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

1. [Vendor Registration & Profile](#1-vendor-registration--profile)
2. [Vendor Service Management](#2-vendor-service-management)
3. [Service Offerings (Product/Service Listings)](#3-service-offerings-productservice-listings)
4. [Quotation Management](#4-quotation-management)
5. [Booking Management (Vendor View)](#5-booking-management-vendor-view)
6. [Media File Uploads](#6-media-file-uploads)
7. [Recommended Page Structure](#7-recommended-page-structure)
8. [Complete Vendor Journeys](#8-complete-vendor-journeys)

---

## 1. Vendor Registration & Profile

**Base Path:** `/vendors`

### 1.1 Register as Vendor

```
POST /vendors
X-Auto-Adda-Id: {userId}
Content-Type: application/json
```

**Request Body:**
```json
{
  "companyBrandName": "QuickFit Tyres LLC",
  "companyArabicName": "كويك فيت",
  "name": "QuickFit",
  "trnNumber": "TRN123456789",
  "webSite": "https://quickfit.ae",
  "email": "info@quickfit.ae",
  "phoneNumber": "+971-4-1234567",
  "mobileNumber": "+971501234567",
  "companyLocation": {
    "address": "Al Quoz Industrial Area 3, Street 8",
    "city": "Dubai",
    "state": "Dubai",
    "zipCode": "12345",
    "latitude": 25.2048,
    "longitude": 55.2708
  },
  "osServiceIds": [1, 3, 5],
  "tradeLicenceNumber": "DED-123456",
  "openingTime": "08:00",
  "closingTime": "20:00",
  "workingDays": "Mon-Sat",
  "logoId": 10,
  "vatCertificateIds": [11],
  "tradeLicenseAttachmentIds": [12],
  "companyImagesIds": [13, 14, 15]
}
```

| Field                      | Type          | Required | Description                                   |
|----------------------------|---------------|----------|-----------------------------------------------|
| companyBrandName           | String        | Yes      | Company brand name (English)                  |
| companyArabicName          | String        | No       | Company name in Arabic                        |
| name                       | String        | Yes      | Short name                                    |
| trnNumber                  | String        | No       | Tax Registration Number                       |
| webSite                    | String        | No       | Company website URL                           |
| email                      | String        | Yes      | Contact email                                 |
| phoneNumber                | String        | Yes      | Landline phone                                |
| mobileNumber               | String        | No       | Mobile phone                                  |
| companyLocation            | Object        | Yes      | Location details (see below)                  |
| companyLocation.address    | String        | Yes      | Street address                                |
| companyLocation.city       | String        | Yes      | City name                                     |
| companyLocation.state      | String        | No       | State/emirate                                 |
| companyLocation.zipCode    | String        | No       | ZIP/postal code                               |
| companyLocation.latitude   | Double        | Yes      | GPS latitude                                  |
| companyLocation.longitude  | Double        | Yes      | GPS longitude                                 |
| osServiceIds               | List<Integer> | Yes      | Service category IDs vendor offers             |
| tradeLicenceNumber         | String        | No       | Trade license number                          |
| openingTime                | String        | Yes      | Opening time (HH:mm)                          |
| closingTime                | String        | Yes      | Closing time (HH:mm)                          |
| workingDays                | String        | Yes      | Working days (e.g., "Mon-Sat")                |
| logoId                     | Long          | No       | MediaFile ID for logo (upload first)          |
| vatCertificateIds          | List<Long>    | No       | MediaFile IDs for VAT certificates            |
| tradeLicenseAttachmentIds  | List<Long>    | No       | MediaFile IDs for trade license docs          |
| companyImagesIds           | List<Long>    | No       | MediaFile IDs for company photos              |

**Response:** `VendorProfileResponse`

### 1.2 Get My Vendor Profiles

```
GET /vendors
X-Auto-Adda-Id: {userId}
```

Returns all vendor profiles owned by this user.

### 1.3 Get Vendor by ID

```
GET /vendors/{id}
```

### 1.4 Update Vendor Profile

```
PUT /vendors/{id}
Content-Type: application/json
```

Same request body as registration.

### 1.5 Delete Vendor

```
DELETE /vendors/{id}
```

### Registration Flow

```
1. Upload logo                POST /mediafiles/upload
2. Upload trade license       POST /mediafiles/upload
3. Upload VAT certificate     POST /mediafiles/upload
4. Upload company images      POST /mediafiles/upload
5. Get available categories   GET /service-categories
6. Submit registration        POST /vendors (with all IDs from uploads)
```

---

## 2. Vendor Service Management

**Base Path:** `/vendor-services`

Links your vendor to specific service categories (what services you offer).

### 2.1 Get My Services

```
GET /vendor-services/vendor/{vendorId}
```

**Response:** `ApiResponse<List<VendorServiceBridgeResponse>>`

```json
{
  "data": [
    {
      "id": 1,
      "vendorId": 1,
      "serviceId": 3,
      "serviceName": "Tyres",
      "isActive": true
    }
  ]
}
```

### 2.2 Add a Service

```
POST /vendor-services
Content-Type: application/json
```

```json
{
  "vendorId": 1,
  "serviceCategoryId": 3,
  "isActive": true
}
```

| Field     | Type    | Required | Description                |
|-----------|---------|----------|----------------------------|
| vendorId          | Integer | Yes      | Your vendor ID             |
| serviceCategoryId | Integer | Yes      | Service category ID to add |
| isActive  | Boolean | No       | Active status              |

### 2.3 Update a Service Link

```
PUT /vendor-services/{id}
Content-Type: application/json
```

### 2.4 Remove a Service

```
DELETE /vendor-services/{id}
```

---

## 3. Service Offerings (Product/Service Listings)

**Base Path:** `/service-offerings`

Create and manage your sellable items/services within each service category.

### 3.1 Create Offering

```
POST /service-offerings
Content-Type: application/json
```

**Request Body:**
```json
{
  "vendorServiceId": 1,
  "offeringName": "MRF ZLX 185/65 R15 Tubeless Car Tyre",
  "shortDescription": "High-performance tubeless tyre for sedans",
  "description": "MRF ZLX is a premium tubeless radial tyre...",
  "sku": null,
  "pricingType": "FIXED",
  "price": 3500.00,
  "minPrice": null,
  "maxPrice": null,
  "originalPrice": 4000.00,
  "currency": "AED",
  "stockStatus": "IN_STOCK",
  "quantityAvailable": 50,
  "filterableAttributes": {
    "brand": "MRF",
    "width": "185",
    "height": "65",
    "rim_size": "15",
    "tyre_type": "Tubeless"
  },
  "specifications": {
    "warranty": "5 years",
    "country_of_origin": "India",
    "load_index": "88",
    "speed_rating": "H"
  },
  "imageUrl": "https://...",
  "galleryImages": ["https://img1.jpg", "https://img2.jpg"],
  "displayOrder": 1,
  "isFeatured": false
}
```

| Field                | Type            | Required | Description                                    |
|----------------------|-----------------|----------|------------------------------------------------|
| vendorServiceId      | Integer         | Yes      | Your vendor services ID                  |
| offeringName         | String          | Yes      | Product/service name                           |
| shortDescription     | String          | No       | Short tagline                                  |
| description          | String          | No       | Full description                               |
| sku                  | String          | No       | SKU (auto-generated if empty)                  |
| pricingType          | Enum            | Yes      | `FIXED`, `RANGE`, `QUOTE_BASED`, `NEGOTIABLE`  |
| price                | BigDecimal      | Depends  | Price (required for FIXED)                     |
| minPrice             | BigDecimal      | Depends  | Min price (required for RANGE)                 |
| maxPrice             | BigDecimal      | Depends  | Max price (required for RANGE)                 |
| originalPrice        | BigDecimal      | No       | Original/MRP price (for showing discount)      |
| currency             | String          | No       | Currency code, defaults to "AED"               |
| stockStatus          | Enum            | No       | `IN_STOCK`, `LIMITED_STOCK`, `OUT_OF_STOCK`, `PRE_ORDER`, `DISCONTINUED` |
| quantityAvailable    | Integer         | No       | Available quantity                             |
| filterableAttributes | Map             | Yes*     | Attributes matching category attribute defs    |
| specifications       | Map             | No       | Additional specs (free-form)                   |
| imageUrl             | String          | No       | Primary image URL                              |
| galleryImages        | List<String>    | No       | Gallery image URLs                             |
| displayOrder         | Integer         | No       | Sort order in your listings                    |
| isFeatured           | Boolean         | No       | Request featured status                        |

### 3.2 Get Attribute Definitions (Before Creating)

Before creating an offering, get the attribute definitions for the category to know what `filterableAttributes` to provide.

```
GET /service-offerings/category/{categoryId}/attributes
```

**Response:** `ApiResponse<List<ServiceAttributeDefinitionResponse>>`

```json
{
  "data": [
    {
      "id": 1,
      "attributeCode": "brand",
      "attributeName": "Brand",
      "dataType": "STRING",
      "isRequired": true,
      "isFilterable": true,
      "description": "Tyre manufacturer"
    },
    {
      "id": 2,
      "attributeCode": "width",
      "attributeName": "Width (mm)",
      "dataType": "NUMBER",
      "unit": "mm",
      "isRequired": true,
      "isFilterable": true
    }
  ]
}
```

### 3.3 Get Required Attributes

```
GET /service-offerings/category/{categoryId}/attributes/required
```

### 3.4 Get My Offerings

```
GET /service-offerings/vendor/{vendorId}
```

### 3.5 Get All My Offerings (Including Inactive)

```
GET /service-offerings/vendor/{vendorId}/all
```

### 3.6 Get My Offerings by Category

```
GET /service-offerings/vendor/{vendorId}/category/{categoryId}
```

### 3.7 Get Offerings by Vendor Service

```
GET /service-offerings/vendor-service/{vendorServiceId}
GET /service-offerings/vendor-service/{vendorServiceId}/all
```

### 3.8 Get Offering by ID

```
GET /service-offerings/{id}
```

### 3.9 Get Offering by SKU

```
GET /service-offerings/sku/{sku}
```

### 3.10 Update Offering

```
PUT /service-offerings/{id}
Content-Type: application/json
```

Same body as create.

### 3.11 Soft Delete (Deactivate)

```
DELETE /service-offerings/{id}
```

### 3.12 Reactivate

```
POST /service-offerings/{id}/reactivate
```

### 3.13 Update Stock Status

```
PATCH /service-offerings/{id}/stock?status=LIMITED_STOCK&quantity=5
```

| Param    | Type       | Required | Description                                              |
|----------|------------|----------|----------------------------------------------------------|
| status   | Enum       | Yes      | `IN_STOCK`, `LIMITED_STOCK`, `OUT_OF_STOCK`, `PRE_ORDER`, `DISCONTINUED` |
| quantity | Integer    | No       | Available quantity                                       |

### 3.14 Toggle Featured

```
PATCH /service-offerings/{id}/featured
```

### Frontend Flow: Create Offering

```javascript
const API = 'http://localhost:8080';

// 1. Get the vendor services to know vendorServiceId
const { data: services } = await fetch(
  `${API}/vendor-services/vendor/${vendorId}`
).then(r => r.json());

// 2. Select a service -> get its categoryId
const selectedService = services[0]; // e.g., { id: 1, serviceId: 3 }

// 3. Load attribute definitions for the category
const { data: attributes } = await fetch(
  `${API}/service-offerings/category/${selectedService.serviceId}/attributes`
).then(r => r.json());

// 4. Render dynamic form:
//    - Fixed fields: name, description, price, images, etc.
//    - Dynamic fields: generated from attribute definitions
//    - Required attrs marked with *

// 5. Submit offering
await fetch(`${API}/service-offerings`, {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({
    vendorServiceId: selectedService.id,
    offeringName: formData.name,
    pricingType: 'FIXED',
    price: formData.price,
    filterableAttributes: {
      // Dynamic values from attribute form fields
      brand: formData.brand,
      width: formData.width,
      // ... other attributes
    },
    specifications: formData.specs,
    imageUrl: formData.mainImage,
    galleryImages: formData.gallery
  })
});
```

---

## 4. Quotation Management

**Base Path:** `/quotations/vendor`

Respond to customer quotation requests with your pricing.

### 4.1 Find Open Requests

```
GET /quotations/vendor/requests/open?vendorId=1&categoryId=3
```

| Param      | Type    | Required | Description                     |
|------------|---------|----------|---------------------------------|
| vendorId   | Integer | Yes      | Your vendor ID                  |
| categoryId | Integer | Yes      | Service category to browse      |

**Response:** `ApiResponse<List<QuotationRequestResponse>>`

```json
{
  "data": [
    {
      "id": 50,
      "quotationNumber": "QR-20260425-001",
      "serviceCategory": { "id": 3, "name": "AC Service" },
      "customerId": 100,
      "serviceLocationType": "AT_VENDOR",
      "city": "Dubai",
      "area": "Al Quoz",
      "status": "SENT_TO_VENDORS",
      "customerNotes": "AC not cooling properly",
      "expiresAt": "2026-04-28T14:30:00",
      "quotesReceivedCount": 2,
      "fieldValues": [
        {
          "fieldCode": "vehicle_make",
          "label": "Vehicle Make",
          "value": "Toyota"
        },
        {
          "fieldCode": "issue_description",
          "label": "Issue Description",
          "value": "AC not cooling, making noise"
        }
      ],
      "createdAt": "2026-04-25T14:30:00"
    }
  ]
}
```

### 4.2 Get Request Details

```
GET /quotations/vendor/request/{requestId}
```

### 4.3 Get Request Field Values

```
GET /quotations/vendor/request/{requestId}/fields
```

### 4.4 Submit a Quote

```
POST /quotations/vendor/quote
Content-Type: application/json
```

**Request Body:**
```json
{
  "quotationRequestId": 50,
  "vendorId": 1,
  "pricingType": "BREAKUP",
  "totalAmount": 1500.00,
  "minAmount": null,
  "maxAmount": null,
  "inspectionFee": 0,
  "estimatedDuration": "2-3 hours",
  "warrantyInfo": "6 months warranty on parts and labour",
  "validUntil": "2026-05-05",
  "vendorNotes": "We can do it same day if you bring it before 2 PM",
  "lineItems": [
    {
      "itemType": "SERVICE",
      "title": "AC Gas Refill (R134a)",
      "description": "Complete gas refill with leak check",
      "quantity": 1,
      "unit": "service",
      "unitPrice": 800.00,
      "discount": 0,
      "taxRate": 5.0,
      "isOptional": false
    },
    {
      "itemType": "PART",
      "title": "Cabin Air Filter",
      "description": "Genuine Toyota cabin filter",
      "quantity": 1,
      "unit": "piece",
      "unitPrice": 350.00,
      "discount": 50.00,
      "taxRate": 5.0,
      "isOptional": false
    },
    {
      "itemType": "SERVICE",
      "title": "AC Duct Cleaning",
      "description": "Full duct cleaning and sanitization",
      "quantity": 1,
      "unit": "service",
      "unitPrice": 400.00,
      "discount": 0,
      "taxRate": 5.0,
      "isOptional": true
    }
  ]
}
```

| Field              | Type            | Required | Description                                  |
|--------------------|-----------------|----------|----------------------------------------------|
| quotationRequestId | Integer         | Yes      | Request ID to quote on                       |
| vendorId           | Integer         | Yes      | Your vendor ID                               |
| pricingType        | Enum            | Yes      | `FIXED`, `RANGE`, `BREAKUP`, `INSPECTION_REQUIRED` |
| totalAmount        | BigDecimal      | Depends  | Total price (for FIXED/BREAKUP)              |
| minAmount          | BigDecimal      | Depends  | Minimum estimate (for RANGE)                 |
| maxAmount          | BigDecimal      | Depends  | Maximum estimate (for RANGE)                 |
| inspectionFee      | BigDecimal      | No       | Upfront inspection fee                       |
| estimatedDuration  | String          | No       | How long the service takes                   |
| warrantyInfo       | String          | No       | Warranty details                             |
| validUntil         | LocalDate       | No       | Quote validity date                          |
| vendorNotes        | String          | No       | Additional notes for customer                |
| lineItems          | List            | No       | Detailed line items (for BREAKUP)            |

**Line Item Fields:**

| Field       | Type       | Required | Description                      |
|-------------|------------|----------|----------------------------------|
| itemType    | String     | Yes      | `SERVICE`, `PART`, `LABOUR`, etc.|
| title       | String     | Yes      | Item name                        |
| description | String     | No       | Item description                 |
| quantity    | Integer    | Yes      | Quantity                         |
| unit        | String     | No       | Unit of measure                  |
| unitPrice   | BigDecimal | Yes      | Price per unit                   |
| discount    | BigDecimal | No       | Discount amount                  |
| taxRate     | BigDecimal | No       | Tax percentage                   |
| isOptional  | Boolean    | No       | Whether item is optional         |

**Vendor Pricing Types:**
| Type                  | When to Use                                |
|-----------------------|--------------------------------------------|
| FIXED                 | Single total price, no breakdown           |
| RANGE                 | Can't give exact price, give min-max range |
| BREAKUP               | Detailed itemized pricing                  |
| INSPECTION_REQUIRED   | Must inspect before giving final price     |

### 4.5 Get My Submitted Quotes

```
GET /quotations/vendor/quotes?vendorId=1
```

**With Pagination:**
```
GET /quotations/vendor/quotes/page?vendorId=1&page=0&size=10
```

### 4.6 Get Quote by ID

```
GET /quotations/vendor/quote/{quotationId}
```

### 4.7 Get Quote Line Items

```
GET /quotations/vendor/quote/{quotationId}/items
```

### 4.8 Withdraw a Quote

```
POST /quotations/vendor/quote/{quotationId}/withdraw?vendorId=1
```

### 4.9 Get Quotation Statistics

```
GET /quotations/vendor/stats?vendorId=1
```

**Response:**
```json
{
  "data": {
    "total": 25,
    "pending": 8,
    "accepted": 12,
    "rejected": 5
  }
}
```

---

## 5. Booking Management (Vendor View)

**Base Path:** `/bookings`

### 5.1 Get My Appointments

```
GET /bookings/vendor/{vendorId}?status=PENDING
```

| Param          | Type    | Description                                    |
|----------------|---------|------------------------------------------------|
| vendorId | Integer | Your vendor ID                                 |
| status         | String  | Optional filter: `REQUESTED`, `CONFIRMED`, `SCHEDULED`, `WORK_INPROGRESS`, `COMPLETED`, etc. |

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
      "status": "REQUESTED",
      "comments": "Check alignment too",
      "vendor": { "id": 1, "companyBrandName": "QuickFit Tyres" },
      "servicesResponse": [
        { "id": 1, "name": "Tyre Replacement" }
      ],
      "customerId": 100,
      "vehicleId": 10,
      "vehicle": {
        "make": "Toyota",
        "model": "Camry",
        "year": 2022,
        "plateNumber": "A-12345"
      },
      "slot": { "startTime": "09:00", "endTime": "10:00" }
    }
  ]
}
```

### 5.2 Get Appointment Details

```
GET /bookings/{appointmentId}
```

### 5.3 Update Appointment Status

```
PUT /bookings/status
Content-Type: application/json
```

```json
{
  "appointmentId": 123,
  "memberId": 1,
  "memberType": "VENDOR",
  "status": "CONFIRMED",
  "comments": "Confirmed, see you at 9 AM"
}
```

**Typical Vendor Status Flow:**
```
REQUESTED -> CONFIRMED -> SCHEDULED -> WORK_INPROGRESS -> COMPLETED -> DELIVERED
                                    -> WAITING_FOR_SPARES -> WORK_INPROGRESS (resume)
            -> REJECTED (decline)
            -> CANCELLED
```

| Status              | Description                                  |
|---------------------|----------------------------------------------|
| REQUESTED           | New booking from customer                    |
| CONFIRMED           | Vendor accepted the booking                  |
| SCHEDULED           | Booking is scheduled                         |
| ASSIGNED            | Assigned to a technician                     |
| WORK_INPROGRESS     | Service is being performed                   |
| WAITING_FOR_SPARES  | Paused - waiting for parts                   |
| COMPLETED           | Service completed                            |
| READY_FOR_DELIVERY  | Vehicle ready for pickup                     |
| DELIVERED           | Handed back to customer                      |
| CANCELLED           | Booking cancelled                            |
| REJECTED            | Vendor declined the booking                  |

### 5.4 Get Available Slots (Manage Capacity)

```
GET /bookings/vendor/{vendorId}/available-slots?date=25/04/2026
```

---

## 6. Media File Uploads

**Base Path:** `/mediafiles`

Used for uploading logos, images, certificates, and offering photos.

### 6.1 Upload Files

```
POST /mediafiles/upload
Content-Type: multipart/form-data
```

**Form Data:**
- `files`: One or more files (max 10MB each)

**Response:**
```json
{
  "data": [
    {
      "id": 45,
      "fileName": "logo.png",
      "fileUrl": "https://storage.example.com/logo.png",
      "fileType": "image/png",
      "fileSize": 102400
    }
  ]
}
```

### 6.2 Delete File

```
DELETE /mediafiles/delete-image/{id}
```

### Upload Pattern

```javascript
// Reusable upload function
async function uploadFiles(files) {
  const formData = new FormData();
  for (const file of files) {
    if (file.size > 10 * 1024 * 1024) {
      throw new Error(`File ${file.name} exceeds 10MB limit`);
    }
    formData.append('files', file);
  }
  
  const response = await fetch(`${API}/mediafiles/upload`, {
    method: 'POST',
    body: formData
  });
  return response.json();
}

// Usage in offering creation
const { data: images } = await uploadFiles(selectedImageFiles);
const imageUrls = images.map(img => img.fileUrl);

// Use URLs in offering
const offering = {
  imageUrl: imageUrls[0],
  galleryImages: imageUrls.slice(1),
  // ...
};
```

---

## 7. Recommended Page Structure

```
/vendor
  /dashboard                      -- Stats overview (quotation stats, booking counts)
  /profile                        -- View/edit vendor profile
  /profile/edit                   -- Edit form
  /services                       -- Manage linked service categories
  /offerings                      -- All my offerings
    /new                          -- Create offering (select service -> dynamic form)
    /:id                          -- View offering details
    /:id/edit                     -- Edit offering
  /quotations
    /open-requests                -- Browse open quotation requests
    /request/:id                  -- View request details + submit quote
    /my-quotes                    -- My submitted quotes
    /quote/:id                    -- Quote details (status, line items)
  /bookings                       -- All bookings
    /:id                          -- Booking details + status updates
  /slots                          -- Manage availability/calendar
```

---

## 8. Complete Vendor Journeys

### Journey 1: Initial Setup

```
1. Upload company logo           POST /mediafiles/upload
2. Upload trade license           POST /mediafiles/upload
3. Upload VAT certificate         POST /mediafiles/upload
4. Upload company photos          POST /mediafiles/upload
5. Browse service categories      GET /service-categories
6. Register vendor                POST /vendors (X-Auto-Adda-Id header)
7. Link services                  POST /vendor-services (for each service)
```

### Journey 2: Add a Product/Service Listing

```
1. Get my services                GET /vendor-services/vendor/{vendorId}
2. Pick a service -> category
3. Load attribute definitions     GET /service-offerings/category/{categoryId}/attributes
4. Upload product images          POST /mediafiles/upload
5. Create offering                POST /service-offerings
6. Verify listing                 GET /service-offerings/{id}
```

### Journey 3: Respond to Quotation Request

```
1. Browse open requests           GET /quotations/vendor/requests/open?vendorId=1&categoryId=3
2. View request details           GET /quotations/vendor/request/{requestId}
3. View field values              GET /quotations/vendor/request/{requestId}/fields
4. Prepare and submit quote       POST /quotations/vendor/quote
5. Track quote status             GET /quotations/vendor/quote/{quotationId}
6. Check stats                    GET /quotations/vendor/stats?vendorId=1
```

### Journey 4: Handle a Booking

```
1. View incoming bookings         GET /bookings/vendor/{vendorId}?status=REQUESTED
2. View booking details           GET /bookings/{appointmentId}
3. Confirm booking                PUT /bookings/status  { status: "CONFIRMED" }
4. Start work                     PUT /bookings/status  { status: "WORK_INPROGRESS" }
5. Complete                       PUT /bookings/status  { status: "COMPLETED" }
6. Deliver                        PUT /bookings/status  { status: "DELIVERED" }
```

### Journey 5: Manage Stock & Listings

```
1. View all offerings             GET /service-offerings/vendor/{vendorId}/all
2. Update stock                   PATCH /service-offerings/{id}/stock?status=OUT_OF_STOCK
3. Deactivate offering            DELETE /service-offerings/{id}
4. Reactivate offering            POST /service-offerings/{id}/reactivate
5. Update pricing                 PUT /service-offerings/{id} (with new price)
```

### Frontend Code Example: Submit Quotation

```javascript
const API = 'http://localhost:8080';

// 1. Find open requests for my category
const { data: requests } = await fetch(
  `${API}/quotations/vendor/requests/open?vendorId=${vendorId}&categoryId=${categoryId}`
).then(r => r.json());

// 2. View request details
const { data: request } = await fetch(
  `${API}/quotations/vendor/request/${requests[0].id}`
).then(r => r.json());

// 3. Build and submit quote
const quote = {
  quotationRequestId: request.id,
  vendorId: vendorId,
  pricingType: 'BREAKUP',
  totalAmount: 1500.00,
  estimatedDuration: '2-3 hours',
  warrantyInfo: '6 months on parts and labour',
  validUntil: '2026-05-05',
  vendorNotes: 'Same-day service available',
  lineItems: [
    {
      itemType: 'SERVICE',
      title: 'AC Gas Refill',
      quantity: 1,
      unitPrice: 800.00,
      taxRate: 5.0,
      isOptional: false
    },
    {
      itemType: 'PART',
      title: 'Cabin Filter',
      quantity: 1,
      unitPrice: 350.00,
      discount: 50.00,
      taxRate: 5.0,
      isOptional: false
    }
  ]
};

const { data: submitted } = await fetch(`${API}/quotations/vendor/quote`, {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify(quote)
}).then(r => r.json());

console.log(`Quote ${submitted.quoteNumber} submitted!`);

// 4. Track my stats
const { data: stats } = await fetch(
  `${API}/quotations/vendor/stats?vendorId=${vendorId}`
).then(r => r.json());

console.log(`Total: ${stats.total}, Accepted: ${stats.accepted}`);
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

// Set vendor owner auth header
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

### 9.2 Vendor Profile APIs

```javascript
// api/vendor/vendorProfileApi.js
import api from '../axiosInstance';

const VENDOR_BASE = '/vendors';

// Register as vendor
export const registerVendor = (userId, data) =>
  api.post(VENDOR_BASE, data, {
    headers: { 'X-Auto-Adda-Id': userId },
  });
// data = {
//   companyBrandName, companyArabicName, name, trnNumber,
//   webSite, email, phoneNumber, mobileNumber,
//   companyLocation: { address, city, state, zipCode, latitude, longitude },
//   osServiceIds, tradeLicenceNumber, openingTime, closingTime,
//   workingDays, logoId, vatCertificateIds,
//   tradeLicenseAttachmentIds, companyImagesIds
// }

// Get my vendor profiles
export const getMyVendors = (userId) =>
  api.get(VENDOR_BASE, {
    headers: { 'X-Auto-Adda-Id': userId },
  });

// Get vendor by ID
export const getVendorById = (id) =>
  api.get(`${VENDOR_BASE}/${id}`);

// Update vendor profile
export const updateVendor = (id, data) =>
  api.put(`${VENDOR_BASE}/${id}`, data);
// data = same as registerVendor

// Delete vendor
export const deleteVendor = (id) =>
  api.delete(`${VENDOR_BASE}/${id}`);
```

---

### 9.3 Vendor Service Bridge APIs

```javascript
// api/vendor/vendorServiceApi.js
import api from '../axiosInstance';

const VENDOR_SERVICE_BASE = '/vendor-services';

// Get my services
export const getMyServices = (vendorId) =>
  api.get(`${VENDOR_SERVICE_BASE}/vendor/${vendorId}`);

// Add a service
export const addService = (data) =>
  api.post(VENDOR_SERVICE_BASE, data);
// data = { vendorId, serviceCategoryId: categoryId, isActive: true }

// Get vendor service by ID
export const getServiceById = (id) =>
  api.get(`${VENDOR_SERVICE_BASE}/${id}`);

// Update a service link
export const updateService = (id, data) =>
  api.put(`${VENDOR_SERVICE_BASE}/${id}`, data);

// Remove a service
export const removeService = (id) =>
  api.delete(`${VENDOR_SERVICE_BASE}/${id}`);

// Get all vendor services
export const getAllBridges = () =>
  api.get(VENDOR_SERVICE_BASE);
```

---

### 9.4 Service Offering APIs

```javascript
// api/vendor/offeringApi.js
import api from '../axiosInstance';

const OFFERING_BASE = '/service-offerings';

// Create offering
export const createOffering = (data) =>
  api.post(OFFERING_BASE, data);
// data = {
//   vendorServiceId, offeringName, shortDescription, description,
//   sku, pricingType, price, minPrice, maxPrice, originalPrice,
//   currency, stockStatus, quantityAvailable,
//   filterableAttributes: { brand: "MRF", width: "185" },
//   specifications: { warranty: "5 years" },
//   imageUrl, galleryImages, displayOrder, isFeatured
// }

// Get offering by ID
export const getOfferingById = (id) =>
  api.get(`${OFFERING_BASE}/${id}`);

// Get offering by SKU
export const getOfferingBySku = (sku) =>
  api.get(`${OFFERING_BASE}/sku/${sku}`);

// Update offering
export const updateOffering = (id, data) =>
  api.put(`${OFFERING_BASE}/${id}`, data);

// Soft delete (deactivate)
export const deactivateOffering = (id) =>
  api.delete(`${OFFERING_BASE}/${id}`);

// Reactivate
export const reactivateOffering = (id) =>
  api.post(`${OFFERING_BASE}/${id}/reactivate`);

// Update stock status
export const updateStockStatus = (id, status, quantity = null) =>
  api.patch(`${OFFERING_BASE}/${id}/stock`, null, {
    params: { status, quantity },
  });

// Toggle featured
export const toggleFeatured = (id) =>
  api.patch(`${OFFERING_BASE}/${id}/featured`);

// Get my offerings (active only)
export const getMyOfferings = (vendorId) =>
  api.get(`${OFFERING_BASE}/vendor/${vendorId}`);

// Get my offerings (including inactive)
export const getAllMyOfferings = (vendorId) =>
  api.get(`${OFFERING_BASE}/vendor/${vendorId}/all`);

// Get my offerings by category
export const getMyOfferingsByCategory = (vendorId, categoryId) =>
  api.get(`${OFFERING_BASE}/vendor/${vendorId}/category/${categoryId}`);

// Get offerings by vendor service
export const getOfferingsByVendorService = (vendorServiceId) =>
  api.get(`${OFFERING_BASE}/vendor-service/${vendorServiceId}`);

// Get offerings by vendor service (including inactive)
export const getAllOfferingsByVendorService = (vendorServiceId) =>
  api.get(`${OFFERING_BASE}/vendor-service/${vendorServiceId}/all`);

// Get attribute definitions for a category (for building offering form)
export const getAttributeDefinitions = (categoryId) =>
  api.get(`${OFFERING_BASE}/category/${categoryId}/attributes`);

// Get required attribute definitions
export const getRequiredAttributes = (categoryId) =>
  api.get(`${OFFERING_BASE}/category/${categoryId}/attributes/required`);

// Search offerings by name
export const searchByName = (q) =>
  api.get(`${OFFERING_BASE}/search`, { params: { q } });
```

---

### 9.5 Quotation APIs

```javascript
// api/vendor/quotationApi.js
import api from '../axiosInstance';

const QUOTATION_BASE = '/quotations/vendor';

// --- Browse Open Requests ---

// Find open quotation requests to quote on
export const findOpenRequests = (vendorId, categoryId) =>
  api.get(`${QUOTATION_BASE}/requests/open`, {
    params: { vendorId, categoryId },
  });

// Get request details
export const getRequestDetails = (requestId) =>
  api.get(`${QUOTATION_BASE}/request/${requestId}`);

// Get field values for a request
export const getRequestFieldValues = (requestId) =>
  api.get(`${QUOTATION_BASE}/request/${requestId}/fields`);

// --- Submit & Manage Quotes ---

// Submit a quotation
export const submitQuote = (data) =>
  api.post(`${QUOTATION_BASE}/quote`, data);
// data = {
//   quotationRequestId, vendorId, pricingType,
//   totalAmount, minAmount, maxAmount, inspectionFee,
//   estimatedDuration, warrantyInfo, validUntil, vendorNotes,
//   lineItems: [{
//     itemType, title, description, quantity, unit,
//     unitPrice, discount, taxRate, isOptional
//   }]
// }

// Get my submitted quotations
export const getMyQuotations = (vendorId) =>
  api.get(`${QUOTATION_BASE}/quotes`, { params: { vendorId } });

// Get my quotations (paginated)
export const getMyQuotationsPaginated = (vendorId, page = 0, size = 10) =>
  api.get(`${QUOTATION_BASE}/quotes/page`, {
    params: { vendorId, page, size },
  });

// Get quotation by ID
export const getQuotationById = (quotationId) =>
  api.get(`${QUOTATION_BASE}/quote/${quotationId}`);

// Get quotation line items
export const getQuotationLineItems = (quotationId) =>
  api.get(`${QUOTATION_BASE}/quote/${quotationId}/items`);

// Withdraw a quotation
export const withdrawQuote = (quotationId, vendorId) =>
  api.post(`${QUOTATION_BASE}/quote/${quotationId}/withdraw`, null, {
    params: { vendorId },
  });

// --- Statistics ---

// Get quotation statistics
export const getQuotationStats = (vendorId) =>
  api.get(`${QUOTATION_BASE}/stats`, { params: { vendorId } });
```

---

### 9.6 Booking APIs

```javascript
// api/vendor/bookingApi.js
import api from '../axiosInstance';

const BOOKING_BASE = '/bookings';

// Get my appointments (by vendor ID)
export const getMyAppointments = (vendorId, status = null) =>
  api.get(`${BOOKING_BASE}/vendor/${vendorId}`, {
    params: { status },
  });

// Get appointment details
export const getAppointmentDetails = (appointmentId) =>
  api.get(`${BOOKING_BASE}/${appointmentId}`);

// Update appointment status
export const updateAppointmentStatus = (data) =>
  api.put(`${BOOKING_BASE}/status`, data);
// data = { appointmentId, memberId, memberType: "VENDOR", status, comments }

// Get available slots (manage capacity)
export const getAvailableSlots = (vendorId, date) =>
  api.get(`${BOOKING_BASE}/vendor/${vendorId}/available-slots`, {
    params: { date }, // format: dd/MM/yyyy
  });
```

---

### 9.7 Media File APIs

```javascript
// api/vendor/mediaApi.js
import api from '../axiosInstance';

const MEDIA_BASE = '/mediafiles';

// Upload files (logos, images, certificates, product photos)
export const uploadFiles = (files) => {
  const formData = new FormData();
  files.forEach((file) => {
    if (file.size > 10 * 1024 * 1024) {
      throw new Error(`File ${file.name} exceeds 10MB limit`);
    }
    formData.append('files', file);
  });
  return api.post(`${MEDIA_BASE}/upload`, formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
    timeout: 60000,
  });
};

// Delete a file
export const deleteFile = (id) =>
  api.delete(`${MEDIA_BASE}/delete-image/${id}`);
```

---

### 9.8 Service Category APIs (Read-Only for Vendor)

```javascript
// api/vendor/categoryApi.js
import api from '../axiosInstance';

const CATEGORY_BASE = '/service-categories';

// Get all categories (for selecting which services to offer)
export const getAllCategories = () =>
  api.get(CATEGORY_BASE);

// Get root categories
export const getRootCategories = () =>
  api.get(`${CATEGORY_BASE}/root`);

// Get sub-categories
export const getSubCategories = (parentId) =>
  api.get(`${CATEGORY_BASE}/parent/${parentId}`);

// Get category by ID
export const getCategoryById = (id) =>
  api.get(`${CATEGORY_BASE}/${id}`);
```

---

### 9.9 Usage Examples (React Components)

#### Vendor Registration

```javascript
import React, { useEffect, useState } from 'react';
import { registerVendor } from '../api/vendor/vendorProfileApi';
import { getAllCategories } from '../api/vendor/categoryApi';
import { uploadFiles } from '../api/vendor/mediaApi';

const VendorRegistration = ({ userId }) => {
  const [categories, setCategories] = useState([]);
  const [form, setForm] = useState({});
  const [logo, setLogo] = useState(null);
  const [tradeLicense, setTradeLicense] = useState([]);
  const [companyImages, setCompanyImages] = useState([]);

  useEffect(() => {
    getAllCategories().then((res) => setCategories(res.data.data));
  }, []);

  const handleSubmit = async () => {
    // Upload all files first
    let logoId = null;
    let tradeLicenseIds = [];
    let companyImageIds = [];

    if (logo) {
      const res = await uploadFiles([logo]);
      logoId = res.data.data[0].id;
    }
    if (tradeLicense.length) {
      const res = await uploadFiles(tradeLicense);
      tradeLicenseIds = res.data.data.map((f) => f.id);
    }
    if (companyImages.length) {
      const res = await uploadFiles(companyImages);
      companyImageIds = res.data.data.map((f) => f.id);
    }

    // Register
    const vendorData = {
      companyBrandName: form.brandName,
      name: form.shortName,
      email: form.email,
      phoneNumber: form.phone,
      companyLocation: {
        address: form.address,
        city: form.city,
        latitude: form.lat,
        longitude: form.lng,
      },
      osServiceIds: form.selectedServiceIds,
      openingTime: form.openingTime,
      closingTime: form.closingTime,
      workingDays: form.workingDays,
      logoId,
      tradeLicenseAttachmentIds: tradeLicenseIds,
      companyImagesIds: companyImageIds,
    };

    try {
      const res = await registerVendor(userId, vendorData);
      alert(`Registered! Vendor ID: ${res.data.id}`);
    } catch (err) {
      alert('Registration failed: ' + err.response?.data?.message);
    }
  };

  // ... render form with file pickers, category multi-select, etc.
};
```

#### Offering Management

```javascript
import React, { useEffect, useState } from 'react';
import {
  getMyOfferings,
  createOffering,
  updateStockStatus,
  deactivateOffering,
  reactivateOffering,
  getAttributeDefinitions,
} from '../api/vendor/offeringApi';
import { getMyServices } from '../api/vendor/vendorServiceApi';
import { uploadFiles } from '../api/vendor/mediaApi';

const OfferingManagement = ({ vendorId }) => {
  const [offerings, setOfferings] = useState([]);
  const [services, setServices] = useState([]);
  const [attributes, setAttributes] = useState([]);

  useEffect(() => {
    loadData();
  }, [vendorId]);

  const loadData = async () => {
    const [offRes, svcRes] = await Promise.all([
      getMyOfferings(vendorId),
      getMyServices(vendorId),
    ]);
    setOfferings(offRes.data.data);
    setServices(svcRes.data.data);
  };

  // When vendor selects a service category, load its attribute definitions
  const onServiceSelect = async (service) => {
    const res = await getAttributeDefinitions(service.serviceId);
    setAttributes(res.data.data);
    // Render dynamic form fields from attributes
  };

  const handleCreateOffering = async (formData, imageFiles) => {
    // Upload images
    let imageUrl = null;
    let galleryImages = [];
    if (imageFiles.length) {
      const uploadRes = await uploadFiles(imageFiles);
      const urls = uploadRes.data.data.map((f) => f.fileUrl);
      imageUrl = urls[0];
      galleryImages = urls.slice(1);
    }

    // Build filterableAttributes from dynamic form
    const filterableAttributes = {};
    attributes.forEach((attr) => {
      if (formData[attr.attributeCode]) {
        filterableAttributes[attr.attributeCode] = formData[attr.attributeCode];
      }
    });

    await createOffering({
      vendorServiceId: formData.vendorServiceId,
      offeringName: formData.name,
      shortDescription: formData.shortDesc,
      description: formData.description,
      pricingType: formData.pricingType,
      price: formData.price,
      minPrice: formData.minPrice,
      maxPrice: formData.maxPrice,
      stockStatus: 'IN_STOCK',
      filterableAttributes,
      imageUrl,
      galleryImages,
    });

    loadData(); // refresh
  };

  const handleStockUpdate = async (offeringId, status) => {
    await updateStockStatus(offeringId, status);
    loadData();
  };

  const handleToggleActive = async (offering) => {
    if (offering.active) {
      await deactivateOffering(offering.id);
    } else {
      await reactivateOffering(offering.id);
    }
    loadData();
  };

  // ... render table with actions
};
```

#### Quotation Dashboard

```javascript
import React, { useEffect, useState } from 'react';
import {
  findOpenRequests,
  getRequestDetails,
  getRequestFieldValues,
  submitQuote,
  getMyQuotationsPaginated,
  getQuotationStats,
  withdrawQuote,
} from '../api/vendor/quotationApi';

const QuotationDashboard = ({ vendorId, categoryId }) => {
  const [stats, setStats] = useState({});
  const [openRequests, setOpenRequests] = useState([]);
  const [myQuotes, setMyQuotes] = useState([]);
  const [page, setPage] = useState(0);

  useEffect(() => {
    loadDashboard();
  }, [vendorId]);

  const loadDashboard = async () => {
    const [statsRes, requestsRes, quotesRes] = await Promise.all([
      getQuotationStats(vendorId),
      findOpenRequests(vendorId, categoryId),
      getMyQuotationsPaginated(vendorId, 0, 10),
    ]);
    setStats(statsRes.data.data);
    setOpenRequests(requestsRes.data.data);
    setMyQuotes(quotesRes.data.data.content || []);
  };

  const handleViewRequest = async (requestId) => {
    const [detailRes, fieldsRes] = await Promise.all([
      getRequestDetails(requestId),
      getRequestFieldValues(requestId),
    ]);
    // Show request detail modal with fields
    return { detail: detailRes.data.data, fields: fieldsRes.data.data };
  };

  const handleSubmitQuote = async (requestId, quoteData) => {
    try {
      const res = await submitQuote({
        quotationRequestId: requestId,
        vendorId,
        pricingType: quoteData.pricingType,
        totalAmount: quoteData.totalAmount,
        estimatedDuration: quoteData.duration,
        warrantyInfo: quoteData.warranty,
        validUntil: quoteData.validUntil,
        vendorNotes: quoteData.notes,
        lineItems: quoteData.lineItems.map((item) => ({
          itemType: item.type,
          title: item.title,
          description: item.description,
          quantity: item.qty,
          unit: item.unit,
          unitPrice: item.price,
          discount: item.discount || 0,
          taxRate: item.tax || 5.0,
          isOptional: item.optional || false,
        })),
      });
      alert(`Quote submitted: ${res.data.data.quoteNumber}`);
      loadDashboard();
    } catch (err) {
      alert('Failed: ' + err.response?.data?.message);
    }
  };

  const handleWithdraw = async (quotationId) => {
    if (!window.confirm('Withdraw this quote?')) return;
    await withdrawQuote(quotationId, vendorId);
    loadDashboard();
  };

  return (
    <div>
      {/* Stats Cards */}
      <div className="stats-row">
        <div>Total: {stats.total}</div>
        <div>Pending: {stats.pending}</div>
        <div>Accepted: {stats.accepted}</div>
        <div>Rejected: {stats.rejected}</div>
      </div>

      {/* Open Requests */}
      <h3>Open Requests</h3>
      {openRequests.map((req) => (
        <div key={req.id}>
          <span>{req.quotationNumber} - {req.serviceCategory?.name}</span>
          <span>City: {req.city} | Expires: {req.expiresAt}</span>
          <button onClick={() => handleViewRequest(req.id)}>View & Quote</button>
        </div>
      ))}

      {/* My Quotes */}
      <h3>My Submitted Quotes</h3>
      {myQuotes.map((q) => (
        <div key={q.id}>
          <span>{q.quoteNumber} - {q.status} - {q.totalAmount} AED</span>
          {q.status === 'SENT' && (
            <button onClick={() => handleWithdraw(q.id)}>Withdraw</button>
          )}
        </div>
      ))}
    </div>
  );
};
```

#### Booking Management

```javascript
import React, { useEffect, useState } from 'react';
import {
  getMyAppointments,
  getAppointmentDetails,
  updateAppointmentStatus,
} from '../api/vendor/bookingApi';

const BookingManagement = ({ vendorId }) => {
  const [bookings, setBookings] = useState([]);
  const [statusFilter, setStatusFilter] = useState(null);

  useEffect(() => {
    loadBookings();
  }, [vendorId, statusFilter]);

  const loadBookings = async () => {
    const res = await getMyAppointments(vendorId, statusFilter);
    setBookings(res.data.data);
  };

  const handleStatusUpdate = async (appointmentId, newStatus, comments = '') => {
    try {
      await updateAppointmentStatus({
        appointmentId,
        memberId: vendorId,
        memberType: 'VENDOR',
        status: newStatus,
        comments,
      });
      loadBookings();
    } catch (err) {
      alert('Update failed: ' + err.response?.data?.message);
    }
  };

  const statusActions = {
    REQUESTED: ['CONFIRMED', 'REJECTED'],
    CONFIRMED: ['SCHEDULED', 'CANCELLED'],
    SCHEDULED: ['WORK_INPROGRESS', 'CANCELLED'],
    WORK_INPROGRESS: ['COMPLETED', 'WAITING_FOR_SPARES'],
    WAITING_FOR_SPARES: ['WORK_INPROGRESS'],
    COMPLETED: ['READY_FOR_DELIVERY'],
    READY_FOR_DELIVERY: ['DELIVERED'],
  };

  return (
    <div>
      {/* Status filter tabs */}
      <div className="tabs">
        {['ALL', 'REQUESTED', 'CONFIRMED', 'WORK_INPROGRESS', 'COMPLETED'].map((s) => (
          <button
            key={s}
            className={statusFilter === (s === 'ALL' ? null : s) ? 'active' : ''}
            onClick={() => setStatusFilter(s === 'ALL' ? null : s)}
          >
            {s}
          </button>
        ))}
      </div>

      {/* Booking cards */}
      {bookings.map((b) => (
        <div key={b.id} className="booking-card">
          <h4>{b.appointmentReferenceId}</h4>
          <p>Date: {b.scheduleDate} | Time: {b.serviceStartTime} - {b.serviceEndTime}</p>
          <p>Status: <strong>{b.status}</strong></p>
          <p>Customer #{b.customerId} | Vehicle: {b.vehicle?.make} {b.vehicle?.model}</p>
          <p>Services: {b.servicesResponse?.map((s) => s.name).join(', ')}</p>

          {/* Action buttons based on current status */}
          <div className="actions">
            {(statusActions[b.status] || []).map((action) => (
              <button key={action} onClick={() => handleStatusUpdate(b.id, action)}>
                {action.replace(/_/g, ' ')}
              </button>
            ))}
          </div>
        </div>
      ))}
    </div>
  );
};
```
