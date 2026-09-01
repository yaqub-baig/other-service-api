# Plan v3: API Response DTO Conversion Checklist

This document outlines the tasks required to refactor all identified API endpoints to return dedicated Response Data Transfer Objects (DTOs) instead of raw JPA entities. This is a critical step to improve API stability, security, maintainability, and flexibility.

Each item in the checklist below represents a controller or a specific endpoint that needs to be updated. The task for each item typically involves:
1.  Creating a new Response DTO (or updating an existing one) for the entity being returned.
2.  Creating a new MapStruct/Manual Mapper (or updating an existing one) to convert the JPA entity to the DTO.
3.  Updating the corresponding Service layer methods to return the new DTO.
4.  Updating the Controller layer endpoints to return `ResponseEntity` containing the new DTO.

---

## 1. BookingController

-   [x] **`POST /os-appointments`**: Endpoint `createCustomerAppointment` returns `Booking` entity wrapped in `OsGenericResponse`.
-   [x] **`PUT /os-appointments/status`**: Endpoint `updateAppointmentStatus` returns `Booking` entity wrapped in `OsGenericResponse`.
-   [x] **`PUT /os-appointments/edit/appointment`**: Endpoint `editAppointment` directly returns `ResponseEntity<Booking>`.

## 2. MediaFileController

-   [x] **`POST /mediafiles/upload`**: Endpoint `upload` directly returns `List<MediaFile>`.

## 3. OsJobCardController

-   [x] **`PUT /os-jobcards/status`**: Endpoint `updateJobCardStatus` returns `OsJobCardResponse` wrapped in `ApiResponse`.
-   [x] **`GET /os-jobcards/technician/{technicianId}`**: Endpoint `getTechnicianJobCards` returns `List<OsJobCardResponse>` wrapped in `ApiResponse`.
-   [x] **`GET /os-jobcards/vendor/{vendorId}`**: Endpoint `getVendorJobCards` returns `List<OsJobCardResponse>` wrapped in `ApiResponse`.

## 4. QuotationAdminController

-   [x] **`POST /admin/quotation/scope`**: Endpoint `saveScopeDefinition` returns `QuotationScopeDefinition` entity wrapped in `OsGenericResponse`.
-   [x] **`GET /admin/quotation/scope/category/{categoryId}`**: Endpoint `getScopeForCategory` returns `QuotationScopeDefinition` entity wrapped in `OsGenericResponse`.
-   [x] **`GET /admin/quotation/scope/{id}`**: Endpoint `getScopeById` returns `QuotationScopeDefinition` entity wrapped in `OsGenericResponse`.
-   [x] **`GET /admin/quotation/scope/enabled`**: Endpoint `getQuotationEnabledCategories` returns `List<QuotationScopeDefinition>` wrapped in `OsGenericResponse`.
-   [x] **`POST /admin/quotation/field`**: Endpoint `createFieldDefinition` returns `QuotationFieldDefinition` entity wrapped in `OsGenericResponse`.
-   [x] **`PUT /admin/quotation/field/{fieldId}`**: Endpoint `updateFieldDefinition` returns `QuotationFieldDefinition` entity wrapped in `OsGenericResponse`.
-   [x] **`GET /admin/quotation/field/category/{categoryId}`**: Endpoint `getFieldsForCategory` returns `List<QuotationFieldDefinition>` wrapped in `OsGenericResponse`.
-   [x] **`GET /admin/quotation/field/category/{categoryId}/required`**: Endpoint `getRequiredFieldsForCategory` returns `List<QuotationFieldDefinition>` wrapped in `OsGenericResponse`.
-   [x] **`GET /admin/quotation/field/{fieldId}`**: Endpoint `getFieldById` returns `QuotationFieldDefinition` entity wrapped in `OsGenericResponse`.

## 5. QuotationCustomerController

-   [x] **`POST /quotations/customer/request`**: Endpoint `createRequest` returns `QuotationRequest` entity wrapped in `OsGenericResponse`.
-   [x] **`GET /quotations/customer/requests`**: Endpoint `getMyRequests` returns `List<QuotationRequest>` wrapped in `OsGenericResponse`.
-   [x] **`GET /quotations/customer/requests/page`**: Endpoint `getMyRequestsPaginated` returns `Page<QuotationRequest>` wrapped in `OsGenericResponse`.
-   [x] **`GET /quotations/customer/request/{requestId}`**: Endpoint `getRequestById` returns `QuotationRequest` entity wrapped in `OsGenericResponse`.
-   [x] **`GET /quotations/customer/request/number/{quotationNumber}`**: Endpoint `getRequestByNumber` returns `QuotationRequest` entity wrapped in `OsGenericResponse`.
-   [x] **`GET /quotations/customer/request/{requestId}/fields`**: Endpoint `getRequestFieldValues` returns `List<QuotationRequestFieldValue>` wrapped in `OsGenericResponse`.
-   [x] **`POST /quotations/customer/request/{requestId}/cancel`**: Endpoint `cancelRequest` returns `QuotationRequest` entity wrapped in `OsGenericResponse`.
-   [x] **`GET /quotations/customer/request/{requestId}/quotes`**: Endpoint `getQuotesForRequest` returns `List<VendorQuotation>` wrapped in `OsGenericResponse`.
-   [x] **`GET /quotations/customer/quote/{quotationId}`**: Endpoint `getQuoteDetails` returns `VendorQuotation` entity wrapped in `OsGenericResponse`.
-   [x] **`GET /quotations/customer/quote/{quotationId}/items`**: Endpoint `getQuoteLineItems` returns `List<VendorQuotationLineItem>` wrapped in `OsGenericResponse`.
-   [x] **`POST /quotations/customer/quote/{quotationId}/accept`**: Endpoint `acceptQuote` returns `VendorQuotation` entity wrapped in `OsGenericResponse`.
-   [x] **`POST /quotations/customer/quote/{quotationId}/reject`**: Endpoint `rejectQuote` returns `VendorQuotation` entity wrapped in `OsGenericResponse`.
-   [x] **`GET /quotations/customer/form/category/{categoryId}`**: Endpoint `getQuotationFormFields` returns `List<QuotationFieldDefinition>` wrapped in `OsGenericResponse`.
-   [x] **`POST /quotations/customer/quote/{quotationId}/book`**: Endpoint `createBookingFromQuote` returns `Booking` entity wrapped in `OsGenericResponse`.

## 6. QuotationVendorController

-   [x] **`GET /quotations/vendor/requests/open`**: Endpoint `findOpenRequests` returns `List<QuotationRequest>` wrapped in `OsGenericResponse`.
-   [x] **`GET /quotations/vendor/request/{requestId}`**: Endpoint `getRequestDetails` returns `QuotationRequest` entity wrapped in `OsGenericResponse`.
-   [x] **`GET /quotations/vendor/request/{requestId}/fields`**: Endpoint `getRequestFieldValues` returns `List<QuotationRequestFieldValue>` wrapped in `OsGenericResponse`.
-   [x] **`POST /quotations/vendor/quote`**: Endpoint `submitQuote` returns `VendorQuotation` entity wrapped in `OsGenericResponse`.
-   [x] **`GET /quotations/vendor/quotes`**: Endpoint `getMyQuotations` returns `List<VendorQuotation>` wrapped in `OsGenericResponse`.
-   [x] **`GET /quotations/vendor/quotes/page`**: Endpoint `getMyQuotationsPaginated` returns `Page<VendorQuotation>` wrapped in `OsGenericResponse`.
-   [x] **`GET /quotations/vendor/quote/{quotationId}`**: Endpoint `getQuotationById` returns `VendorQuotation` entity wrapped in `OsGenericResponse`.
-   [x] **`GET /quotations/vendor/quote/{quotationId}/items`**: Endpoint `getQuotationLineItems` returns `List<VendorQuotationLineItem>` wrapped in `OsGenericResponse`.
-   [x] **`POST /quotations/vendor/quote/{quotationId}/withdraw`**: Endpoint `withdrawQuote` returns `VendorQuotation` entity wrapped in `OsGenericResponse`.

## 7. ServiceAttributeDefinitionController

-   [x] **`POST /admin/service-attributes`**: Endpoint `create` returns `ServiceAttributeDefinition` entity wrapped in `OsGenericResponse`.
-   [x] **`GET /admin/service-attributes/category/{categoryId}`**: Endpoint `getByCategory` returns `List<ServiceAttributeDefinition>`.
-   [x] **`GET /admin/service-attributes/category/{categoryId}/all`**: Endpoint `getAllByCategory` returns `List<ServiceAttributeDefinition>`.
-   [x] **`GET /admin/service-attributes/category/{categoryId}/filterable`**: Endpoint `getFilterableByCategory` returns `List<ServiceAttributeDefinition>`.
-   [x] **`GET /admin/service-attributes/category/{categoryId}/required`**: Endpoint `getRequiredByCategory` returns `List<ServiceAttributeDefinition>`.
-   [x] **`GET /admin/service-attributes/{id}`**: Endpoint `getById` returns `ServiceAttributeDefinition`.
-   [x] **`GET /admin/service-attributes/category/{categoryId}/code/{code}`**: Endpoint `getByCode` returns `ServiceAttributeDefinition`.
-   [x] **`PUT /admin/service-attributes/{id}`**: Endpoint `update` returns `ServiceAttributeDefinition` entity wrapped in `OsGenericResponse`.
-   [x] **`POST /admin/service-attributes/{id}/reactivate`**: Endpoint `reactivate` returns `ServiceAttributeDefinition` entity wrapped in `OsGenericResponse`.
-   [x] **`GET /admin/service-attributes`**: Endpoint `getAll` returns `List<ServiceAttributeDefinition>`.

## 8. ServiceCategoryController

-   [x] **`GET /service-categories`**: Endpoint `getAll` returns `List<ServiceCategory>`.
-   [x] **`POST /service-categories`**: Endpoint `create` returns `ServiceCategory` entity wrapped in `OsGenericResponse`.
-   [x] **`GET /service-categories/{id}`**: Endpoint `getById` returns `ServiceCategory`.
-   [x] **`PUT /service-categories/{id}`**: Endpoint `update` returns `ServiceCategory` entity wrapped in `OsGenericResponse`.
-   [x] **`GET /service-categories/parent/{parentId}`**: Endpoint `getByParentId` returns `List<ServiceCategory>`.
-   [x] **`GET /service-categories/root`**: Endpoint `getRootCategories` returns `List<ServiceCategory>`.

## 9. ServiceFilterDefinitionController

-   [x] **`POST /admin/service-filters`**: Endpoint `create` returns `ServiceFilterDefinitionResponse` wrapped in `ApiResponse`.
-   [x] **`GET /admin/service-filters/category/{categoryId}`**: Endpoint `getByCategory` returns `List<ServiceFilterDefinitionResponse>` wrapped in `ApiResponse`.
-   [x] **`GET /admin/service-filters/category/{categoryId}/all`**: Endpoint `getAllByCategory` returns `List<ServiceFilterDefinitionResponse>` wrapped in `ApiResponse`.
-   [x] **`GET /admin/service-filters/category/{categoryId}/required`**: Endpoint `getRequiredByCategory` returns `List<ServiceFilterDefinitionResponse>` wrapped in `ApiResponse`.
-   [x] **`GET /admin/service-filters/category/{categoryId}/dependent/{parentFilterCode}`**: Endpoint `getDependentFilters` returns `List<ServiceFilterDefinitionResponse>` wrapped in `ApiResponse`.
-   [x] **`GET /admin/service-filters/{id}`**: Endpoint `getById` returns `ServiceFilterDefinitionResponse` wrapped in `ApiResponse`.
-   [x] **`GET /admin/service-filters/category/{categoryId}/code/{code}`**: Endpoint `getByCode` returns `ServiceFilterDefinitionResponse` wrapped in `ApiResponse`.
-   [x] **`PUT /admin/service-filters/{id}`**: Endpoint `update` returns `ServiceFilterDefinitionResponse` wrapped in `ApiResponse`.
-   [x] **`POST /admin/service-filters/{filterId}/options`**: Endpoint `addOption` returns `ServiceFilterDefinitionResponse` wrapped in `ApiResponse`.
-   [x] **`GET /admin/service-filters/{filterId}/options`**: Endpoint `getOptions` returns `List<FilterOptionResponse>` wrapped in `ApiResponse`.
-   [x] **`PUT /admin/service-filters/options/{optionId}`**: Endpoint `updateOption` returns `FilterOptionResponse` wrapped in `ApiResponse`.

## 10. ServiceOfferingController

-   [x] **`POST /service-offerings`**: Endpoint `create` returns `ServiceOfferingResponse` wrapped in `ApiResponse`.
-   [x] **`GET /service-offerings/{id}`**: Endpoint `getById` returns `ServiceOfferingResponse` wrapped in `ApiResponse`.
-   [x] **`GET /service-offerings/sku/{sku}`**: Endpoint `getBySku` returns `ServiceOfferingResponse` wrapped in `ApiResponse`.
-   [x] **`PUT /service-offerings/{id}`**: Endpoint `update` returns `ServiceOfferingResponse` wrapped in `ApiResponse`.
-   [x] **`POST /service-offerings/{id}/reactivate`**: Endpoint `reactivate` returns `ServiceOfferingResponse` wrapped in `ApiResponse`.
-   [x] **`GET /service-offerings/vendor-service/{vendorServiceId}`**: Endpoint `getByVendorService` returns `List<ServiceOfferingResponse>` wrapped in `ApiResponse`.
-   [x] **`GET /service-offerings/vendor-service/{vendorServiceId}/all`**: Endpoint `getAllByVendorService` returns `List<ServiceOfferingResponse>` wrapped in `ApiResponse`.
-   [x] **`GET /service-offerings/vendor/{vendorId}`**: Endpoint `getByVendor` returns `List<ServiceOfferingResponse>` wrapped in `ApiResponse`.
-   [x] **`GET /service-offerings/vendor/{vendorId}/all`**: Endpoint `getAllByVendor` returns `List<ServiceOfferingResponse>` wrapped in `ApiResponse`.
-   [x] **`GET /service-offerings/vendor/{vendorId}/category/{categoryId}`**: Endpoint `getByVendorAndCategory` returns `List<ServiceOfferingResponse>` wrapped in `ApiResponse`.
-   [x] **`GET /service-offerings/category/{categoryId}`**: Endpoint `getByCategory` returns `List<ServiceOfferingResponse>` wrapped in `ApiResponse`.
-   [x] **`GET /service-offerings/category/{categoryId}/page`**: Endpoint `getByCategoryPaginated` returns `Page<ServiceOfferingResponse>` wrapped in `ApiResponse`.
-   [x] **`GET /service-offerings/search`**: Endpoint `searchByName` returns `List<ServiceOfferingResponse>` wrapped in `ApiResponse`.
-   [x] **`GET /service-offerings/category/{categoryId}/search`**: Endpoint `searchByNameInCategory` returns `List<ServiceOfferingResponse>` wrapped in `ApiResponse`.
-   [x] **`GET /service-offerings/category/{categoryId}/price-range`**: Endpoint `getByPriceRange` returns `List<ServiceOfferingResponse>` wrapped in `ApiResponse`.
-   [x] **`GET /service-offerings/featured`**: Endpoint `getFeatured` returns `List<ServiceOfferingResponse>` wrapped in `ApiResponse`.
-   [x] **`GET /service-offerings/category/{categoryId}/featured`**: Endpoint `getFeaturedByCategory` returns `List<ServiceOfferingResponse>` wrapped in `ApiResponse`.
-   [x] **`GET /service-offerings/top-rated`**: Endpoint `getTopRated` returns `List<ServiceOfferingResponse>` wrapped in `ApiResponse`.
-   [x] **`GET /service-offerings/popular`**: Endpoint `getMostPopular` returns `List<ServiceOfferingResponse>` wrapped in `ApiResponse`.
-   [x] **`PATCH /service-offerings/{id}/stock`**: Endpoint `updateStockStatus` returns `ServiceOfferingResponse` wrapped in `ApiResponse`.
-   [x] **`PATCH /service-offerings/{id}/featured`**: Endpoint `toggleFeatured` returns `ServiceOfferingResponse` wrapped in `ApiResponse`.
-   [x] **`GET /service-offerings/filter`**: Endpoint `filterByAttribute` returns `List<ServiceOfferingResponse>` wrapped in `ApiResponse`.
-   [x] **`POST /service-offerings/filter`**: Endpoint `filterByAttributes` returns `List<ServiceOfferingResponse>` wrapped in `ApiResponse`.
-   [x] **`GET /service-offerings/category/{categoryId}/attributes`**: Endpoint `getAttributeDefinitions` returns `List<ServiceAttributeDefinitionResponse>` wrapped in `ApiResponse`.
-   [x] **`GET /service-offerings/category/{categoryId}/attributes/required`**: Endpoint `getRequiredAttributeDefinitions` returns `List<ServiceAttributeDefinitionResponse>` wrapped in `ApiResponse`.

## 11. VendorServiceBridgeController

-   [x] **`POST /vendor-service-bridges`**: Endpoint `create` returns `VendorServiceBridgeResponse` wrapped in `ApiResponse`.
-   [x] **`GET /vendor-service-bridges/{id}`**: Endpoint `getById` returns `VendorServiceBridgeResponse` wrapped in `ApiResponse`.
-   [x] **`PUT /vendor-service-bridges/{id}`**: Endpoint `update` returns `VendorServiceBridgeResponse` wrapped in `ApiResponse`.
-   [x] **`GET /vendor-service-bridges`**: Endpoint `getAll` returns `List<VendorServiceBridgeResponse>` wrapped in `ApiResponse`.
-   [x] **`GET /vendor-service-bridges/vendor/{vendorId}`**: Endpoint `getByVendor` returns `List<VendorServiceBridgeResponse>` wrapped in `ApiResponse`.
