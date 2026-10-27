# Frontend Integration Guide - Admin Panel

> **Base URL:** `{API_BASE_URL}` (e.g., `http://localhost:9010/api/v1`)
> **Language:** send `Accept-Language: ar` to get Arabic in the display fields (`name`, `description`, `attributeName`, `label`, `filterLabel`, `optionLabel`, `placeholder`, `helpText`). The raw Arabic is always returned too (`nameAr`, `attributeNameAr`, `labelAr`, ...) for edit forms.
> **Standard Response Wrapper:**
> ```json
> {
>   "status": "success",
>   "code": "200",
>   "message": "success",
>   "data": { ... }
> }
> ```
> Not wrapped: `/vendors/**` returns the DTO directly.
>
> **Errors** come back as **HTTP 201** with `"status": "failed"` and the real status in `code`, so check the body, not the HTTP status:
> ```json
> { "status": "failed", "code": "400", "message": "Attribute 'tyre_brand' is a SINGLE_SELECT and needs at least one option", "body": null }
> ```
> Validation errors list each failing field in `body`: `[{ "field": "...", "message": "..." }]`.
>
> **Nothing is hard-deleted.** Every `DELETE` below deactivates the record (`isActive: false`); offerings, bookings and quotes keep pointing at it.

---

## Table of Contents

1. [Service Category Management](#1-service-category-management)
2. [Catalog Services, Markets & Schema Versions](#2-catalog-services-markets--schema-versions)
3. [Service Attribute Definitions](#3-service-attribute-definitions)
4. [Service Filter Definitions](#4-service-filter-definitions)
5. [Quotation Scope Configuration](#5-quotation-scope-configuration)
6. [Quotation Field Definitions](#6-quotation-field-definitions)
7. [Vendor Management (Admin View)](#7-vendor-management-admin-view)
8. [Service Offering Administration](#8-service-offering-administration)
9. [Media File Management](#9-media-file-management)
10. [Booking Management (Admin View)](#10-booking-management-admin-view)
11. [Recommended Admin Page Structure](#11-recommended-admin-page-structure)
12. [Axios API Reference](#12-axios-api-reference)

---

## 1. Service Category Management

**Base Path:** `/service-categories`

Categories are the top level of the catalog (e.g., "Tires & Wheels", "Washing & Detailing"). Under each category sit **catalog services** (section 2), and vendors' offerings are listed against those services. Categories support a parent-child hierarchy, but the starter catalog has 10 root categories and no children.

Vendor onboarding files each vendor service type under a category by `code` (e.g. `CAR_WASH` → `CLEANING_DETAILING`), so don't change the seeded codes.

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
      "id": 3,
      "code": "TIRES_WHEELS",
      "name": "Tires & Wheels",
      "nameAr": "الإطارات والعجلات",
      "description": null,
      "descriptionAr": null,
      "displayOrder": 30,
      "parentId": null,
      "image": null,
      "isActive": true
    }
  ]
}
```

The lists include deactivated categories; check `isActive`.

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
  "code": "TIRES_WHEELS",
  "name": "Tires & Wheels",
  "nameAr": "الإطارات والعجلات",
  "description": "Tyre sales, fitting and wheel services",
  "descriptionAr": null,
  "displayOrder": 30,
  "imageId": 45,
  "parentId": null
}
```

| Field         | Type    | Required | Description                                  |
|---------------|---------|----------|----------------------------------------------|
| code          | String  | No       | Stable machine code; stored upper-case, unique |
| name          | String  | Yes      | English display name                         |
| nameAr        | String  | No       | Arabic display name                          |
| description   | String  | No       | Category description                         |
| descriptionAr | String  | No       | Arabic description                           |
| displayOrder  | Integer | No       | Sort order                                   |
| imageId       | Long    | No       | MediaFile ID for category image              |
| parentId      | Integer | No       | Parent category ID (null for root)           |

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

Deactivates the category (`isActive: false`).

### Frontend Flow

```
Admin Dashboard
  -> Service Categories page
     -> Tree/table view of categories (root + children)
     -> "Add Category" button -> modal/form
        -> Upload image first via /mediafiles/upload
        -> Use returned imageId in create request
     -> Edit/Deactivate actions per row
     -> Click category -> its catalog services (section 2)
```

---

## 2. Catalog Services, Markets & Schema Versions

**Base Path:** `/admin/services`

A **catalog service** is one thing a customer can buy within a category, e.g. Tires & Wheels → Tire Replacement. Offerings, quotation requests and bookings point at a service. Each service has:

- a **quotation mode**: whether customers book directly, ask for quotes, or either;
- **markets**: the countries it is sold in, each with its currency;
- **schema versions**: its attribute form, versioned so published forms never change under existing offerings.

There is no delete; deactivate instead.

### 2.1 Create Service

```
POST /admin/services
Content-Type: application/json
```

```json
{
  "serviceCategoryId": 3,
  "code": "TIRE_REPLACEMENT",
  "name": "Tire Replacement",
  "nameAr": "تبديل الإطارات",
  "description": "Supply and fit new tyres",
  "descriptionAr": null,
  "quotationMode": null,
  "fulfillmentMode": "WORKSHOP",
  "estimatedDurationMinutes": 60,
  "displayOrder": 10
}
```

| Field                    | Type    | Required | Description                                                   |
|--------------------------|---------|----------|---------------------------------------------------------------|
| serviceCategoryId        | Integer | Yes      | Category the service belongs to                               |
| code                     | String  | Yes      | Stored upper-case; must be unique                             |
| name                     | String  | Yes      | English name                                                  |
| nameAr, description, descriptionAr | String | No | Arabic name and descriptions                              |
| quotationMode            | Enum    | No       | `DIRECT`, `OPTIONAL`, `REQUIRED`; null = follow the quotation scope (section 5) |
| fulfillmentMode          | Enum    | No       | `WORKSHOP` (default), `MOBILE`, `BOTH`                        |
| estimatedDurationMinutes | Integer | No       | Must be greater than 0                                        |
| displayOrder             | Integer | No       | Sort order (default 0)                                        |

**Response:** `ApiResponse<CatalogServiceResponse>`

```json
{
  "data": {
    "id": 5,
    "serviceCategoryId": 3,
    "serviceCategoryName": "Tires & Wheels",
    "code": "TIRE_REPLACEMENT",
    "name": "Tire Replacement",
    "nameAr": "تبديل الإطارات",
    "quotationMode": null,
    "effectiveQuotationMode": "DIRECT",
    "fulfillmentMode": "WORKSHOP",
    "estimatedDurationMinutes": 60,
    "displayOrder": 10,
    "isActive": true,
    "createdAt": "2026-10-27T10:00:00",
    "updatedAt": "2026-10-27T10:00:00"
  }
}
```

### 2.2 List / Get / Update

```
GET /admin/services?categoryId=3&activeOnly=false
GET /admin/services/{id}
GET /admin/services/code/{code}
PUT /admin/services/{id}
```

`PUT` takes the same body as create; only the fields you send change.

### 2.3 Deactivate / Reactivate

```
POST /admin/services/{id}/deactivate
POST /admin/services/{id}/reactivate
```

### 2.4 Quotation Mode

`effectiveQuotationMode` is what customers get:

1. the service's own `quotationMode`, when set;
2. otherwise the service's own quotation scope (section 5, saved with `serviceId`);
3. otherwise the category's quotation scope;
4. otherwise `DIRECT`.

A scope maps to a mode like this: quotation disabled → `DIRECT`; `MANDATORY` → `REQUIRED`; `OPTIONAL` or `HYBRID` → `OPTIONAL`.

| Mode       | Customers can                                                |
|------------|--------------------------------------------------------------|
| `DIRECT`   | Book an offering; quotation requests are refused             |
| `OPTIONAL` | Book directly or ask for quotes                              |
| `REQUIRED` | Only ask for quotes; direct booking is refused               |

To clear a service's own mode and go back to following the scope:

```
POST /admin/services/{id}/quotation-mode/reset
```

### 2.5 Markets

A service is only sold in the countries it has a market for. An offering is priced in its market's currency: a vendor's offering in another currency, or in a country without a market, is refused.

```
GET /admin/services/{id}/markets
PUT /admin/services/{id}/markets
Content-Type: application/json
```

```json
{
  "countryCode": "SA",
  "currencyCode": "SAR",
  "isAvailable": true,
  "regulatoryConfig": { "requiresLicence": true },
  "regulatoryNote": "Workshop must hold a municipality licence",
  "regulatoryNoteAr": null
}
```

| Field            | Type    | Required | Description                                              |
|------------------|---------|----------|----------------------------------------------------------|
| countryCode      | String  | Yes      | ISO 3166 alpha-2; one market per country (PUT creates or updates it) |
| currencyCode     | String  | New only | ISO 4217; required when the market is new                |
| isAvailable      | Boolean | No       | Default `true`; `false` hides the service in that country |
| regulatoryConfig | Object  | No       | Free-form JSON object                                    |
| regulatoryNote, regulatoryNoteAr | String | No | Notes shown with the service                     |

**Response** item (`MarketAvailabilityResponse`): `id`, `serviceId`, `serviceCode`, `countryCode`, `currencyCode`, `isAvailable`, `regulatoryConfig` (JSON string), `regulatoryNote`, `regulatoryNoteAr`, `updatedAt`.

### 2.6 Schema Versions

A service's attributes live in numbered versions: `DRAFT` → `PUBLISHED` → `RETIRED`. Only the latest published version is live for offerings, quotation requests and search. Published and retired versions can't be edited; changes go into a draft.

```
GET  /admin/services/{id}/schema-versions                           -- newest first
POST /admin/services/{id}/schema-versions?notes=Add%20load%20index  -- open a draft
GET  /admin/services/{id}/schema-versions/{versionId}/attributes
POST /admin/services/{id}/schema-versions/{versionId}/publish
```

- Opening a draft returns the existing draft if there is one. Otherwise it creates the next version, starting as a copy of the published version's attributes.
- `POST /admin/service-attributes` with `serviceId` (section 3) adds the attribute to the service's draft, opening one if needed. Edit or deactivate the draft's copies the same way.
- Publishing retires the previous published version. Offerings, quotation requests and bookings created under it keep its `schemaVersionId`.
- Only a `DRAFT` can be published.

**Response** item (`SchemaVersionResponse`): `id`, `serviceId`, `serviceCode`, `versionNo`, `status`, `notes`, `attributeCount`, `publishedAt`, `createdAt`.

### Frontend Flow

```
Admin -> Category -> "Services" tab
  -> Table: Code | Name | Mode (effective) | Fulfillment | Active
  -> "Add Service" -> form (section 2.1)
  -> Service detail
     -> "Markets" tab: one row per country (country, currency, available)
     -> "Form" tab: versions list; "Edit form" opens a draft,
        add/edit attributes in the draft, then "Publish"
     -> "Quotation" tab: mode override, or reset to follow the scope
```

---

## 3. Service Attribute Definitions

**Base Path:** `/admin/service-attributes`

Attributes define the fields of an offering or request (e.g., for tyres: brand, width, rim_size). They live at two levels:

- **Category defaults** (`serviceCategoryId`, no `serviceId`): apply to every service in the category.
- **Service attributes** (`serviceId`): belong to a schema version of that service (section 2.6). A service attribute overrides a category default with the same code.

`inputContext` says who fills an attribute in:

| inputContext | Filled in by                                                               |
|--------------|----------------------------------------------------------------------------|
| `BOTH`       | Default. Vendor on the offering, and customer as a search filter           |
| `PROVIDER`   | Vendor on the offering                                                     |
| `CUSTOMER`   | Customer (e.g. `vehicle_size`); never required on an offering             |
| `VENDOR`     | Vendor once per branch when adding a service (e.g. a car wash's `bay_count`). Category-level only; never on offering or quotation forms. Vendors read them from `GET /vendors/services/attributes?serviceType=` |

### 3.1 Get All Active Attributes

```
GET /admin/service-attributes
```

### 3.2 Get Attributes for Category

```
GET /admin/service-attributes/category/{categoryId}
```

Category defaults only (including `VENDOR` attributes). Service attributes come from 3.6.

### 3.3 Get All Attributes (Including Inactive)

```
GET /admin/service-attributes/category/{categoryId}/all
```

### 3.4 Get Filterable Attributes

```
GET /admin/service-attributes/category/{categoryId}/filterable
```

### 3.5 Get Required Attributes

```
GET /admin/service-attributes/category/{categoryId}/required
```

### 3.6 Get Attributes for a Service

```
GET /admin/service-attributes/service/{serviceId}
```

The service's newest schema version: the open draft if there is one, otherwise the published version. For a specific version use `GET /admin/services/{id}/schema-versions/{versionId}/attributes`.

### 3.7 Get by ID

```
GET /admin/service-attributes/{id}
```

### 3.8 Get by Code

```
GET /admin/service-attributes/category/{categoryId}/code/{code}
```

### 3.9 Create Attribute Definition

```
POST /admin/service-attributes
Content-Type: application/json
```

**Request Body:**
```json
{
  "serviceId": 5,
  "attributeCode": "tyre_brand",
  "attributeName": "Tyre brand",
  "attributeNameAr": "ماركة الإطار",
  "dataType": "SINGLE_SELECT",
  "inputContext": "PROVIDER",
  "unit": null,
  "isRequired": true,
  "isFilterable": true,
  "isSearchable": true,
  "isIndexed": false,
  "displayOrder": 10,
  "description": "Tyre manufacturer",
  "descriptionAr": null,
  "defaultValue": null,
  "validationRules": null,
  "options": [
    { "value": "michelin", "label": "Michelin", "labelAr": "ميشلان" },
    { "value": "mrf", "label": "MRF", "labelAr": null }
  ]
}
```

| Field             | Type    | Required | Description                                               |
|-------------------|---------|----------|-----------------------------------------------------------|
| serviceCategoryId | Integer | One of   | Category, for a category default                          |
| serviceId         | Integer | One of   | Catalog service; the attribute goes into its draft (category is taken from the service) |
| attributeCode     | String  | Yes      | 1–50 lowercase letters, digits or `_`, starting with a letter. Unique within the category defaults or the schema version |
| attributeName     | String  | Yes      | Display label                                             |
| attributeNameAr   | String  | No       | Arabic label                                              |
| dataType          | Enum    | Yes      | `STRING`, `NUMBER`, `INTEGER`, `DECIMAL`, `BOOLEAN`, `DATE`, `SINGLE_SELECT`, `MULTI_SELECT`, `JSON` |
| inputContext      | Enum    | No       | `BOTH` (default), `PROVIDER`, `CUSTOMER`, `VENDOR` (category defaults only) |
| options           | List    | Select types | `[{ value, label, labelAr }]`; required for `SINGLE_SELECT` / `MULTI_SELECT` |
| unit              | String  | No       | Unit of measure (e.g., "mm", "inch")                      |
| isRequired        | Boolean | No       | Must be filled in (by whoever `inputContext` names)       |
| isFilterable      | Boolean | No       | Customers can filter by this attribute                    |
| isSearchable      | Boolean | No       | Included in text search                                   |
| isIndexed         | Boolean | No       | Gets a database search index (MySQL). Capped by `catalog.max-indexed-attributes` (default 20) |
| displayOrder      | Integer | No       | Order in which to display                                 |
| description, descriptionAr | String | No | Help text                                             |
| defaultValue      | String  | No       | Default value                                             |
| validationRules   | String  | No       | JSON object as a string. Keys: `minimum` / `maximum` (or `min` / `max`), `minLength`, `maxLength`, `pattern`, `minItems`, `maxItems` |

**Response** (`ServiceAttributeDefinitionResponse`) adds `id`, `serviceCategoryName`, `serviceCode`, `schemaVersionId`, `schemaVersionNo`, `schemaVersionStatus`, `isActive`, `createdAt`, `updatedAt`. `options` comes back as a **JSON string**; parse it with `JSON.parse`.

### 3.10 Update Attribute

```
PUT /admin/service-attributes/{id}
Content-Type: application/json
```

Same body as create; only the fields you send change. Attributes in a published or retired schema version can't be updated, deactivated or reactivated. Open a draft (section 2.6) and change its copy.

### 3.11 Deactivate

```
DELETE /admin/service-attributes/{id}
```

### 3.12 Reactivate

```
POST /admin/service-attributes/{id}/reactivate
```

### Frontend Flow

```
Admin -> Select Category -> "Attributes" tab        (category defaults, incl. VENDOR)
Admin -> Select Service  -> "Form" tab              (schema versions, section 2.6)
  -> Table of attribute definitions
     Columns: Code | Name | Data Type | Input Context | Required | Filterable | Order
  -> "Add Attribute" button -> form (options editor for select types)
  -> Edit/Deactivate/Reactivate actions per row (draft or category defaults only)
  -> Drag-and-drop reorder (update displayOrder)
```

---

## 4. Service Filter Definitions

**Base Path:** `/admin/service-filters`

Filters define the UI filter controls shown to customers when browsing a category. They build on top of attributes but add UI-specific configuration. Link a filter to an attribute with `attributeDefinitionId`; the filter's code then follows the attribute's code.

### 4.1 Get All Active Filters

```
GET /admin/service-filters
```

### 4.2 Get Filters for Category

```
GET /admin/service-filters/category/{categoryId}
```

### 4.3 Get All Filters (Including Inactive)

```
GET /admin/service-filters/category/{categoryId}/all
```

### 4.4 Get Required Filters

```
GET /admin/service-filters/category/{categoryId}/required
```

### 4.5 Get Dependent Filters

```
GET /admin/service-filters/category/{categoryId}/dependent/{parentFilterCode}
```

### 4.6 Get by ID / Code

```
GET /admin/service-filters/{id}
GET /admin/service-filters/category/{categoryId}/code/{code}
```

### 4.7 Create Filter Definition

```
POST /admin/service-filters
Content-Type: application/json
```

**Request Body:**
```json
{
  "serviceCategoryId": 3,
  "attributeDefinitionId": 12,
  "filterCode": "tyre_brand",
  "filterLabel": "Brand",
  "filterLabelAr": "الماركة",
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
      "optionValue": "mrf",
      "optionLabel": "MRF",
      "optionLabelAr": null,
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

**Data Sources (`dataSource`):** `STATIC` (the options listed here), `DATABASE`, `API`, `LOCATION`

### 4.8 Update Filter

```
PUT /admin/service-filters/{id}
```

### 4.9 Deactivate / Reactivate

```
DELETE /admin/service-filters/{id}
POST  /admin/service-filters/{id}/reactivate
```

### 4.10 Manage Filter Options

```
POST   /admin/service-filters/{filterId}/options         -- Add option
GET    /admin/service-filters/{filterId}/options?parentValue=...  -- Get options
PUT    /admin/service-filters/options/{optionId}          -- Update option
DELETE /admin/service-filters/options/{optionId}          -- Deactivate option
```

**Filter Option Request Body:**
```json
{
  "optionValue": "mrf",
  "optionLabel": "MRF",
  "optionLabelAr": null,
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

## 5. Quotation Scope Configuration

**Base Path:** `/admin/quotation/scope`

Controls whether quotation is enabled for a category or a single service, and how it works. A scope saved with `serviceId` applies to that service and overrides the category's. The scope decides a service's quotation mode unless the service sets `quotationMode` itself (section 2.4).

### 5.1 Save Scope Definition (Create or Update)

```
POST /admin/quotation/scope
Content-Type: application/json
```

**Request Body:**
```json
{
  "serviceCategoryId": 5,
  "serviceId": null,
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
| serviceCategoryId         | Integer | Target category (taken from the service when `serviceId` is sent) |
| serviceId                 | Integer | Optional: scope for one catalog service                      |
| isQuotationEnabled        | Boolean | Enable/disable quotation (default `false`)                   |
| quotationType             | Enum    | `OPTIONAL` (default) / `MANDATORY` / `HYBRID`                |
| autoExpiryHours           | Integer | Hours until a request auto-expires (default 48; checked hourly) |
| allowMultiVendorBroadcast | Boolean | Whether to send request to multiple vendors (default `true`) |
| maxBroadcastVendors       | Integer | Max vendors to broadcast to                                  |
| minQuotesRequired         | Integer | Min quotes before customer should decide (default 1)         |

Saving again for the same category (or service) updates the existing scope. Fields you leave out are reset to their defaults, so send the full body.

**Quotation Types** and the service quotation mode they give:
- `OPTIONAL` - Vendor shows fixed price + customer can optionally request quote → `OPTIONAL`
- `MANDATORY` - No price shown, quote required → `REQUIRED`
- `HYBRID` - Base price shown, custom quote for additional work → `OPTIONAL`

With `isQuotationEnabled: false` the mode is `DIRECT`.

### 5.2 Get Scope for Category

```
GET /admin/quotation/scope/category/{categoryId}
```

The category's own scope only, not service scopes. A service scope can be read by its `id` (returned when it is saved) with 5.3.

### 5.3 Get Scope by ID

```
GET /admin/quotation/scope/{id}
```

### 5.4 Check if Quotation Enabled

```
GET /admin/quotation/scope/category/{categoryId}/enabled
```

**Response:** `ApiResponse<Boolean>`

### 5.5 Get All Quotation-Enabled Categories

```
GET /admin/quotation/scope/enabled
```

### 5.6 Disable Quotation for Category

```
POST /admin/quotation/scope/category/{categoryId}/disable
```

### 5.7 Delete Scope Definition

```
DELETE /admin/quotation/scope/{id}
```

Deactivates the scope.

### Frontend Flow

```
Admin -> Select Category -> "Quotation Settings" tab
  -> Toggle: Enable/Disable quotation
  -> If enabled:
     - Select quotation type (OPTIONAL/MANDATORY/HYBRID)
     - Optionally per service: same form with serviceId
     - Set auto-expiry hours
     - Toggle multi-vendor broadcast
     - Set max broadcast vendors
     - Set minimum quotes required
  -> Save button
```

---

## 6. Quotation Field Definitions

**Base Path:** `/admin/quotation/field`

Defines dynamic form fields shown to customers when they submit a quotation request. Fields saved with `serviceId` belong to that service; customers see the service's fields plus the category fields whose code the service doesn't redefine.

### 6.1 Create Field Definition

```
POST /admin/quotation/field
Content-Type: application/json
```

**Request Body:**
```json
{
  "serviceCategoryId": 1,
  "serviceId": null,
  "fieldCode": "vehicle_make",
  "label": "Vehicle Make",
  "labelAr": "نوع السيارة",
  "fieldType": "DROPDOWN",
  "isRequired": true,
  "displayOrder": 1,
  "placeholder": "Select make",
  "helpText": "Select your vehicle manufacturer",
  "placeholderAr": null,
  "helpTextAr": null,
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
| serviceId          | Integer       | Optional: field for one catalog service                    |
| fieldCode          | String        | Unique code within category                                |
| label              | String        | Display label                                              |
| labelAr, placeholderAr, helpTextAr | String | Arabic text                                    |
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

In responses, `options`, `validationRules` and `dependsOnCondition` come back as **JSON strings**; parse them with `JSON.parse`.

### 6.2 Update Field

```
PUT /admin/quotation/field/{fieldId}
```

### 6.3 Get Fields for Category

```
GET /admin/quotation/field/category/{categoryId}
```

Category fields only. To see what customers get for a service (service fields plus category fields), use `GET /quotations/customer/form/category/{categoryId}?serviceId={serviceId}`.

### 6.4 Get Required Fields

```
GET /admin/quotation/field/category/{categoryId}/required
```

### 6.5 Get Field by ID

```
GET /admin/quotation/field/{fieldId}
```

### 6.6 Delete Field

```
DELETE /admin/quotation/field/{fieldId}
```

Deactivates the field. Refused while other active fields depend on it.

### 6.7 Reorder Fields

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

## 7. Vendor Management (Admin View)

**Base Path:** `/vendors`

### 7.1 Get All Vendors

```
GET /vendors/all
```

**Response:** `List<VendorProfileResponse>` (not wrapped)

```json
[
  {
    "id": 1,
    "companyBrandName": "QuickFit Tyres",
    "companyArabicName": "كويك فيت",
    "name": "QuickFit Tyres LLC",
    "trnNumber": "TRN123456",
    "webSite": "https://quickfit.ae",
    "email": "info@quickfit.ae",
    "phoneNumber": "+971-4-1234567",
    "mobileNumber": "+971501234567",
    "accountStatus": "PENDING",
    "countryCode": "AE",
    "companyLogo": { "id": 10, "fileUrl": "https://..." },
    "companyLocation": { "address": "...", "city": "Dubai", "country": "AE", "latitude": 25.2, "longitude": 55.27 },
    "vatCertificate": [ ... ],
    "services": [ VendorServiceResponse, ... ]
  }
]
```

Hours, capacity, trade licence, branch address, photos and details (`attributes`) are per service, in `services[]`.

### 7.2 Get Vendor by ID

```
GET /vendors/{id}
```

### 7.3 Search Vendors

```
GET /vendors/search?city=Dubai&zip=12345&vendorName=Quick&serviceCategoryIds=1,2
```

| Query Param        | Type         | Description              |
|--------------------|--------------|--------------------------|
| city               | String       | Filter by city           |
| zip                | String       | Filter by zip code       |
| vendorName         | String       | Search by name           |
| serviceCategoryIds | List<Integer>| Filter by service types  |

### 7.4 Search by Brand Name

```
GET /vendors/brandname/{name}
```

### 7.5 Get Vendor Services by Type

```
GET /vendors/services/all?serviceType=CAR_WASH
GET /vendors/{vendorId}/services
```

Every active vendor service (branch) of a type (all types when `serviceType` is left out), or one vendor's services. Returns `List<VendorServiceResponse>`.

### 7.6 Update Vendor

```
PUT /vendors/{id}
Content-Type: application/json
```

**Request Body (`VendorCompanyRequest`):** company fields only; every field is optional.
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
  "countryCode": "AE",
  "companyLocation": {
    "streetAddress1": "Al Quoz Industrial Area",
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

A vendor service's hours, licence and details are edited with `PUT /vendors/services/{vendorServiceId}` (see the vendor guide).

### 7.7 Delete Vendor

```
DELETE /vendors/{id}
```

Deactivates the vendor.

### 7.8 Vendor Services (Admin)

**Base Path:** `/vendor-services`

Low-level access to vendor service rows and their default pricing. Responses are wrapped in `ApiResponse`. A row created here has no hours or slots; vendors add real services with `POST /vendors/{vendorId}/services`.

```
GET  /vendor-services                          -- Get all
GET  /vendor-services/{id}                     -- Get by ID
GET  /vendor-services/vendor/{vendorId}        -- Get by vendor
POST /vendor-services                          -- Create
PUT  /vendor-services/{id}                     -- Update
DELETE /vendor-services/{id}                   -- Deactivate
```

**Create Request:**
```json
{
  "vendorId": 1,
  "serviceCategoryId": 3,
  "isActive": true,
  "pricingType": "FIXED",
  "price": 120.00,
  "minPrice": null,
  "maxPrice": null,
  "currency": "AED",
  "description": null
}
```

`pricingType`: `FIXED`, `RANGE`, `QUOTE_BASED`, `NEGOTIABLE`. `serviceId` is accepted as an alias of `serviceCategoryId`.

---

## 8. Service Offering Administration

**Base Path:** `/service-offerings`

Admins can manage all vendor offerings.

### 8.1 Deactivate

```
DELETE /service-offerings/{id}
```

There is no hard delete; offerings are deactivated so bookings keep pointing at them.

### 8.2 Reactivate

```
POST /service-offerings/{id}/reactivate
```

### 8.3 Toggle Featured

```
PATCH /service-offerings/{id}/featured
```

### 8.4 View All Vendor Offerings (Including Inactive)

```
GET /service-offerings/vendor/{vendorId}/all
GET /service-offerings/vendor-service/{vendorServiceId}/all
```

Offerings return `serviceId`, `schemaVersionId` and `schemaVersionNo`: the catalog service and attribute form version they were created under.

### 8.5 Update Stock Status

```
PATCH /service-offerings/{id}/stock?status=OUT_OF_STOCK&quantity=0
```

**Stock Statuses:** `IN_STOCK`, `LIMITED_STOCK`, `OUT_OF_STOCK`, `PRE_ORDER`, `DISCONTINUED`. Stock status and quantity apply to `PRODUCT` offerings only; they're refused on `SERVICE` offerings.

---

## 9. Media File Management

**Base Path:** `/mediafiles`

### 9.1 Upload Files

```
POST /mediafiles/upload
Content-Type: multipart/form-data
```

**Form Data:**
- `files`: List of files (images up to 10 MB, videos up to 30 MB)

**Response:**
```json
{
  "status": "success",
  "message": "Files uploaded successfully",
  "data": [
    {
      "id": 45,
      "fileName": "8f2a11c3-logo.png",
      "originalFileName": "logo.png",
      "filePath": "...",
      "mediaType": "image/png",
      "size": 102400,
      "fileUrl": "https://...",
      "createdAt": "2026-10-27T10:00:00"
    }
  ]
}
```

### 9.2 Delete File

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
// Then use the id as imageId in ServiceCategoryRequest, logoId in VendorCompanyRequest, etc.
```

---

## 10. Booking Management (Admin View)

**Base Path:** `/bookings`

### 10.1 Get Appointments by Vendor/Service

```
GET /bookings/vendor/{vendorId}?status=PENDING
```

| Param          | Type    | Description                      |
|----------------|---------|----------------------------------|
| vendorId       | Integer | Vendor ID; covers all its services |
| status         | String  | Optional filter (enum Status)    |

### 10.2 Get Appointment Details

```
GET /bookings/{appointmentId}
```

### 10.3 Update Appointment Status

```
PUT /bookings/status
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

## 11. Recommended Admin Page Structure

```
/admin
  /dashboard                    -- Overview stats
  /categories                   -- Service Category CRUD
    /:id                        -- Category detail
      /services                 -- Catalog services
        /:serviceId             -- Service detail
          /markets              -- Countries + currency
          /form                 -- Schema versions + attributes
          /quotation            -- Mode override / service scope
      /attributes               -- Category default attributes (incl. VENDOR)
      /filters                  -- Filter definitions
      /quotation-settings       -- Quotation scope config
      /quotation-fields         -- Quotation form builder
  /vendors                      -- Vendor list & management
    /:id                        -- Vendor detail
      /services                 -- Vendor services
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
3. Define category defaults     POST /admin/service-attributes  { serviceCategoryId, ... } (repeat)
4. Define filters               POST /admin/service-filters (repeat)
5. Configure quotation scope    POST /admin/quotation/scope
6. Define quotation form fields POST /admin/quotation/field (repeat)
```

**2. Add a Catalog Service:**
```
1. Create the service           POST /admin/services
2. Add markets                  PUT  /admin/services/{id}/markets  (one per country)
3. Add form attributes          POST /admin/service-attributes  { serviceId, ... }  -> go into draft v1
4. Check the draft              GET  /admin/services/{id}/schema-versions
5. Publish                      POST /admin/services/{id}/schema-versions/{versionId}/publish
6. (Optional) quotation mode    PUT  /admin/services/{id}  { quotationMode }  or a service scope
```

**3. Change a Live Service's Form:**
```
1. Open a draft                 POST /admin/services/{id}/schema-versions   (copies the live version)
2. Edit the draft's attributes  POST / PUT / DELETE /admin/service-attributes...
3. Publish                      POST /admin/services/{id}/schema-versions/{versionId}/publish
```

**4. Manage Vendor:**
```
1. View all vendors             GET /vendors/all
2. View vendor detail           GET /vendors/{id}
3. View vendor services         GET /vendors/{id}/services
4. View vendor offerings        GET /service-offerings/vendor/{id}/all
5. Toggle featured offering     PATCH /service-offerings/{id}/featured
6. Deactivate bad offering      DELETE /service-offerings/{id}
```

**5. Handle Bookings:**
```
1. View bookings for vendor     GET /bookings/vendor/{id}
2. View booking details         GET /bookings/{id}
3. Update status                PUT /bookings/status
```

---

## 12. Axios API Reference

### 12.1 Axios Setup

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

### 12.2 Service Category APIs

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
// data = { code, name, nameAr, description, descriptionAr, displayOrder, imageId, parentId }

// Update category
export const updateCategory = (id, data) =>
  api.put(`${SERVICE_CATEGORY_BASE}/${id}`, data);
// data = same as create

// Deactivate category
export const deleteCategory = (id) =>
  api.delete(`${SERVICE_CATEGORY_BASE}/${id}`);
```

---

### 12.3 Catalog Service APIs

```javascript
// api/admin/catalogServiceApi.js
import api from '../axiosInstance';

const SERVICE_BASE = '/admin/services';

export const listServices = ({ categoryId, activeOnly = false } = {}) =>
  api.get(SERVICE_BASE, { params: { categoryId, activeOnly } });
export const getService = (id) => api.get(`${SERVICE_BASE}/${id}`);
export const getServiceByCode = (code) => api.get(`${SERVICE_BASE}/code/${code}`);
export const createService = (data) => api.post(SERVICE_BASE, data);
// data = { serviceCategoryId, code, name, nameAr, description, descriptionAr,
//          quotationMode, fulfillmentMode, estimatedDurationMinutes, displayOrder }
export const updateService = (id, data) => api.put(`${SERVICE_BASE}/${id}`, data);
export const deactivateService = (id) => api.post(`${SERVICE_BASE}/${id}/deactivate`);
export const reactivateService = (id) => api.post(`${SERVICE_BASE}/${id}/reactivate`);
export const resetQuotationMode = (id) => api.post(`${SERVICE_BASE}/${id}/quotation-mode/reset`);

// Markets
export const getMarkets = (id) => api.get(`${SERVICE_BASE}/${id}/markets`);
export const saveMarket = (id, data) => api.put(`${SERVICE_BASE}/${id}/markets`, data);
// data = { countryCode, currencyCode, isAvailable, regulatoryConfig, regulatoryNote, regulatoryNoteAr }

// Schema versions
export const getSchemaVersions = (id) => api.get(`${SERVICE_BASE}/${id}/schema-versions`);
export const openDraft = (id, notes) =>
  api.post(`${SERVICE_BASE}/${id}/schema-versions`, null, { params: { notes } });
export const getVersionAttributes = (id, versionId) =>
  api.get(`${SERVICE_BASE}/${id}/schema-versions/${versionId}/attributes`);
export const publishVersion = (id, versionId) =>
  api.post(`${SERVICE_BASE}/${id}/schema-versions/${versionId}/publish`);
```

---

### 12.4 Service Attribute Definition APIs

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

// Get a service's attributes (open draft, else published version)
export const getAttributesByService = (serviceId) =>
  api.get(`${ATTR_BASE}/service/${serviceId}`);

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
//   serviceCategoryId or serviceId, attributeCode, attributeName, attributeNameAr,
//   dataType, inputContext, options: [{ value, label, labelAr }],
//   unit, isRequired, isFilterable, isSearchable, isIndexed, displayOrder,
//   description, descriptionAr, defaultValue, validationRules
// }

// Update attribute definition
export const updateAttribute = (id, data) =>
  api.put(`${ATTR_BASE}/${id}`, data);

// Deactivate attribute
export const deleteAttribute = (id) =>
  api.delete(`${ATTR_BASE}/${id}`);

// Reactivate soft-deleted attribute
export const reactivateAttribute = (id) =>
  api.post(`${ATTR_BASE}/${id}/reactivate`);
```

---

### 12.5 Service Filter Definition APIs

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
//   serviceCategoryId, attributeDefinitionId, filterCode, filterLabel, filterLabelAr, filterType,
//   dataSource, dataSourceConfig, dependsOn, displayOrder,
//   isRequired, allowMultiple, placeholder, helpText,
//   minValue, maxValue, stepValue, options: [{ optionValue, optionLabel, optionLabelAr, ... }]
// }

// Update filter definition
export const updateFilter = (id, data) =>
  api.put(`${FILTER_BASE}/${id}`, data);

// Deactivate filter
export const deleteFilter = (id) =>
  api.delete(`${FILTER_BASE}/${id}`);

// Reactivate soft-deleted filter
export const reactivateFilter = (id) =>
  api.post(`${FILTER_BASE}/${id}/reactivate`);

// --- Filter Options ---

// Add option to a filter
export const addFilterOption = (filterId, data) =>
  api.post(`${FILTER_BASE}/${filterId}/options`, data);
// data = { optionValue, optionLabel, optionLabelAr, displayOrder, isDefault, isActive, iconUrl, parentValue, metadata }

// Get options for a filter
export const getFilterOptions = (filterId, parentValue = null) =>
  api.get(`${FILTER_BASE}/${filterId}/options`, {
    params: { parentValue },
  });

// Update filter option
export const updateFilterOption = (optionId, data) =>
  api.put(`${FILTER_BASE}/options/${optionId}`, data);

// Deactivate filter option
export const deleteFilterOption = (optionId) =>
  api.delete(`${FILTER_BASE}/options/${optionId}`);
```

---

### 12.6 Quotation Scope APIs

```javascript
// api/admin/quotationScopeApi.js
import api from '../axiosInstance';

const SCOPE_BASE = '/admin/quotation/scope';

// Create or update quotation scope
export const saveScopeDefinition = (data) =>
  api.post(SCOPE_BASE, data);
// data = {
//   serviceCategoryId, serviceId (optional), isQuotationEnabled, quotationType,
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

// Deactivate scope definition
export const deleteScopeDefinition = (id) =>
  api.delete(`${SCOPE_BASE}/${id}`);
```

---

### 12.7 Quotation Field Definition APIs

```javascript
// api/admin/quotationFieldApi.js
import api from '../axiosInstance';

const FIELD_BASE = '/admin/quotation/field';

// Create field definition
export const createField = (data) =>
  api.post(FIELD_BASE, data);
// data = {
//   serviceCategoryId, serviceId (optional), fieldCode, label, labelAr, fieldType,
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

// Deactivate field definition
export const deleteField = (fieldId) =>
  api.delete(`${FIELD_BASE}/${fieldId}`);

// Reorder fields within a category
export const reorderFields = (categoryId, fieldIds) =>
  api.post(`${FIELD_BASE}/category/${categoryId}/reorder`, fieldIds);
// fieldIds = [3, 1, 5, 2, 4]  -- array of field IDs in desired order
```

---

### 12.8 Vendor Management APIs (Admin View)

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

// Vendor services (branches) of one type, or all types
export const getVendorServicesByType = (serviceType) =>
  api.get(`${VENDOR_BASE}/services/all`, { params: { serviceType } });

// One vendor's services
export const getVendorServices = (vendorId) =>
  api.get(`${VENDOR_BASE}/${vendorId}/services`);

// Update vendor (company fields)
export const updateVendor = (id, data) =>
  api.put(`${VENDOR_BASE}/${id}`, data);
// data = VendorCompanyRequest (see Section 7.6)

// Deactivate vendor
export const deleteVendor = (id) =>
  api.delete(`${VENDOR_BASE}/${id}`);
```

---

### 12.9 Vendor Service APIs

```javascript
// api/admin/vendorServiceApi.js
import api from '../axiosInstance';

const VENDOR_SERVICE_BASE = '/vendor-services';

// Get all vendor services
export const getAllVendorServices = () =>
  api.get(VENDOR_SERVICE_BASE);

// Get vendor service by ID
export const getVendorServiceById = (id) =>
  api.get(`${VENDOR_SERVICE_BASE}/${id}`);

// Get vendor services by vendor
export const getVendorServicesByVendor = (vendorId) =>
  api.get(`${VENDOR_SERVICE_BASE}/vendor/${vendorId}`);

// Add a category to a vendor
export const createVendorService = (data) =>
  api.post(VENDOR_SERVICE_BASE, data);
// data = { vendorId, serviceCategoryId, isActive, pricingType, price, minPrice, maxPrice, currency, description }

// Update vendor service
export const updateVendorService = (id, data) =>
  api.put(`${VENDOR_SERVICE_BASE}/${id}`, data);

// Deactivate vendor service
export const deactivateVendorService = (id) =>
  api.delete(`${VENDOR_SERVICE_BASE}/${id}`);
```

---

### 12.10 Service Offering Admin APIs

```javascript
// api/admin/serviceOfferingAdminApi.js
import api from '../axiosInstance';

const OFFERING_BASE = '/service-offerings';

// Get all offerings for a vendor (including inactive)
export const getAllVendorOfferings = (vendorId) =>
  api.get(`${OFFERING_BASE}/vendor/${vendorId}/all`);

// Deactivate offering (there is no hard delete)
export const deactivateOffering = (id) =>
  api.delete(`${OFFERING_BASE}/${id}`);

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

### 12.11 Media File APIs

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

### 12.12 Booking Admin APIs

```javascript
// api/admin/bookingAdminApi.js
import api from '../axiosInstance';

const BOOKING_BASE = '/bookings';

// Get appointments by vendor/service
export const getAppointmentsByVendor = (vendorId, status = null) =>
  api.get(`${BOOKING_BASE}/vendor/${vendorId}`, {
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

### 12.13 Usage Example (React Component)

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
        code: formValues.code,
        name: formValues.name,
        nameAr: formValues.nameAr,
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
    if (!window.confirm('Deactivate this category?')) return;
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
