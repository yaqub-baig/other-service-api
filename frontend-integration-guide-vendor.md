# Frontend Integration Guide - Vendor Panel

> **Base URL:** `{API_BASE_URL}` (e.g., `http://localhost:9010/api/v1`)
> **Authentication Header:** `X-Auto-Adda-Id: {vendorOwnerUserId}` (used in vendor registration)
> **Language:** send `Accept-Language: ar` to get Arabic attribute names, labels and help text.
> **Standard Response Wrapper:**
> ```json
> {
>   "status": "success",
>   "code": "200",
>   "message": "success",
>   "data": { ... }
> }
> ```
> Not wrapped: `/vendors/**` and `GET /bookings/{vendorServiceId}/available-slots` return the DTO directly.
>
> **Errors** come back as **HTTP 201** with `"status": "failed"` and the real status in `code`, so check the body, not the HTTP status (axios and `fetch` won't treat them as errors):
> ```json
> { "status": "failed", "code": "400", "message": "Invalid attribute values: 1 field(s) failed validation",
>   "body": [ { "field": "tire_size", "message": "Unknown attribute 'tire_size'" } ] }
> ```

---

## Table of Contents

1. [Vendor Registration & Profile](#1-vendor-registration--profile)
2. [Vendor Service Pricing](#2-vendor-service-pricing)
3. [Service Offerings (Product/Service Listings)](#3-service-offerings-productservice-listings)
4. [Quotation Management](#4-quotation-management)
5. [Booking Management (Vendor View)](#5-booking-management-vendor-view)
6. [Media File Uploads](#6-media-file-uploads)
7. [Recommended Page Structure](#7-recommended-page-structure)
8. [Complete Vendor Journeys](#8-complete-vendor-journeys)

---

## 1. Vendor Registration & Profile

**Base Path:** `/vendors`. These endpoints return the DTO directly, without the `ApiResponse` wrapper.

Registration has two parts: the **company** (brand, contact, HQ address, logo, VAT certificate) and one
or more **services** the company runs (a car wash, a tyre shop, ...). Each service has its own hours,
capacity, trade licence, branch address, photos, booking slots and details.

### 1.1 Register the Company

```
POST /vendors
X-Auto-Adda-Id: {userId}
Content-Type: application/json
```

**Request Body (`VendorCompanyRequest`):**
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
  "countryCode": "AE",
  "companyLocation": {
    "streetAddress1": "Al Quoz Industrial Area 3, Street 8",
    "city": "Dubai",
    "state": "Dubai",
    "postalCode": "12345",
    "country": "AE",
    "latitude": 25.2048,
    "longitude": 55.2708
  },
  "logoId": 10,
  "vatCertificateIds": [11]
}
```

| Field             | Type       | Required | Description                                      |
|-------------------|------------|----------|--------------------------------------------------|
| companyBrandName  | String     | Yes      | Company brand name (English)                     |
| companyArabicName | String     | No       | Company name in Arabic                           |
| name              | String     | Yes      | Legal name                                       |
| trnNumber         | String     | No       | Tax Registration Number                          |
| webSite           | String     | No       | Company website URL                              |
| email             | String     | Yes      | Contact email                                    |
| phoneNumber       | String     | Yes      | Landline phone                                   |
| mobileNumber      | String     | No       | Mobile phone                                     |
| countryCode       | String     | No       | ISO country of the vendor's market (default AE)  |
| companyLocation   | Object     | Yes      | HQ address (`streetAddress1`, `city`, `latitude`, `longitude`, ...) |
| logoId            | Long       | No       | MediaFile ID for the logo (upload first)         |
| vatCertificateIds | List<Long> | No       | MediaFile IDs for VAT certificates               |

**Response:** `VendorProfileResponse`, with `accountStatus: PENDING`.

### 1.2 Add a Service

```
POST /vendors/{vendorId}/services
X-Auto-Adda-Id: {userId}
Content-Type: application/json
```

First load the details form for the service type:

```
GET /vendors/services/attributes?serviceType=CAR_WASH
```

It returns attribute definitions (`attributeCode`, `attributeName`, `dataType`, `isRequired`, `options`,
`validationRules`, `unit`). Send `Accept-Language: ar` for Arabic labels. Types with no details return
an empty list.

**Request Body (`VendorServiceOnboardingRequest`):**
```json
{
  "serviceType": "CAR_WASH",
  "openingTime": "08:00:00",
  "closingTime": "20:00:00",
  "breakStartTime": "13:00:00",
  "breakEndTime": "14:00:00",
  "workingDays": ["MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY"],
  "concurrentAppointmentCapacity": 3,
  "tradeLicenceNumber": "DED-123456",
  "serviceLocation": { "streetAddress1": "Shop 4", "city": "Dubai", "country": "AE", "latitude": 25.14, "longitude": 55.24 },
  "tradeLicenseAttachmentIds": [12],
  "serviceImagesIds": [13, 14],
  "attributes": {
    "bay_count": 4,
    "average_wash_duration_minutes": 30,
    "mobile_service": false,
    "express_wash_available": true
  }
}
```

| Field                         | Type               | Required | Description                                             |
|-------------------------------|--------------------|----------|---------------------------------------------------------|
| serviceType                   | String             | Yes      | e.g. `CAR_WASH`, `BATTERY`, `TYRE_AND_BALANCING`; picks the category |
| openingTime / closingTime     | String (HH:mm:ss)  | Yes*     | *Required for a car wash                                |
| breakStartTime / breakEndTime | String (HH:mm:ss)  | No       | Daily break                                             |
| workingDays                   | List<String>       | Yes*     | `MONDAY` ... `SUNDAY`. *Required for a car wash         |
| concurrentAppointmentCapacity | Integer            | No       | Bookings per slot (car wash default 2)                  |
| tradeLicenceNumber            | String             | No       | This branch's trade licence                             |
| serviceLocation               | Object             | No       | Branch address; the company HQ is used when left out    |
| tradeLicenseAttachmentIds     | List<Long>         | No       | MediaFile IDs for licence documents                     |
| serviceImagesIds              | List<Long>         | No       | MediaFile IDs for branch photos                         |
| serviceCategoryIds            | List<Integer>      | No       | Extra categories beyond the type's own                  |
| attributes                    | Object             | Depends  | The type's details, keyed by `attributeCode`. Required ones must be sent |

Unknown attribute codes and invalid values are refused with `code: "400"` and every failing field in
`body`.

**Response:** `VendorServiceResponse`, including `id` (the `vendorServiceId` used for bookings) and
`attributes`. Adding a service creates its working hours and 60 days of booking slots.

To create the company and its first service in one call:

```
POST /vendors/onboard
X-Auto-Adda-Id: {userId}

{ "company": { VendorCompanyRequest }, "service": { VendorServiceOnboardingRequest } }
```

It returns `{ "vendor": VendorProfileResponse, "service": VendorServiceResponse }`.

### 1.3 Update a Service

```
PUT /vendors/services/{vendorServiceId}
Content-Type: application/json
```

Same body as 1.2. Every field is optional and `serviceType` is ignored. Sent `attributes` are merged into
the stored ones, and a `null` value clears one. Changing hours or capacity regenerates upcoming slots.

### 1.4 Get Services

```
GET /vendors/{vendorId}/services              a company's services
GET /vendors/services?serviceType=CAR_WASH    the caller's services (X-Auto-Adda-Id); all types if serviceType is left out
```

### 1.5 Get My Vendor Profiles

```
GET /vendors
X-Auto-Adda-Id: {userId}
```

Returns all companies owned by this user, each with its `services`.

### 1.6 Get Vendor by ID

```
GET /vendors/{id}
```

### 1.7 Update Vendor Profile

```
PUT /vendors/{id}
Content-Type: application/json
```

Same body as 1.1; every field is optional.

### 1.8 Delete Vendor

```
DELETE /vendors/{id}
```

Deactivates the company.

### Registration Flow

```
1. Upload logo and VAT certificate      POST /mediafiles/upload
2. Register the company                 POST /vendors
3. Upload trade licence and photos      POST /mediafiles/upload
4. Load the details form                GET /vendors/services/attributes?serviceType=...
5. Add the service                      POST /vendors/{vendorId}/services
   (steps 2 and 5 can be one call:      POST /vendors/onboard)
```

---

## 2. Vendor Service Pricing

**Base Path:** `/vendor-services`

Low-level access to vendor service rows, mainly to set the vendor's default pricing for a service.
Use section 1 to add services; a service added here has no hours or slots.

### 2.1 Get My Services

```
GET /vendor-services/vendor/{vendorId}
```

**Response:** `ApiResponse<List<VendorServiceResponse>>`

### 2.2 Set Pricing

```
PUT /vendor-services/{id}
Content-Type: application/json
```

```json
{
  "vendorId": 1,
  "serviceCategoryId": 3,
  "pricingType": "FIXED",
  "price": 120.00,
  "currency": "AED",
  "description": "Exterior and interior wash"
}
```

| Field             | Type       | Required | Description                         |
|-------------------|------------|----------|-------------------------------------|
| vendorId          | Integer    | Yes      | Your vendor ID                      |
| serviceCategoryId | Integer    | Yes      | The service's category              |
| isActive          | Boolean    | No       | Left unchanged when not sent (`active` is also accepted) |
| pricingType       | String     | No       | `FIXED` (default), `RANGE`, `QUOTE_BASED` or `NEGOTIABLE` |
| price             | Decimal    | No       | Fixed price                         |
| minPrice/maxPrice | Decimal    | No       | Range                               |
| currency          | String     | No       | ISO 4217                            |
| description       | String     | No       |                                     |

### 2.3 Remove a Service

```
DELETE /vendor-services/{id}
```

Deactivates it; offerings, bookings and quotes keep pointing at it.

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
  "serviceId": 7,
  "offeringType": "PRODUCT",
  "offeringName": "MRF ZLX 185/65 R15 Tubeless Car Tyre",
  "shortDescription": "High-performance tubeless tyre for sedans",
  "description": "MRF ZLX is a premium tubeless radial tyre...",
  "sku": null,
  "pricingType": "FIXED",
  "price": 350.00,
  "minPrice": null,
  "maxPrice": null,
  "originalPrice": 400.00,
  "currency": "AED",
  "stockStatus": "IN_STOCK",
  "quantityAvailable": 50,
  "filterableAttributes": {
    "tire_size": "185/65 R15",
    "tire_brand": "MRF"
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
| vendorServiceId      | Integer         | Yes      | Your vendor service (branch) ID                |
| serviceId            | Integer         | No       | Catalog service (`GET /catalog/services?categoryId=`). Must be in the vendor service's category. Recommended: it decides the attribute set, the currency and the schema version |
| offeringType         | Enum            | No       | `SERVICE` (default) or `PRODUCT`               |
| offeringName         | String          | Yes      | Product/service name                           |
| shortDescription     | String          | No       | Short tagline                                  |
| description          | String          | No       | Full description                               |
| sku                  | String          | No       | SKU (auto-generated if empty; must be unique)  |
| pricingType          | Enum            | No       | `FIXED` (default), `RANGE`, `QUOTE_BASED`, `NEGOTIABLE` |
| price                | BigDecimal      | Depends  | Price (for FIXED)                              |
| minPrice             | BigDecimal      | Depends  | Min price (for RANGE)                          |
| maxPrice             | BigDecimal      | Depends  | Max price (for RANGE)                          |
| originalPrice        | BigDecimal      | No       | Original/MRP price (for showing discount)      |
| currency             | String          | No       | See currency rules below                       |
| stockStatus          | Enum            | No       | PRODUCT only: `IN_STOCK` (default), `LIMITED_STOCK`, `OUT_OF_STOCK`, `PRE_ORDER`, `DISCONTINUED` |
| quantityAvailable    | Integer         | No       | PRODUCT only                                   |
| filterableAttributes | Map             | Depends  | Attribute code → value; see validation rules below |
| specifications       | Map             | No       | Additional specs (free-form, not validated)    |
| imageUrl             | String          | No       | Primary image URL                              |
| galleryImages        | List<String>    | No       | Gallery image URLs                             |
| displayOrder         | Integer         | No       | Sort order in your listings                    |
| isFeatured           | Boolean         | No       | Featured flag (default false)                  |

**Rules:**
- **Attributes** are always validated, even when none are sent. The allowed codes are the service's published schema plus the category defaults (3.2). Required attributes must be sent, values must match their type and options, and unknown codes are refused. Attributes marked `CUSTOMER` are never required on an offering.
- **Currency:** with a `serviceId`, the offering is priced in the currency of the vendor's market (the company's `countryCode`). Leave `currency` out to use it; a different currency is refused, and so is a service that isn't available in that country. Without a `serviceId`, `currency` is used as sent (default `AED`).
- **Stock:** `stockStatus` other than `IN_STOCK`, and `quantityAvailable`, are refused on `SERVICE` offerings.
- **Schema version:** the offering records the service's live schema version (`schemaVersionId`, `schemaVersionNo` in the response). Changing its attributes or service later re-validates against the current version.

**Response:** `ApiResponse<ServiceOfferingResponse>`. It includes `vendorServiceId`, `vendorId`, `serviceCategoryId`, `serviceId`, `serviceCode`, `serviceName`, `schemaVersionId`, `schemaVersionNo`, `offeringType`, `isActive` and the fields above. **`filterableAttributes`, `specifications` and `galleryImages` come back as JSON strings**; `JSON.parse` them.

### 3.2 Get Attribute Definitions (Before Creating)

Before creating an offering, get the attribute definitions to know what `filterableAttributes` to provide.

```
GET /service-offerings/category/{categoryId}/attributes?serviceId={serviceId}
```

With `serviceId`, this returns the service's published attributes plus the category defaults; without it, only the category defaults. `GET /catalog/services/{serviceId}/attributes` returns the same set.

**Response:** `ApiResponse<List<ServiceAttributeDefinitionResponse>>`

```json
{
  "data": [
    {
      "id": 1,
      "serviceCategoryId": 3,
      "serviceId": 7,
      "serviceCode": "TIRE_REPLACEMENT",
      "schemaVersionNo": 1,
      "schemaVersionStatus": "PUBLISHED",
      "attributeCode": "tire_size",
      "attributeName": "Tire size",
      "attributeNameAr": "مقاس الإطار",
      "dataType": "STRING",
      "inputContext": "CUSTOMER",
      "isRequired": true,
      "isFilterable": false,
      "unit": null,
      "options": null,
      "validationRules": null,
      "displayOrder": 10
    },
    {
      "id": 2,
      "serviceCode": "WHEEL_ALIGNMENT",
      "attributeCode": "alignment_type",
      "attributeName": "Alignment type",
      "dataType": "SINGLE_SELECT",
      "inputContext": "CUSTOMER",
      "isRequired": true,
      "options": "[{\"value\":\"front\",\"label\":\"Front axle\",\"labelAr\":\"...\"},{\"value\":\"four_wheel\",\"label\":\"Four-wheel alignment\",\"labelAr\":\"...\"}]"
    }
  ]
}
```

- `options` and `validationRules` are **JSON strings**; `JSON.parse` them. Options are `{ value, label, labelAr }`; send the `value`.
- `inputContext`: `PROVIDER` and `BOTH` attributes are what you describe on an offering; `CUSTOMER` attributes describe the customer's request and are optional here.
- `dataType`: `STRING`, `NUMBER`, `INTEGER`, `DECIMAL`, `BOOLEAN`, `DATE` (`yyyy-MM-dd`), `SINGLE_SELECT`, `MULTI_SELECT` (send a list), `JSON`.

### 3.3 Get Required Attributes

```
GET /service-offerings/category/{categoryId}/attributes/required?serviceId={serviceId}
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

Same body as create; every field is optional and only sent fields change. Sending `filterableAttributes` replaces the whole map and re-validates it. Switching `offeringType` to `SERVICE` resets the stock fields.

### 3.11 Soft Delete (Deactivate)

```
DELETE /service-offerings/{id}
```

Sets `isActive` to false. There is no hard delete; bookings and quotes keep pointing at the offering.

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

PRODUCT offerings only; a SERVICE offering refuses anything but `IN_STOCK` with no quantity.

### 3.14 Toggle Featured

```
PATCH /service-offerings/{id}/featured
```

### Frontend Flow: Create Offering

```javascript
const API = 'http://localhost:9010/api/v1';

// 1. Get the vendor's services (raw list, not wrapped) to know vendorServiceId
const services = await fetch(`${API}/vendors/${vendorId}/services`).then(r => r.json());

// 2. Select a service (branch) -> its category
const selectedService = services[0]; // e.g. { id: 1, serviceCategoryId: 3, serviceType: "TYRE_AND_BALANCING" }

// 3. Pick the catalog service in that category (e.g. Tire Replacement)
const { data: catalogServices } = await fetch(
  `${API}/catalog/services?categoryId=${selectedService.serviceCategoryId}&country=AE`
).then(r => r.json());
const catalogService = catalogServices[0];

// 4. Load attribute definitions for the service
const { data: attributes } = await fetch(
  `${API}/service-offerings/category/${selectedService.serviceCategoryId}/attributes?serviceId=${catalogService.id}`
).then(r => r.json());

// 5. Render dynamic form:
//    - Fixed fields: name, description, price, images, etc.
//    - Dynamic fields: one per attribute; JSON.parse(attr.options) for selects
//    - Required attrs marked with *

// 6. Submit offering
const res = await fetch(`${API}/service-offerings`, {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({
    vendorServiceId: selectedService.id,
    serviceId: catalogService.id,
    offeringType: 'PRODUCT',
    offeringName: formData.name,
    pricingType: 'FIXED',
    price: formData.price,          // currency comes from the vendor's market
    filterableAttributes: {
      tire_size: formData.tireSize,
      tire_brand: formData.brand,
    },
    specifications: formData.specs,
    imageUrl: formData.mainImage,
    galleryImages: formData.gallery
  })
}).then(r => r.json());

if (res.status === 'failed') {
  // HTTP 201 with the error; body lists failing fields: [{ field, message }]
  showErrors(res.body);
}
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

Only requests with status `SENT_TO_VENDORS` that haven't expired and have a location (`latitude`/`longitude`) are listed, newest first.

> **Note:** nothing in the API moves a request from `REQUESTED` to `SENT_TO_VENDORS` yet (the broadcast step isn't wired up), so this list is empty until that is added. You can still quote on a `REQUESTED` request by its ID (4.4).

**Response:** `ApiResponse<List<QuotationRequestResponse>>`

```json
{
  "data": [
    {
      "id": 50,
      "quotationNumber": "QR-20260425-001",
      "serviceCategory": { "id": 5, "code": "REPAIRS_DIAGNOSTICS", "name": "Repairs & Diagnostics" },
      "serviceId": 4,
      "serviceCode": "AC_SERVICE",
      "serviceName": "Air Conditioning Service",
      "schemaVersionId": 1,
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
          "id": 1,
          "fieldCode": "vehicle_make",
          "fieldLabel": "Vehicle Make",
          "fieldValue": "\"Toyota\"",
          "fileUrl": null
        },
        {
          "id": 2,
          "fieldCode": "issue_description",
          "fieldLabel": "Issue Description",
          "fieldValue": "\"AC not cooling, making noise\"",
          "fileUrl": null
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

`ApiResponse<List<QuotationRequestFieldValueResponse>>`: `{ id, fieldCode, fieldLabel, fieldValue, fileUrl }`. `fieldValue` is a JSON string; `JSON.parse` it.

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
| validUntil         | LocalDate       | No       | Quote validity date (default: 7 days from today) |
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

Each line's `totalPrice` is calculated by the server. `totalAmount` is stored as sent; it isn't recalculated from the lines.

**Rules:** the request must be `REQUESTED`, `SENT_TO_VENDORS` or `QUOTES_RECEIVED` and not expired, and a vendor can quote once per request. The quote is created with status `SENT`, and the request moves to `QUOTES_RECEIVED`.

**Response:** `ApiResponse<VendorQuotationResponse>` with `id`, `quoteNumber`, `status`, `lineItems` and the fields above.

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

Only your own quotes with status `SENT` or `DRAFT` can be withdrawn; they become `WITHDRAWN`.

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

`pending` counts quotes with status `SENT` (waiting for the customer).

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
| status         | String  | Optional filter, a booking status name: `CONFIRMED`, `WORK_INPROGRESS`, `COMPLETED`, `DONE`, `CANCELLED`, ... |

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
      "deliveryTime": null,
      "status": "CONFIRMED",
      "comments": "Check alignment too",
      "vendor": { "id": 1, "companyBrandName": "QuickFit Tyres" },
      "selectedServiceIds": "5",
      "servicesResponse": [
        { "id": 3, "code": "TIRES_WHEELS", "name": "Tires & Wheels" }
      ],
      "subscriptionPlanId": null,
      "customerId": 100,
      "vehicleId": 10,
      "technicianId": null,
      "vehicle": {
        "brand": "Toyota",
        "model": "Camry",
        "year": "2022",
        "code": "A",
        "number": "12345"
      },
      "slot": {
        "id": 77, "slotDate": "2026-04-25", "startTime": "09:00", "endTime": "10:00",
        "isAvailable": true, "maxBookings": 3, "currentBookings": 1, "availableBookings": 2,
        "vendorServiceId": 1
      }
    }
  ]
}
```

### 5.2 Get Appointment Details

```
GET /bookings/{appointmentId}
```

**Response:** `ApiResponse<{ appointment, services, jobcards }>`: the booking, its booked services and its job cards.

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

**Response:** `ApiResponse<BookingResponse>`

**Typical Vendor Status Flow:**
```
CONFIRMED -> WORK_INPROGRESS -> COMPLETED -> READY_FOR_DELIVERY -> DELIVERED (stored as DONE)
                             -> WAITING_FOR_SPARES -> WORK_INPROGRESS (resume)
          -> REJECTED (decline)
          -> CANCELLED
```

Bookings start as `CONFIRMED` (direct, slot and quote bookings alike). The server doesn't check transitions, so the vendor app should only offer the next sensible statuses. Side effects:

| Status              | Description                                  | Server side effect                     |
|---------------------|----------------------------------------------|----------------------------------------|
| CONFIRMED           | New booking (initial status)                 |                                        |
| ASSIGNED            | Assigned to a technician (via job card)      |                                        |
| WORK_INPROGRESS     | Service is being performed                   | Sets `serviceStartTime` to now         |
| WAITING_FOR_SPARES  | Paused - waiting for parts                   |                                        |
| COMPLETED           | Service completed                            | Sets `serviceEndTime` to now           |
| READY_FOR_DELIVERY  | Vehicle ready for pickup                     |                                        |
| DELIVERED           | Handed back to customer                      | Saved as `DONE`, sets `deliveryTime`   |
| CANCELLED           | Booking cancelled                            |                                        |
| REJECTED            | Vendor declined the booking                  |                                        |

Every change is recorded with `memberId`, `memberType` and `comments`.

### 5.4 Get Available Slots (Manage Capacity)

```
GET /bookings/{vendorServiceId}/available-slots?date=25/04/2026
```

Not wrapped. Returns `{ vendorServiceId, date, availableSlots: [{ slotId, startTime, endTime, maxBookings, currentBookings, availableBookings, isAvailable }] }`. Slots are generated 60 days ahead when the service is added; changing hours or capacity (`PUT /vendors/services/{vendorServiceId}`) regenerates them.

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
- `files`: One or more files. Images up to 10 MB each, videos under 20 MB (the server allows 30 MB for video but its upload limit is 20 MB per file, 50 MB per request).

**Response:** `ApiResponse<List<MediaFileResponse>>`
```json
{
  "data": [
    {
      "id": 45,
      "fileName": "8c1e...-logo.png",
      "originalFileName": "logo.png",
      "filePath": "...",
      "mediaType": "image/png",
      "size": 102400,
      "fileUrl": "https://storage.example.com/logo.png",
      "createdAt": "2026-04-25T10:00:00"
    }
  ]
}
```

Logos, VAT certificates, licences and branch photos are referenced by `id`; offering images by `fileUrl`.

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
  const json = await response.json();
  if (json.status === 'failed') throw new Error(json.message);
  return json;
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
  /services                       -- My services (branches): hours, details, pricing
    /new                          -- Add a service (type -> details form)
  /offerings                      -- All my offerings
    /new                          -- Create offering (branch -> catalog service -> dynamic form)
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
1. Upload logo and VAT cert       POST /mediafiles/upload
2. Register the company           POST /vendors (X-Auto-Adda-Id header)
3. Upload licence and photos      POST /mediafiles/upload
4. Load the details form          GET /vendors/services/attributes?serviceType=...
5. Add each service               POST /vendors/{vendorId}/services
6. Set pricing (optional)         PUT /vendor-services/{vendorServiceId}
```

### Journey 2: Add a Product/Service Listing

```
1. Get my services                GET /vendors/{vendorId}/services
2. Pick a service -> category     (serviceCategoryId)
3. Pick the catalog service       GET /catalog/services?categoryId={categoryId}&country=AE
4. Load attribute definitions     GET /service-offerings/category/{categoryId}/attributes?serviceId={serviceId}
5. Upload product images          POST /mediafiles/upload
6. Create offering                POST /service-offerings  { vendorServiceId, serviceId, filterableAttributes, ... }
7. Verify listing                 GET /service-offerings/{id}
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
1. View incoming bookings         GET /bookings/vendor/{vendorId}?status=CONFIRMED
2. View booking details           GET /bookings/{appointmentId}
3. Start work                     PUT /bookings/status  { status: "WORK_INPROGRESS" }
4. Complete                       PUT /bookings/status  { status: "COMPLETED" }
5. Deliver                        PUT /bookings/status  { status: "DELIVERED" }  (stored as DONE)
   (or decline: { status: "REJECTED" })
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
const API = 'http://localhost:9010/api/v1';

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

const res = await fetch(`${API}/quotations/vendor/quote`, {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify(quote)
}).then(r => r.json());

if (res.status === 'failed') {
  // HTTP 201 with the error, e.g. "You have already submitted a quote for this request"
  throw new Error(res.message);
}
console.log(`Quote ${res.data.quoteNumber} submitted!`);

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
  baseURL: process.env.REACT_APP_API_BASE_URL || 'http://localhost:9010/api/v1',
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 30000,
});

// Set vendor owner auth header
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
//   webSite, email, phoneNumber, mobileNumber, countryCode,
//   companyLocation: { streetAddress1, city, state, postalCode, country, latitude, longitude },
//   logoId, vatCertificateIds
// }

// Details form for a service type
export const getServiceAttributes = (serviceType) =>
  api.get(`${VENDOR_BASE}/services/attributes`, { params: { serviceType } });

// Add a service to a company
export const addVendorService = (userId, vendorId, data) =>
  api.post(`${VENDOR_BASE}/${vendorId}/services`, data, {
    headers: { 'X-Auto-Adda-Id': userId },
  });
// data = {
//   serviceType, openingTime, closingTime, breakStartTime, breakEndTime,
//   workingDays, concurrentAppointmentCapacity, tradeLicenceNumber,
//   serviceLocation, tradeLicenseAttachmentIds, serviceImagesIds,
//   attributes: { bay_count: 4, ... }
// }

// Company and first service in one call
export const onboardVendor = (userId, company, service) =>
  api.post(`${VENDOR_BASE}/onboard`, { company, service }, {
    headers: { 'X-Auto-Adda-Id': userId },
  });

// Update a service (attributes are merged; null clears one)
export const updateVendorService = (vendorServiceId, data) =>
  api.put(`${VENDOR_BASE}/services/${vendorServiceId}`, data);

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

### 9.3 Vendor Service APIs

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
// data = { vendorId, serviceCategoryId, active: true } (no hours or slots; use addVendorService to onboard)

// Get vendor service by ID
export const getServiceById = (id) =>
  api.get(`${VENDOR_SERVICE_BASE}/${id}`);

// Update pricing (send active: true, or the service is deactivated)
export const updateService = (id, data) =>
  api.put(`${VENDOR_SERVICE_BASE}/${id}`, data);
// data = { vendorId, serviceCategoryId, active: true, pricingType, price, minPrice, maxPrice, currency, description }

// Remove a service
export const removeService = (id) =>
  api.delete(`${VENDOR_SERVICE_BASE}/${id}`);

// Get all vendor services
export const getAllVendorServices = () =>
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
//   vendorServiceId, serviceId, offeringType, offeringName, shortDescription, description,
//   sku, pricingType, price, minPrice, maxPrice, originalPrice,
//   currency, stockStatus, quantityAvailable,       // stock fields: PRODUCT only
//   filterableAttributes: { tire_size: "185/65 R15", tire_brand: "MRF" },
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

// Get attribute definitions for a category / catalog service (for building offering form)
export const getAttributeDefinitions = (categoryId, serviceId) =>
  api.get(`${OFFERING_BASE}/category/${categoryId}/attributes`, { params: { serviceId } });

// Get required attribute definitions
export const getRequiredAttributes = (categoryId, serviceId) =>
  api.get(`${OFFERING_BASE}/category/${categoryId}/attributes/required`, { params: { serviceId } });

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

// Get appointment details -> data: { appointment, services, jobcards }
export const getAppointmentDetails = (appointmentId) =>
  api.get(`${BOOKING_BASE}/${appointmentId}`);

// Update appointment status
export const updateAppointmentStatus = (data) =>
  api.put(`${BOOKING_BASE}/status`, data);
// data = { appointmentId, memberId, memberType: "VENDOR", status, comments }

// Get available slots (manage capacity)
export const getAvailableSlots = (vendorServiceId, date) =>
  api.get(`${BOOKING_BASE}/${vendorServiceId}/available-slots`, {
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
    const limit = file.type.startsWith('video/') ? 20 : 10; // MB
    if (file.size > limit * 1024 * 1024) {
      throw new Error(`File ${file.name} exceeds ${limit}MB limit`);
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

// Catalog services in a category (to pick serviceId for an offering)
export const getCatalogServices = (categoryId, country) =>
  api.get('/catalog/services', { params: { categoryId, country } });
```

---

### 9.9 Usage Examples (React Components)

#### Vendor Registration

```javascript
import React, { useEffect, useState } from 'react';
import { onboardVendor, getServiceAttributes } from '../api/vendor/vendorProfileApi';
import { uploadFiles } from '../api/vendor/mediaApi';

const VendorRegistration = ({ userId }) => {
  const [form, setForm] = useState({ serviceType: 'CAR_WASH' });
  const [detailFields, setDetailFields] = useState([]);   // attribute definitions
  const [details, setDetails] = useState({});             // attributeCode -> value
  const [logo, setLogo] = useState(null);
  const [tradeLicense, setTradeLicense] = useState([]);
  const [serviceImages, setServiceImages] = useState([]);

  // The details form depends on the service type
  useEffect(() => {
    getServiceAttributes(form.serviceType).then((res) => setDetailFields(res.data));
  }, [form.serviceType]);

  const upload = async (files) =>
    files.length ? (await uploadFiles(files)).data.data.map((f) => f.id) : [];

  const handleSubmit = async () => {
    const [logoId] = await upload(logo ? [logo] : []);
    const tradeLicenseAttachmentIds = await upload(tradeLicense);
    const serviceImagesIds = await upload(serviceImages);

    const company = {
      companyBrandName: form.brandName,
      name: form.legalName,
      email: form.email,
      phoneNumber: form.phone,
      countryCode: 'AE',
      companyLocation: {
        streetAddress1: form.address,
        city: form.city,
        latitude: form.lat,
        longitude: form.lng,
      },
      logoId,
    };
    const service = {
      serviceType: form.serviceType,
      openingTime: form.openingTime,                 // "08:00:00"
      closingTime: form.closingTime,
      workingDays: form.workingDays,                 // ["MONDAY", ...]
      tradeLicenceNumber: form.tradeLicenceNumber,
      tradeLicenseAttachmentIds,
      serviceImagesIds,
      attributes: details,
    };

    try {
      const res = await onboardVendor(userId, company, service);
      alert(`Registered! Vendor ID: ${res.data.vendor.id}, service ID: ${res.data.service.id}`);
    } catch (err) {
      // the 9.1 interceptor rejects "failed" bodies; validation errors list
      // each failing field in body: [{ field, message }]
      alert('Registration failed: ' + err.message);
    }
  };

  // ... render the company fields, a service type picker, and one input per entry in
  // detailFields (number, checkbox or select depending on dataType)
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
    setServices(svcRes.data.data); // each has id (vendorServiceId) and serviceCategoryId
  };

  // When the vendor picks a branch and a catalog service, load the attribute definitions
  const onServiceSelect = async (vendorService, catalogServiceId) => {
    const res = await getAttributeDefinitions(vendorService.serviceCategoryId, catalogServiceId);
    setAttributes(res.data.data);
    // Render dynamic form fields from attributes; JSON.parse(attr.options) for selects
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
      serviceId: formData.catalogServiceId,
      offeringType: formData.offeringType,          // 'SERVICE' or 'PRODUCT'
      offeringName: formData.name,
      shortDescription: formData.shortDesc,
      description: formData.description,
      pricingType: formData.pricingType,
      price: formData.price,
      minPrice: formData.minPrice,
      maxPrice: formData.maxPrice,
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
    if (offering.isActive) {
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
          <span>{q.quoteNumber} - {q.status} - {q.totalAmount}</span>
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

  // bookings start CONFIRMED; the server doesn't check transitions, so offer only these
  const statusActions = {
    CONFIRMED: ['WORK_INPROGRESS', 'REJECTED', 'CANCELLED'],
    ASSIGNED: ['WORK_INPROGRESS', 'CANCELLED'],
    WORK_INPROGRESS: ['COMPLETED', 'WAITING_FOR_SPARES'],
    WAITING_FOR_SPARES: ['WORK_INPROGRESS'],
    COMPLETED: ['READY_FOR_DELIVERY'],
    READY_FOR_DELIVERY: ['DELIVERED'],
  };

  return (
    <div>
      {/* Status filter tabs */}
      <div className="tabs">
        {['ALL', 'CONFIRMED', 'WORK_INPROGRESS', 'COMPLETED', 'DONE'].map((s) => (
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
          <p>Customer #{b.customerId} | Vehicle: {b.vehicle?.brand} {b.vehicle?.model}</p>
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
