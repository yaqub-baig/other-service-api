# Service catalog: API changes for frontends

Branch `feature/service-catalog-evolution`. Everything below is additive unless marked **changed** or **removed**. Existing category-only calls keep working.

## Error responses

Errors still return HTTP 201 with the real status in `code` (existing convention).

- Attribute and form validation failures now return `code: "400"` with every failing field in `body`:

```json
{ "status": "failed", "code": "400", "message": "Invalid attribute values: 2 field(s) failed validation",
  "body": [ { "field": "oil_type", "message": "'diesel' is not an allowed value for Oil type" },
            { "field": "colour",   "message": "Unknown attribute 'colour'" } ] }
```

- Business rule errors (`BusinessException`) now return `code: "400"` with the message. They used to return `500`.

## Languages

Send `Accept-Language: ar` to get Arabic in the display fields (`name`, `description`, `label`, `attributeName`, `filterLabel`, `optionLabel`, `placeholder`, `helpText`). English is used when no Arabic text exists. The raw Arabic is also returned in `nameAr`, `labelAr`, and so on, for admin forms. Attribute option labels are in the `options` JSON as `label` / `labelAr`.

## Catalog services (new)

A service sits between a category and an offering, for example Tires & Wheels → Tire Replacement.

| Method | Path | Notes |
| --- | --- | --- |
| GET | `/catalog/services?categoryId=&country=AE` | Customer list. With `country`, only services available there |
| GET | `/catalog/services/{id}` | Includes `effectiveQuotationMode` |
| GET | `/catalog/services/{id}/attributes` | Live form: published schema plus category defaults |
| POST | `/admin/services` | `serviceCategoryId`, `code`, `name`, `nameAr`, `fulfillmentMode` (WORKSHOP / MOBILE / BOTH), `estimatedDurationMinutes`, `quotationMode` |
| GET / PUT | `/admin/services`, `/admin/services/{id}`, `/admin/services/code/{code}` | |
| POST | `/admin/services/{id}/deactivate`, `/reactivate` | There is no delete |
| POST | `/admin/services/{id}/quotation-mode/reset` | Go back to following the category |

## Markets and currency (new)

| Method | Path | Body |
| --- | --- | --- |
| GET | `/admin/services/{id}/markets` | |
| PUT | `/admin/services/{id}/markets` | `countryCode`, `currencyCode`, `isAvailable`, `regulatoryConfig` (object), `regulatoryNote`, `regulatoryNoteAr` |

- Vendors have a `countryCode` (onboarding request and responses). It defaults to `AE` when not sent.
- An offering linked to a service is priced in the currency of the vendor's market. If you omit `currency`, the market's currency is used. A different currency is refused, and so is a service that isn't available in that country.
- Offering search accepts `countryCode` and `serviceId` (POST body), or `country` and `serviceId` (GET query).

## Schema versions (new)

A service's attributes are versioned. Only the **published** version is live.

| Method | Path | Notes |
| --- | --- | --- |
| GET | `/admin/services/{id}/schema-versions` | Newest first |
| POST | `/admin/services/{id}/schema-versions?notes=` | Opens a draft, copied from the published version |
| GET | `/admin/services/{id}/schema-versions/{versionId}/attributes` | |
| POST | `/admin/services/{id}/schema-versions/{versionId}/publish` | Retires the previous version |

- `POST /admin/service-attributes` with `serviceId` adds the attribute to the service's draft. A draft is opened if none exists.
- Attributes in a published or retired version can't be edited, deactivated or reactivated. Open a draft instead.
- Offerings, quotation requests and bookings return the `schemaVersionId` they were created under.

## Attributes (**changed**)

New request fields: `serviceId`, `options` (`[{ "value", "label", "labelAr" }]`), `inputContext` (CUSTOMER / PROVIDER / BOTH, default BOTH), `isIndexed`, `attributeNameAr`, `descriptionAr`.

- New `dataType` values: `INTEGER`, `DECIMAL`, `SINGLE_SELECT`, `MULTI_SELECT`. Select types must have options.
- Codes must be 1–50 lowercase letters, digits or underscores, starting with a letter.
- `validationRules` must be a JSON object. Supported keys: `minimum` / `maximum` (or `min` / `max`), `minLength`, `maxLength`, `pattern`, `minItems`, `maxItems`.
- **Changed:** `GET /admin/service-attributes/category/{id}` returns category defaults only. Service attributes come from `/admin/service-attributes/service/{serviceId}`.
- **Removed:** `DELETE /admin/service-attributes/{id}/hard`.

## Offerings (**changed**)

- New request fields: `serviceId`, `offeringType` (SERVICE default, or PRODUCT).
- Attributes are always validated, including when none are sent, so required attributes can't be skipped. Unknown codes are refused, where they used to be stored with a warning. CUSTOMER-only attributes are never required on an offering.
- Stock status and quantity are refused on SERVICE offerings.
- `GET /service-offerings/category/{id}/attributes` accepts `?serviceId=`.
- **Removed:** `DELETE /service-offerings/{id}/hard`.

## Quotations (**changed**)

- Scope, field and request bodies accept `serviceId`. A service's own scope and fields override the category's.
- `GET /quotations/customer/form/category/{id}` and `/check/category/{id}` accept `?serviceId=`.
- A request for a service whose effective mode is DIRECT is refused.
- `quotation_scope_definitions` is no longer unique per category, because services can have their own scope.

## Bookings (**changed**)

- **Changed:** the base path is now `/bookings` instead of `/os-appointments`, and the "other service" naming is gone:

| Before | Now |
| --- | --- |
| `GET /os-appointments/other-service/{otherServiceId}` | `GET /bookings/vendor/{vendorId}` |
| `GET /os-appointments/{otherServiceId}/available-slots` | `GET /bookings/{vendorServiceId}/available-slots` |
| `POST /os-appointments/{serviceId}/book?selectedOtherServiceIds=` | `POST /bookings/{vendorServiceId}/book?selectedVendorServiceIds=` |
| `PUT /os-appointments/edit/appointment?appointmentId=&otherServiceId=` | `PUT /bookings/edit?bookingId=&serviceId=` |
| `POST /os-appointments` body field `otherServiceId` (vendor) | `POST /bookings` body field `vendorServiceId` |

  Slots and bookings belong to a vendor service (one service or branch of a vendor), so these paths take a vendor service id.
  The other booking paths (`/status`, `/{appointmentId}`, `/{appointmentId}/cancel`, `/user/bookings`) only change the base.
- **Changed:** booking responses return `selectedVendorServiceIds` instead of `selectedOtherServiceIds`, and the available-slots response returns `vendorServiceId` instead of `garageId`.
- **Changed:** booking responses return `subscriptionPlanId` instead of `osSubscriptionPlanId`.
- **Changed (job cards):** the assign request takes `vendorId` instead of `otherServiceId`. Job card responses return `vendorId`, which is now filled in; `otherServiceId` was always `null`.
- `POST /bookings` accepts `catalogServiceId`. `POST /bookings/{vendorServiceId}/book` accepts `?catalogServiceId=`.
- A service whose effective mode is REQUIRED can't be booked directly; book from the accepted quote instead. The vendor must offer the service's category.

## Filters, categories and vendor services (**changed**)

- Filters accept `attributeDefinitionId`. The filter code then follows the attribute's code.
- **Removed:** `DELETE /admin/service-filters/{id}/hard`.
- `DELETE /admin/service-filters/options/{id}` and `DELETE /service-categories/{id}` now deactivate instead of deleting.
- Categories accept and return `code`, `nameAr`, `descriptionAr`, `displayOrder`, `isActive` and `parentId`.
- **Changed:** `/vendor-service-bridges` is now `/vendor-services`. The body uses `vendorId` and `serviceCategoryId` instead of `garageId` and `serviceId` (`serviceId` is still accepted). `PUT` takes the same body as `POST` (plus optional `pricingType`, `price`, `minPrice`, `maxPrice`, `currency`, `description`), and `DELETE` deactivates.

## Starter data

On first start the service loads 10 categories and 14 services, each with a published v1 schema (39 attributes, 64 options), from `src/main/resources/seed/catalog-seed.json`. Markets are created only for the countries in `catalog.seed.markets` (default `AE`). Set `catalog.seed.enabled=false` to skip it.

These are the only categories. Vendor onboarding files each service type (`VendorServiceType`) under one of them; for example Car Wash and Car Detailing both go under Washing & Detailing. Onboarding needs the seed, so keep `catalog.seed.enabled` on outside tests.
