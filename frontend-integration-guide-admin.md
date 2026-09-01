# Frontend Integration Guide - Admin Panel

> **Base URL:** `{API_BASE_URL}` (e.g., `http://localhost:8080`)
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

1. [Service Category Management](#1-service-category-management)
2. [Service Attribute Definitions](#2-service-attribute-definitions)
3. [Service Filter Definitions](#3-service-filter-definitions)
4. [Quotation Scope Configuration](#4-quotation-scope-configuration)
5. [Quotation Field Definitions](#5-quotation-field-definitions)
6. [Vendor Management (Admin View)](#6-vendor-management-admin-view)
7. [Service Offering Administration](#7-service-offering-administration)
8. [Media File Management](#8-media-file-management)
9. [Booking Management (Admin View)](#9-booking-management-admin-view)
10. [Recommended Admin Page Structure](#10-recommended-admin-page-structure)

---

## 1. Service Category Management

**Base Path:** `/service-categories`

Service categories are the top-level taxonomy (e.g., "Tyres", "Car Wash", "AC Service"). They support parent-child hierarchy.

### 1.1 Get All Categories

```
GET /service-categories
```

**Response:** `ApiResponse<List<AvailableServiceResponse>>`

```json
{
  "status": "success",
  "code": "200",
  "data": [
    {
      "id": 1,
      "name": "Tyres",
      "description": "Tyre sales and fitting services",
      "imageId": 45,
      "parentId": null
    }
  ]
}
```

### 1.2 Get Root Categories (No Parent)

```
GET /service-categories/root
```

**Response:** `ApiResponse<List<AvailableServiceResponse>>`

### 1.3 Get Sub-Categories by Parent

```
GET /service-categories/parent/{parentId}
```

| Path Param | Type    | Description          |
|------------|---------|----------------------|
| parentId   | Integer | Parent category ID   |

### 1.4 Get Category by ID

```
GET /service-categories/{id}
```

### 1.5 Create Category

```
POST /service-categories
Content-Type: application/json
```

**Request Body:**
```json
{
  "name": "Car Tyres",
  "description": "All types of car tyres",
  "imageId": 45,
  "parentId": 1
}
```

| Field       | Type    | Required | Description                        |
|-------------|---------|----------|------------------------------------|
| name        | String  | Yes      | Category display name              |
| description | String  | No       | Category description               |
| imageId     | Long    | No       | MediaFile ID for category image    |
| parentId    | Integer | No       | Parent category ID (null for root) |

### 1.6 Update Category

```
PUT /service-categories/{id}
Content-Type: application/json
```

Same body as create.

### 1.7 Delete Category

```
DELETE /service-categories/{id}
```

### Frontend Flow

```
Admin Dashboard
  -> Service Categories page
     -> Tree/table view of categories (root + children)
     -> "Add Category" button -> modal/form
        -> Upload image first via /mediafiles/upload
        -> Use returned imageId in create request
     -> Edit/Delete actions per row
     -> Click category -> view sub-categories
```

---

## 2. Service Attribute Definitions

**Base Path:** `/admin/service-attributes`

Attributes define the measurable characteristics of offerings within a category (e.g., for "Tyres": brand, width, height, rim_size).

### 2.1 Get All Active Attributes

```
GET /admin/service-attributes
```

### 2.2 Get Attributes for Category

```
GET /admin/service-attributes/category/{categoryId}
```

### 2.3 Get All Attributes (Including Inactive)

```
GET /admin/service-attributes/category/{categoryId}/all
```

### 2.4 Get Filterable Attributes

```
GET /admin/service-attributes/category/{categoryId}/filterable
```

### 2.5 Get Required Attributes

```
GET /admin/service-attributes/category/{categoryId}/required
```

### 2.6 Get by ID

```
GET /admin/service-attributes/{id}
```

### 2.7 Get by Code

```
GET /admin/service-attributes/category/{categoryId}/code/{code}
```

### 2.8 Create Attribute Definition

```
POST /admin/service-attributes
Content-Type: application/json
```

**Request Body:**
```json
{
  "serviceCategoryId": 1,
  "attributeCode": "brand",
  "attributeName": "Brand",
  "dataType": "STRING",
  "unit": null,
  "isRequired": true,
  "isFilterable": true,
  "isSearchable": true,
  "displayOrder": 1,
  "description": "Tyre manufacturer brand",
  "defaultValue": null,
  "validationRules": null
}
```

| Field             | Type    | Required | Description                                               |
|-------------------|---------|----------|-----------------------------------------------------------|
| serviceCategoryId | Integer | Yes      | Category this attribute belongs to                        |
| attributeCode     | String  | Yes      | Unique code within category (e.g., "brand", "width")      |
| attributeName     | String  | Yes      | Display label                                             |
| dataType          | Enum    | Yes      | `STRING`, `NUMBER`, `BOOLEAN`, `DATE`, `JSON`             |
| unit              | String  | No       | Unit of measure (e.g., "mm", "inch")                      |
| isRequired        | Boolean | No       | Whether vendors must fill this attribute                  |
| isFilterable      | Boolean | No       | Whether customers can filter by this attribute            |
| isSearchable      | Boolean | No       | Whether this attribute is included in text search         |
| displayOrder      | Integer | No       | Order in which to display                                 |
| description       | String  | No       | Help text for vendors                                     |
| defaultValue      | String  | No       | Default value                                             |
| validationRules   | String  | No       | JSON string of validation rules                           |

### 2.9 Update Attribute

```
PUT /admin/service-attributes/{id}
Content-Type: application/json
```

Same body as create.

### 2.10 Soft Delete

```
DELETE /admin/service-attributes/{id}
```

### 2.11 Hard Delete (Permanent)

```
DELETE /admin/service-attributes/{id}/hard
```

### 2.12 Reactivate

```
POST /admin/service-attributes/{id}/reactivate
```

### Frontend Flow

```
Admin -> Select Category -> "Attributes" tab
  -> Table of attribute definitions
     Columns: Code | Name | Data Type | Required | Filterable | Order
  -> "Add Attribute" button -> form
  -> Edit/Delete/Reactivate actions per row
  -> Drag-and-drop reorder (update displayOrder)
```

---

## 3. Service Filter Definitions

**Base Path:** `/admin/service-filters`

Filters define the UI filter controls shown to customers when browsing a category. They build on top of attributes but add UI-specific configuration.

### 3.1 Get All Active Filters

```
GET /admin/service-filters
```

### 3.2 Get Filters for Category

```
GET /admin/service-filters/category/{categoryId}
```

### 3.3 Get All Filters (Including Inactive)

```
GET /admin/service-filters/category/{categoryId}/all
```

### 3.4 Get Required Filters

```
GET /admin/service-filters/category/{categoryId}/required
```

### 3.5 Get Dependent Filters

```
GET /admin/service-filters/category/{categoryId}/dependent/{parentFilterCode}
```

### 3.6 Get by ID / Code

```
GET /admin/service-filters/{id}
GET /admin/service-filters/category/{categoryId}/code/{code}
```

### 3.7 Create Filter Definition

```
POST /admin/service-filters
Content-Type: application/json
```

**Request Body:**
```json
{
  "serviceCategoryId": 1,
  "filterCode": "brand",
  "filterLabel": "Brand",
  "filterType": "MULTI_SELECT",
  "dataSource": "STATIC",
  "dataSourceConfig": null,
  "dependsOn": null,
  "displayOrder": 1,
  "isRequired": false,
  "allowMultiple": true,
  "placeholder": "Select brand(s)",
  "helpText": "Filter by tyre brand",
  "minValue": null,
  "maxValue": null,
  "stepValue": null,
  "options": [
    {
      "optionValue": "MRF",
      "optionLabel": "MRF",
      "displayOrder": 1,
      "isDefault": false,
      "isActive": true,
      "iconUrl": null,
      "parentValue": null,
      "metadata": null
    }
  ]
}
```

**Filter Types:** `DROPDOWN`, `MULTI_SELECT`, `RANGE`, `TEXT`, `LOCATION`, `AUTOCOMPLETE`, `DATE_RANGE`, `BOOLEAN`

### 3.8 Update Filter

```
PUT /admin/service-filters/{id}
```

### 3.9 Delete / Hard Delete / Reactivate

```
DELETE /admin/service-filters/{id}
DELETE /admin/service-filters/{id}/hard
POST  /admin/service-filters/{id}/reactivate
```

### 3.10 Manage Filter Options

```
POST   /admin/service-filters/{filterId}/options         -- Add option
GET    /admin/service-filters/{filterId}/options?parentValue=...  -- Get options
PUT    /admin/service-filters/options/{optionId}          -- Update option
DELETE /admin/service-filters/options/{optionId}          -- Delete option
```

**Filter Option Request Body:**
```json
{
  "optionValue": "MRF",
  "optionLabel": "MRF",
  "displayOrder": 1,
  "isDefault": false,
  "isActive": true,
  "iconUrl": null,
  "parentValue": null,
  "metadata": null
}
```

### Frontend Flow

```
Admin -> Select Category -> "Filters" tab
  -> List of filter definitions with type badge
  -> "Add Filter" form:
     - Basic: code, label, type, order, required
     - For RANGE: min/max/step fields
     - For DROPDOWN/MULTI_SELECT: manage options inline
     - Dependency: select parent filter
  -> Click filter -> manage options sub-panel
  -> Preview: show how filter looks to customers
```

---

## 4. Quotation Scope Configuration

**Base Path:** `/admin/quotation/scope`

Controls whether quotation is enabled for a category and how it works.

### 4.1 Save Scope Definition (Create or Update)

```
POST /admin/quotation/scope
Content-Type: application/json
```

**Request Body:**
```json
{
  "serviceCategoryId": 1,
  "isQuotationEnabled": true,
  "quotationType": "OPTIONAL",
  "autoExpiryHours": 72,
  "allowMultiVendorBroadcast": true,
  "maxBroadcastVendors": 10,
  "minQuotesRequired": 3
}
```

| Field                     | Type    | Description                                                  |
|---------------------------|---------|--------------------------------------------------------------|
| serviceCategoryId         | Integer | Target category                                              |
| isQuotationEnabled        | Boolean | Enable/disable quotation for category                        |
| quotationType             | Enum    | `OPTIONAL` / `MANDATORY` / `HYBRID`                         |
| autoExpiryHours           | Integer | Hours until request auto-expires                             |
| allowMultiVendorBroadcast | Boolean | Whether to send request to multiple vendors                  |
| maxBroadcastVendors       | Integer | Max vendors to broadcast to                                  |
| minQuotesRequired         | Integer | Min quotes before customer should decide                     |

**Quotation Types:**
- `OPTIONAL` - Vendor shows fixed price + customer can optionally request quote
- `MANDATORY` - No price shown, quote required
- `HYBRID` - Base price shown, custom quote for additional work

### 4.2 Get Scope for Category

```
GET /admin/quotation/scope/category/{categoryId}
```

### 4.3 Get Scope by ID

```
GET /admin/quotation/scope/{id}
```

### 4.4 Check if Quotation Enabled

```
GET /admin/quotation/scope/category/{categoryId}/enabled
```

**Response:** `ApiResponse<Boolean>`

### 4.5 Get All Quotation-Enabled Categories

```
GET /admin/quotation/scope/enabled
```

### 4.6 Disable Quotation for Category

```
POST /admin/quotation/scope/category/{categoryId}/disable
```

### 4.7 Delete Scope Definition

```
DELETE /admin/quotation/scope/{id}
```

### Frontend Flow

```
Admin -> Select Category -> "Quotation Settings" tab
  -> Toggle: Enable/Disable quotation
  -> If enabled:
     - Select quotation type (OPTIONAL/MANDATORY/HYBRID)
     - Set auto-expiry hours
     - Toggle multi-vendor broadcast
     - Set max broadcast vendors
     - Set minimum quotes required
  -> Save button
```

---

## 5. Quotation Field Definitions

**Base Path:** `/admin/quotation/field`

Defines dynamic form fields shown to customers when they submit a quotation request for a category.

### 5.1 Create Field Definition

```
POST /admin/quotation/field
Content-Type: application/json
```

**Request Body:**
```json
{
  "serviceCategoryId": 1,
  "fieldCode": "vehicle_make",
  "label": "Vehicle Make",
  "fieldType": "DROPDOWN",
  "isRequired": true,
  "displayOrder": 1,
  "placeholder": "Select make",
  "helpText": "Select your vehicle manufacturer",
  "defaultValue": null,
  "options": ["Toyota", "Honda", "BMW", "Mercedes"],
  "validationRules": {},
  "dependsOnFieldId": null,
  "dependsOnCondition": null
}
```

| Field              | Type          | Description                                                |
|--------------------|---------------|------------------------------------------------------------|
| serviceCategoryId  | Integer       | Category this field belongs to                             |
| fieldCode          | String        | Unique code within category                                |
| label              | String        | Display label                                              |
| fieldType          | Enum          | `TEXT`, `NUMBER`, `DROPDOWN`, `MULTI_SELECT`, `BOOLEAN`, `FILE_UPLOAD`, `DATE`, `TEXTAREA` |
| isRequired         | Boolean       | Whether customer must fill this                            |
| displayOrder       | Integer       | Display order                                              |
| placeholder        | String        | Placeholder text                                           |
| helpText           | String        | Help text shown below field                                |
| defaultValue       | String        | Default value                                              |
| options            | List<String>  | Options for DROPDOWN/MULTI_SELECT                          |
| validationRules    | Map           | Custom validation rules (JSON)                             |
| dependsOnFieldId   | Integer       | ID of field this depends on                                |
| dependsOnCondition | Map           | Condition for when this field appears                      |

### 5.2 Update Field

```
PUT /admin/quotation/field/{fieldId}
```

### 5.3 Get Fields for Category

```
GET /admin/quotation/field/category/{categoryId}
```

### 5.4 Get Required Fields

```
GET /admin/quotation/field/category/{categoryId}/required
```

### 5.5 Get Field by ID

```
GET /admin/quotation/field/{fieldId}
```

### 5.6 Delete Field

```
DELETE /admin/quotation/field/{fieldId}
```

### 5.7 Reorder Fields

```
POST /admin/quotation/field/category/{categoryId}/reorder
Content-Type: application/json

[3, 1, 5, 2, 4]   // List of field IDs in desired order
```

### Frontend Flow

```
Admin -> Select Category -> "Quotation Form Builder" tab
  -> Visual form builder with drag-and-drop reorder
  -> "Add Field" -> choose type -> configure:
     - TEXT: label, placeholder, required
     - DROPDOWN: label, options list, required
     - FILE_UPLOAD: label, accepted formats
     - etc.
  -> Set up conditional fields (dependsOnFieldId + condition)
  -> Preview: show rendered form as customer would see it
  -> Reorder: drag fields -> POST /reorder with new order
```

---

## 6. Vendor Management (Admin View)

**Base Path:** `/vendors`

### 6.1 Get All Vendors

```
GET /vendors/all
```

**Response:** `List<VendorProfileResponse>`

```json
[
  {
    "id": 1,
    "companyBrandName": "QuickFit Tyres",
    "webSite": "https://quickfit.ae",
    "email": "info@quickfit.ae",
    "phoneNumber": "+971501234567",
    "openingTime": "08:00",
    "closingTime": "20:00",
    "workingDays": "Mon-Sat",
    "companyLogo": { "id": 10, ... },
    "companyLocation": { ... },
    "companyImages": [ ... ]
  }
]
```

### 6.2 Get Vendor by ID

```
GET /vendors/{id}
```

### 6.3 Search Vendors

```
GET /vendors/search?city=Dubai&zip=12345&vendorName=Quick&serviceCategoryIds=1,2
```

| Query Param        | Type         | Description              |
|--------------------|--------------|--------------------------|
| city               | String       | Filter by city           |
| zip                | String       | Filter by zip code       |
| vendorName         | String       | Search by name           |
| serviceCategoryIds | List<Integer>| Filter by service types  |

### 6.4 Search by Brand Name

```
GET /vendors/brandname/{name}
```

### 6.5 Get Vendors by Service Type

```
GET /vendors/service/{serviceType}
```

### 6.6 Update Vendor

```
PUT /vendors/{id}
Content-Type: application/json
```

**Request Body (OtherServiceRequest):**
```json
{
  "companyBrandName": "QuickFit Tyres LLC",
  "companyArabicName": "كويك فيت",
  "name": "QuickFit",
  "trnNumber": "TRN123456",
  "webSite": "https://quickfit.ae",
  "email": "info@quickfit.ae",
  "phoneNumber": "+971-4-1234567",
  "mobileNumber": "+971501234567",
  "companyLocation": {
    "address": "Al Quoz Industrial Area",
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

### 6.7 Delete Vendor

```
DELETE /vendors/{id}
```

### 6.8 Vendor Service Bridges (Admin)

**Base Path:** `/vendor-service-bridges`

Links vendors to service categories.

```
GET  /vendor-service-bridges                          -- Get all
GET  /vendor-service-bridges/{id}                     -- Get by ID
GET  /vendor-service-bridges/vendor/{vendorId}        -- Get by vendor
POST /vendor-service-bridges                          -- Create
PUT  /vendor-service-bridges/{id}                     -- Update
DELETE /vendor-service-bridges/{id}                   -- Delete
```

**Create Request:**
```json
{
  "garageId": 1,
  "serviceId": 3,
  "isActive": true
}
```

---

## 7. Service Offering Administration

**Base Path:** `/service-offerings`

Admins can manage all vendor offerings.

### 7.1 Hard Delete (Admin Only)

```
DELETE /service-offerings/{id}/hard
```

Permanently removes an offering (unlike soft delete which just deactivates).

### 7.2 Reactivate Soft-Deleted

```
POST /service-offerings/{id}/reactivate
```

### 7.3 Toggle Featured

```
PATCH /service-offerings/{id}/featured
```

### 7.4 View All Vendor Offerings (Including Inactive)

```
GET /service-offerings/vendor/{vendorId}/all
```

### 7.5 Update Stock Status

```
PATCH /service-offerings/{id}/stock?status=OUT_OF_STOCK&quantity=0
```

**Stock Statuses:** `IN_STOCK`, `LIMITED_STOCK`, `OUT_OF_STOCK`, `PRE_ORDER`, `DISCONTINUED`

---

## 8. Media File Management

**Base Path:** `/mediafiles`

### 8.1 Upload Files

```
POST /mediafiles/upload
Content-Type: multipart/form-data
```

**Form Data:**
- `files`: List of files (max 10MB each)

**Response:**
```json
{
  "status": "success",
  "message": "Files uploaded successfully",
  "data": [
    {
      "id": 45,
      "fileName": "logo.png",
      "fileUrl": "https://...",
      "fileType": "image/png",
      "fileSize": 102400
    }
  ]
}
```

### 8.2 Delete File

```
DELETE /mediafiles/delete-image/{id}
```

### Frontend Integration

```javascript
// Upload helper
async function uploadFiles(files) {
  const formData = new FormData();
  files.forEach(file => formData.append('files', file));
  
  const response = await fetch(`${BASE_URL}/mediafiles/upload`, {
    method: 'POST',
    body: formData
  });
  return response.json(); // Returns { data: [{ id, fileName, fileUrl, ... }] }
}

// Use returned IDs in other requests
const uploaded = await uploadFiles([logoFile]);
const logoId = uploaded.data[0].id;
// Then use logoId in ServiceCategoryRequest, OtherServiceRequest, etc.
```

---

## 9. Booking Management (Admin View)

**Base Path:** `/os-appointments`

### 9.1 Get Appointments by Vendor/Service

```
GET /os-appointments/other-service/{otherServiceId}?status=PENDING
```

| Param          | Type    | Description                      |
|----------------|---------|----------------------------------|
| otherServiceId | Integer | Vendor ID                        |
| status         | String  | Optional filter (enum Status)    |

### 9.2 Get Appointment Details

```
GET /os-appointments/{appointmentId}
```

### 9.3 Update Appointment Status

```
PUT /os-appointments/status
Content-Type: application/json
```

```json
{
  "appointmentId": 123,
  "memberId": 1,
  "memberType": "ADMIN",
  "status": "CONFIRMED",
  "comments": "Confirmed by admin"
}
```

**Common Status Values:** `REQUESTED`, `CONFIRMED`, `SCHEDULED`, `WORK_INPROGRESS`, `COMPLETED`, `CANCELLED`, `DELIVERED`

---

## 10. Recommended Admin Page Structure

```
/admin
  /dashboard                    -- Overview stats
  /categories                   -- Service Category CRUD
    /:id                        -- Category detail
      /attributes               -- Attribute definitions
      /filters                  -- Filter definitions
      /quotation-settings       -- Quotation scope config
      /quotation-fields         -- Quotation form builder
  /vendors                      -- Vendor list & management
    /:id                        -- Vendor detail
      /services                 -- Vendor service bridges
      /offerings                -- Vendor's offerings
  /offerings                    -- All offerings across vendors
  /bookings                     -- All bookings
    /:id                        -- Booking details
  /media                        -- Media library
```

### Typical Admin Workflows

**1. Set Up a New Service Category:**
```
1. Upload category image        POST /mediafiles/upload
2. Create category              POST /service-categories
3. Define attributes            POST /admin/service-attributes (repeat)
4. Define filters               POST /admin/service-filters (repeat)
5. Configure quotation scope    POST /admin/quotation/scope
6. Define quotation form fields POST /admin/quotation/field (repeat)
```

**2. Manage Vendor:**
```
1. View all vendors             GET /vendors/all
2. View vendor detail           GET /vendors/{id}
3. View vendor services         GET /vendor-service-bridges/vendor/{id}
4. View vendor offerings        GET /service-offerings/vendor/{id}/all
5. Toggle featured offering     PATCH /service-offerings/{id}/featured
6. Hard delete bad offering     DELETE /service-offerings/{id}/hard
```

**3. Handle Bookings:**
```
1. View bookings for vendor     GET /os-appointments/other-service/{id}
2. View booking details         GET /os-appointments/{id}
3. Update status                PUT /os-appointments/status
```

---

## 11. Axios API Reference

### 11.1 Axios Setup

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

// Response interceptor to unwrap ApiResponse
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

### 11.2 Service Category APIs

```javascript
// api/admin/serviceCategoryApi.js
import api from '../axiosInstance';

const SERVICE_CATEGORY_BASE = '/service-categories';

// Get all categories
export const getAllCategories = () =>
  api.get(SERVICE_CATEGORY_BASE);

// Get root categories (no parent)
export const getRootCategories = () =>
  api.get(`${SERVICE_CATEGORY_BASE}/root`);

// Get sub-categories by parent
export const getSubCategories = (parentId) =>
  api.get(`${SERVICE_CATEGORY_BASE}/parent/${parentId}`);

// Get category by ID
export const getCategoryById = (id) =>
  api.get(`${SERVICE_CATEGORY_BASE}/${id}`);

// Create category
export const createCategory = (data) =>
  api.post(SERVICE_CATEGORY_BASE, data);
// data = { name, description, imageId, parentId }

// Update category
export const updateCategory = (id, data) =>
  api.put(`${SERVICE_CATEGORY_BASE}/${id}`, data);
// data = { name, description, imageId, parentId }

// Delete category
export const deleteCategory = (id) =>
  api.delete(`${SERVICE_CATEGORY_BASE}/${id}`);
```

---

### 11.3 Service Attribute Definition APIs

```javascript
// api/admin/serviceAttributeApi.js
import api from '../axiosInstance';

const ATTR_BASE = '/admin/service-attributes';

// Get all active attributes
export const getAllAttributes = () =>
  api.get(ATTR_BASE);

// Get attributes for a category
export const getAttributesByCategory = (categoryId) =>
  api.get(`${ATTR_BASE}/category/${categoryId}`);

// Get all attributes for a category (including inactive)
export const getAllAttributesByCategory = (categoryId) =>
  api.get(`${ATTR_BASE}/category/${categoryId}/all`);

// Get filterable attributes for a category
export const getFilterableAttributes = (categoryId) =>
  api.get(`${ATTR_BASE}/category/${categoryId}/filterable`);

// Get required attributes for a category
export const getRequiredAttributes = (categoryId) =>
  api.get(`${ATTR_BASE}/category/${categoryId}/required`);

// Get attribute by ID
export const getAttributeById = (id) =>
  api.get(`${ATTR_BASE}/${id}`);

// Get attribute by code within a category
export const getAttributeByCode = (categoryId, code) =>
  api.get(`${ATTR_BASE}/category/${categoryId}/code/${code}`);

// Create attribute definition
export const createAttribute = (data) =>
  api.post(ATTR_BASE, data);
// data = {
//   serviceCategoryId, attributeCode, attributeName, dataType,
//   unit, isRequired, isFilterable, isSearchable, displayOrder,
//   description, defaultValue, validationRules
// }

// Update attribute definition
export const updateAttribute = (id, data) =>
  api.put(`${ATTR_BASE}/${id}`, data);

// Soft delete attribute
export const deleteAttribute = (id) =>
  api.delete(`${ATTR_BASE}/${id}`);

// Hard delete attribute (permanent)
export const hardDeleteAttribute = (id) =>
  api.delete(`${ATTR_BASE}/${id}/hard`);

// Reactivate soft-deleted attribute
export const reactivateAttribute = (id) =>
  api.post(`${ATTR_BASE}/${id}/reactivate`);
```

---

### 11.4 Service Filter Definition APIs

```javascript
// api/admin/serviceFilterApi.js
import api from '../axiosInstance';

const FILTER_BASE = '/admin/service-filters';

// Get all active filters
export const getAllFilters = () =>
  api.get(FILTER_BASE);

// Get filters for a category
export const getFiltersByCategory = (categoryId) =>
  api.get(`${FILTER_BASE}/category/${categoryId}`);

// Get all filters for a category (including inactive)
export const getAllFiltersByCategory = (categoryId) =>
  api.get(`${FILTER_BASE}/category/${categoryId}/all`);

// Get required filters for a category
export const getRequiredFilters = (categoryId) =>
  api.get(`${FILTER_BASE}/category/${categoryId}/required`);

// Get dependent filters
export const getDependentFilters = (categoryId, parentFilterCode) =>
  api.get(`${FILTER_BASE}/category/${categoryId}/dependent/${parentFilterCode}`);

// Get filter by ID
export const getFilterById = (id) =>
  api.get(`${FILTER_BASE}/${id}`);

// Get filter by code within a category
export const getFilterByCode = (categoryId, code) =>
  api.get(`${FILTER_BASE}/category/${categoryId}/code/${code}`);

// Create filter definition
export const createFilter = (data) =>
  api.post(FILTER_BASE, data);
// data = {
//   serviceCategoryId, filterCode, filterLabel, filterType,
//   dataSource, dataSourceConfig, dependsOn, displayOrder,
//   isRequired, allowMultiple, placeholder, helpText,
//   minValue, maxValue, stepValue, options: [{ optionValue, optionLabel, ... }]
// }

// Update filter definition
export const updateFilter = (id, data) =>
  api.put(`${FILTER_BASE}/${id}`, data);

// Soft delete filter
export const deleteFilter = (id) =>
  api.delete(`${FILTER_BASE}/${id}`);

// Hard delete filter (permanent)
export const hardDeleteFilter = (id) =>
  api.delete(`${FILTER_BASE}/${id}/hard`);

// Reactivate soft-deleted filter
export const reactivateFilter = (id) =>
  api.post(`${FILTER_BASE}/${id}/reactivate`);

// --- Filter Options ---

// Add option to a filter
export const addFilterOption = (filterId, data) =>
  api.post(`${FILTER_BASE}/${filterId}/options`, data);
// data = { optionValue, optionLabel, displayOrder, isDefault, isActive, iconUrl, parentValue, metadata }

// Get options for a filter
export const getFilterOptions = (filterId, parentValue = null) =>
  api.get(`${FILTER_BASE}/${filterId}/options`, {
    params: { parentValue },
  });

// Update filter option
export const updateFilterOption = (optionId, data) =>
  api.put(`${FILTER_BASE}/options/${optionId}`, data);

// Delete filter option
export const deleteFilterOption = (optionId) =>
  api.delete(`${FILTER_BASE}/options/${optionId}`);
```

---

### 11.5 Quotation Scope APIs

```javascript
// api/admin/quotationScopeApi.js
import api from '../axiosInstance';

const SCOPE_BASE = '/admin/quotation/scope';

// Create or update quotation scope
export const saveScopeDefinition = (data) =>
  api.post(SCOPE_BASE, data);
// data = {
//   serviceCategoryId, isQuotationEnabled, quotationType,
//   autoExpiryHours, allowMultiVendorBroadcast,
//   maxBroadcastVendors, minQuotesRequired
// }

// Get scope for a category
export const getScopeByCategory = (categoryId) =>
  api.get(`${SCOPE_BASE}/category/${categoryId}`);

// Get scope by ID
export const getScopeById = (id) =>
  api.get(`${SCOPE_BASE}/${id}`);

// Check if quotation is enabled for a category
export const isQuotationEnabled = (categoryId) =>
  api.get(`${SCOPE_BASE}/category/${categoryId}/enabled`);

// Get all quotation-enabled categories
export const getQuotationEnabledCategories = () =>
  api.get(`${SCOPE_BASE}/enabled`);

// Disable quotation for a category
export const disableQuotation = (categoryId) =>
  api.post(`${SCOPE_BASE}/category/${categoryId}/disable`);

// Delete scope definition
export const deleteScopeDefinition = (id) =>
  api.delete(`${SCOPE_BASE}/${id}`);
```

---

### 11.6 Quotation Field Definition APIs

```javascript
// api/admin/quotationFieldApi.js
import api from '../axiosInstance';

const FIELD_BASE = '/admin/quotation/field';

// Create field definition
export const createField = (data) =>
  api.post(FIELD_BASE, data);
// data = {
//   serviceCategoryId, fieldCode, label, fieldType,
//   isRequired, displayOrder, placeholder, helpText,
//   defaultValue, options, validationRules,
//   dependsOnFieldId, dependsOnCondition
// }

// Update field definition
export const updateField = (fieldId, data) =>
  api.put(`${FIELD_BASE}/${fieldId}`, data);

// Get fields for a category
export const getFieldsByCategory = (categoryId) =>
  api.get(`${FIELD_BASE}/category/${categoryId}`);

// Get required fields for a category
export const getRequiredFields = (categoryId) =>
  api.get(`${FIELD_BASE}/category/${categoryId}/required`);

// Get field by ID
export const getFieldById = (fieldId) =>
  api.get(`${FIELD_BASE}/${fieldId}`);

// Delete field definition
export const deleteField = (fieldId) =>
  api.delete(`${FIELD_BASE}/${fieldId}`);

// Reorder fields within a category
export const reorderFields = (categoryId, fieldIds) =>
  api.post(`${FIELD_BASE}/category/${categoryId}/reorder`, fieldIds);
// fieldIds = [3, 1, 5, 2, 4]  -- array of field IDs in desired order
```

---

### 11.7 Vendor Management APIs (Admin View)

```javascript
// api/admin/vendorManagementApi.js
import api from '../axiosInstance';

const VENDOR_BASE = '/vendors';

// Get all vendors
export const getAllVendors = () =>
  api.get(`${VENDOR_BASE}/all`);

// Get vendor by ID
export const getVendorById = (id) =>
  api.get(`${VENDOR_BASE}/${id}`);

// Search vendors
export const searchVendors = ({ city, zip, vendorName, serviceCategoryIds } = {}) =>
  api.get(`${VENDOR_BASE}/search`, {
    params: { city, zip, vendorName, serviceCategoryIds: serviceCategoryIds?.join(',') },
  });

// Search vendors by brand name
export const searchByBrandName = (name) =>
  api.get(`${VENDOR_BASE}/brandname/${name}`);

// Get vendors by service type
export const getVendorsByServiceType = (serviceType) =>
  api.get(`${VENDOR_BASE}/service/${serviceType}`);

// Update vendor
export const updateVendor = (id, data) =>
  api.put(`${VENDOR_BASE}/${id}`, data);
// data = OtherServiceRequest (see Section 6)

// Delete vendor
export const deleteVendor = (id) =>
  api.delete(`${VENDOR_BASE}/${id}`);
```

---

### 11.8 Vendor Service Bridge APIs

```javascript
// api/admin/vendorServiceBridgeApi.js
import api from '../axiosInstance';

const BRIDGE_BASE = '/vendor-service-bridges';

// Get all bridges
export const getAllBridges = () =>
  api.get(BRIDGE_BASE);

// Get bridge by ID
export const getBridgeById = (id) =>
  api.get(`${BRIDGE_BASE}/${id}`);

// Get bridges by vendor
export const getBridgesByVendor = (vendorId) =>
  api.get(`${BRIDGE_BASE}/vendor/${vendorId}`);

// Create bridge
export const createBridge = (data) =>
  api.post(BRIDGE_BASE, data);
// data = { garageId, serviceId, isActive }

// Update bridge
export const updateBridge = (id, data) =>
  api.put(`${BRIDGE_BASE}/${id}`, data);

// Delete bridge
export const deleteBridge = (id) =>
  api.delete(`${BRIDGE_BASE}/${id}`);
```

---

### 11.9 Service Offering Admin APIs

```javascript
// api/admin/serviceOfferingAdminApi.js
import api from '../axiosInstance';

const OFFERING_BASE = '/service-offerings';

// Get all offerings for a vendor (including inactive)
export const getAllVendorOfferings = (vendorId) =>
  api.get(`${OFFERING_BASE}/vendor/${vendorId}/all`);

// Hard delete offering (permanent - admin only)
export const hardDeleteOffering = (id) =>
  api.delete(`${OFFERING_BASE}/${id}/hard`);

// Reactivate soft-deleted offering
export const reactivateOffering = (id) =>
  api.post(`${OFFERING_BASE}/${id}/reactivate`);

// Toggle featured status
export const toggleFeatured = (id) =>
  api.patch(`${OFFERING_BASE}/${id}/featured`);

// Update stock status
export const updateStockStatus = (id, status, quantity = null) =>
  api.patch(`${OFFERING_BASE}/${id}/stock`, null, {
    params: { status, quantity },
  });
```

---

### 11.10 Media File APIs

```javascript
// api/admin/mediaFileApi.js
import api from '../axiosInstance';

const MEDIA_BASE = '/mediafiles';

// Upload files (multipart)
export const uploadFiles = (files) => {
  const formData = new FormData();
  files.forEach((file) => formData.append('files', file));
  return api.post(`${MEDIA_BASE}/upload`, formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
    timeout: 60000, // longer timeout for uploads
  });
};

// Delete a file
export const deleteFile = (id) =>
  api.delete(`${MEDIA_BASE}/delete-image/${id}`);
```

---

### 11.11 Booking Admin APIs

```javascript
// api/admin/bookingAdminApi.js
import api from '../axiosInstance';

const BOOKING_BASE = '/os-appointments';

// Get appointments by vendor/service
export const getAppointmentsByVendor = (otherServiceId, status = null) =>
  api.get(`${BOOKING_BASE}/other-service/${otherServiceId}`, {
    params: { status },
  });

// Get appointment details
export const getAppointmentDetails = (appointmentId) =>
  api.get(`${BOOKING_BASE}/${appointmentId}`);

// Update appointment status
export const updateAppointmentStatus = (data) =>
  api.put(`${BOOKING_BASE}/status`, data);
// data = { appointmentId, memberId, memberType: "ADMIN", status, comments }
```

---

### 11.12 Usage Example (React Component)

```javascript
// Example: Admin category management page
import React, { useEffect, useState } from 'react';
import {
  getAllCategories,
  createCategory,
  updateCategory,
  deleteCategory,
} from '../api/admin/serviceCategoryApi';
import { uploadFiles } from '../api/admin/mediaFileApi';

const CategoryManagement = () => {
  const [categories, setCategories] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadCategories();
  }, []);

  const loadCategories = async () => {
    try {
      setLoading(true);
      const { data } = await getAllCategories();
      setCategories(data.data); // unwrap ApiResponse
    } catch (err) {
      console.error('Failed to load categories:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleCreate = async (formValues, imageFile) => {
    try {
      let imageId = null;
      if (imageFile) {
        const uploadRes = await uploadFiles([imageFile]);
        imageId = uploadRes.data.data[0].id;
      }
      await createCategory({
        name: formValues.name,
        description: formValues.description,
        imageId,
        parentId: formValues.parentId || null,
      });
      loadCategories(); // refresh list
    } catch (err) {
      console.error('Failed to create category:', err);
    }
  };

  const handleDelete = async (id) => {
    if (!window.confirm('Delete this category?')) return;
    try {
      await deleteCategory(id);
      loadCategories();
    } catch (err) {
      console.error('Failed to delete category:', err);
    }
  };

  // ... render table, modals, etc.
};

export default CategoryManagement;
```
