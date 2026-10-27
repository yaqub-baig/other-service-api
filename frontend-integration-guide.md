# other-service-api — Frontend Integration Guide

Covers the API after the **multi-service vendor** remodel: media, categories, vendors and their
services, and bookings. The catalog (services, attributes, offerings, search) and quotations are in
`frontend-integration-guide-customer.md`, `frontend-integration-guide-vendor.md` and
`frontend-integration-guide-admin.md`; recent changes are listed in `service-catalog-api-changes.md`.

- **Base URL:** `http://<host>:9010/api/v1`  (prod: `https://otherservice.mylstech.xyz/api/v1`)
- **Auth:** no tokens. The caller's identity is passed as header **`X-Auto-Adda-Id: <userId>`** on
  write endpoints that need an owner/customer. That id must be a real user in `identity-api`.

---

## 1. Core concept — Vendor vs VendorService

| | **Vendor** (company) | **VendorService** (one service the company offers) |
|---|---|---|
| what | the business: brand, legal name, contact, HQ address, logo, VAT cert, owner | a car wash / tyre shop / battery service run by that vendor |
| how many | one per company | **many per vendor** |
| owns | — | its own working days/hours, capacity, **trade licence + docs**, branch address, branch photos, and its own **booking slots** |
| id used in | `/vendors/{vendorId}` | `/vendors/{vendorId}/services`, `/vendors/services/{vendorServiceId}`, **all `/bookings/{vendorServiceId}/...`** |

A booking is always made against a **`vendorServiceId`**, not a vendorId.

Every service type uses the same endpoints; `serviceType` in the body says which one. Details that
only apply to some types (a car wash's bay count, express wash, …) go in the service's
**`attributes`** map. Which details a type takes is data, not code: they are its category's `VENDOR`
attributes, read from `GET /vendors/services/attributes?serviceType=`. Adding details for another
type is an admin change, with no new endpoint or table.

---

## 2. Response envelopes

Two shapes depending on the controller:

**Enveloped** (`/bookings`, `/service-categories`, `/vendor-services`, `/mediafiles`, and also
`/catalog`, `/service-offerings`, `/quotations/**` and `/admin/**`):
```json
{ "status": "success", "code": "200", "message": "...", "data": { ... } }
```

**Raw** (`/vendors/**`, `/search/**`, and `GET /bookings/{vendorServiceId}/available-slots`): the DTO
is the body directly, no wrapper. `DELETE /vendors/{id}` returns `204` with no body.
```json
{ "id": 12, "companyBrandName": "...", "services": [ ... ] }
```

Errors (all controllers) come from the global handler with **HTTP status 201**; the real status is
the `code` field in the body, so check `status`, not the HTTP status:
```json
{ "status": "failed", "code": "400", "message": "A car wash must have at least one working day", "body": null }
```
Attribute validation errors (e.g. vendor service `attributes`) list every failing field in `body`:
```json
{ "status": "failed", "code": "400", "message": "Invalid attribute values: 1 field(s) failed validation",
  "body": [ { "field": "bay_count", "message": "Wash bays is required" } ] }
```
Common codes: `400` validation / business rule, `404` not found, `409` conflict, `500` upstream
(identity-api unreachable, etc).

---

## 3. Controllers & scope

### 3.1 `MediaFileController` — `/mediafiles`  · shared (upload first, reference by id)
Upload files, then pass the returned ids into vendor/service payloads (`logoId`,
`vatCertificateIds`, `tradeLicenseAttachmentIds`, `serviceImagesIds`).

| method | path | body | notes |
|---|---|---|---|
| POST | `/mediafiles/upload` | `multipart/form-data`, field `files` (one or many) | images ≤ 10 MB, video ≤ 20 MB (the server's per-file upload limit). Returns `data: [{ id, fileName, originalFileName, mediaType, size, fileUrl, ... }]` |
| DELETE | `/mediafiles/delete-image/{id}` | — | |

### 3.2 `ServiceCategoryController` — `/service-categories`  · admin (manage) / everyone (read)
The grouped starter categories (10 seeded: Washing & Detailing, Tires & Wheels, …). Read it to
populate dropdowns. On onboarding, each service type is attached to its category automatically
(see the service types list in section 6).

| method | path | scope |
|---|---|---|
| GET | `/service-categories` | list all — **frontend reads this** |
| GET | `/service-categories/{id}` | one |
| GET | `/service-categories/root` / `/service-categories/parent/{parentId}` | hierarchy |
| POST / PUT / DELETE | `/service-categories` `/{id}` | **admin only** |

`data` item: `{ id, code, name, nameAr, description, descriptionAr, displayOrder, parentId, image, isActive, price, laborPrice }`.
`name` and `description` follow `Accept-Language` (`ar` for Arabic).

### 3.3 `VendorController` — `/vendors`  · vendor owner (write) / admin + discovery (read)
The company record, and the services a vendor adds. Raw response bodies (no envelope). Header
`X-Auto-Adda-Id` = owner on the write endpoints.

| method | path | scope | body |
|---|---|---|---|
| POST | `/vendors` | vendor owner — create company | `VendorCompanyRequest` |
| PUT | `/vendors/{id}` | vendor owner — edit company | `VendorCompanyRequest` (partial ok) |
| GET | `/vendors/{id}` | anyone — full profile incl. `services[]` | — |
| GET | `/vendors` | owner's own companies (header) | — |
| GET | `/vendors/all` | admin / listing — every active vendor | — |
| GET | `/vendors/brandname/{name}` | search by brand (contains) | — |
| GET | `/vendors/search?city=&zip=&vendorName=&serviceCategoryIds=` | discovery search; returns `VendorResponse` items (company fields only, no `services`) | — |
| DELETE | `/vendors/{id}` | soft-delete (isActive=false) | — |
| POST | `/vendors/onboard` | vendor owner — create **company + first service** in one call | `VendorOnboardingRequest` = `{ company: VendorCompanyRequest, service: VendorServiceOnboardingRequest }` |
| POST | `/vendors/{vendorId}/services` | vendor owner — add a service to an **existing** company | `VendorServiceOnboardingRequest` |
| PUT | `/vendors/services/{vendorServiceId}` | vendor owner — edit a service (hours, capacity, licence, details) | `VendorServiceOnboardingRequest` (partial ok) |
| GET | `/vendors/{vendorId}/services` | anyone — a vendor's services | — |
| GET | `/vendors/services?serviceType=` | the caller's services (header); all types if `serviceType` is left out | — |
| GET | `/vendors/services/all?serviceType=` | every active service (listing); all types if left out | — |
| GET | `/vendors/services/attributes?serviceType=CAR_WASH` | the details form for a type | — |

`POST /vendors/onboard` returns `{ vendor: VendorProfileResponse, service: VendorServiceResponse }`;
the other service endpoints return `VendorServiceResponse`.

`VendorProfileResponse`:
```json
{
  "id": 12, "companyBrandName": "...", "companyArabicName": "...", "name": "...",
  "trnNumber": "...", "webSite": "...", "email": "...", "phoneNumber": "...", "mobileNumber": "...",
  "accountStatus": "PENDING", "countryCode": "AE",
  "companyLogo": { "id": 1, "fileUrl": "..." },
  "companyLocation": { "address": "...", "city": "...", "state": "...", "country": "...", "zip": "...", "latitude": 0, "longitude": 0 },
  "vatCertificate": [ ... ],
  "services": [ VendorServiceResponse, ... ]
}
```

### 3.4 `VendorServiceController` — `/vendor-services`  · generic per-service CRUD
Low-level access to `VendorService` rows and their pricing. Use the `/vendors/.../services`
endpoints above for onboarding; use this for pricing, admin/debug or generic reads.

| method | path | notes |
|---|---|---|
| GET | `/vendor-services` | all |
| GET | `/vendor-services/{id}` | one service |
| GET | `/vendor-services/vendor/{vendorId}` | a vendor's services |
| POST | `/vendor-services` | body `{ vendorId, serviceCategoryId, isActive }` plus optional pricing — bare link, no hours/slots. Reactivates the existing row if the vendor already has that category |
| PUT | `/vendor-services/{id}` | same body as POST; sets pricing (`pricingType` `FIXED` / `RANGE` / `QUOTE_BASED` / `NEGOTIABLE`, `price`, `minPrice`, `maxPrice`, `currency`, `description`) |
| DELETE | `/vendor-services/{id}` | deactivates; offerings, bookings and quotes keep pointing at it |

`isActive` is optional: POST defaults it to `true`, and PUT leaves it unchanged when it isn't sent
(`active` is accepted as an alias).

`VendorServiceResponse`:
```json
{
  "id": 5, "isActive": true, "vendorId": 12, "vendorName": "...",
  "serviceCategoryId": 13, "serviceCategoryName": "Car Wash", "serviceType": "CAR_WASH",
  "openingTime": "08:00", "closingTime": "20:00", "workingDays": "MONDAY,TUESDAY,...",
  "concurrentAppointmentCapacity": 3, "tradeLicenceNumber": "CN-1099001",
  "accountStatus": "PENDING",
  "serviceLocation": { LocationResponse }, "serviceImages": [ ... ], "tradeLicenseAttachment": [ ... ],
  "pricingType": "FIXED", "price": 120.00, "minPrice": null, "maxPrice": null, "currency": "AED",
  "description": "...",
  "attributes": { "bay_count": 4, "mobile_service": false }
}
```

### 3.5 `BookingController` — `/bookings`  · customer (book) / vendor (manage)
All `{serviceId}` / `{vendorServiceId}` path vars are **VendorService ids**.

| method | path | scope | params / body |
|---|---|---|---|
| GET | `/bookings/{serviceId}/available-slots?date=dd/MM/yyyy` | customer — show slots | date is **dd/MM/yyyy** |
| POST | `/bookings/{serviceId}/book` | customer — book a slot | query: `vehicleId`, `time` (ISO `2026-09-05T09:00:00`), `catalogServiceId` (optional), `selectedVendorServiceIds` (csv, optional); header `X-Auto-Adda-Id` = customer |
| POST | `/bookings` | customer — direct booking (no slot) | body `CustomerBookingRequest` |
| GET | `/bookings/{bookingId}` | booking detail (services + job cards) | — |
| GET | `/bookings/user/bookings` | customer's own bookings | header `X-Auto-Adda-Id` |
| GET | `/bookings/vendor/{vendorId}` | vendor — all bookings across its services | `?status=` optional |
| PUT | `/bookings/status` | vendor — change status | body `{ appointmentId, status, comments, memberId, memberType }` |
| PUT | `/bookings/edit?bookingId=&serviceId=&vehicleId=&time=` | reschedule / move | header `X-Auto-Adda-Id` |
| DELETE | `/bookings/{bookingId}/cancel` | cancel | header `X-Auto-Adda-Id` |

`available-slots` returns `{ vendorServiceId, date, availableSlots: [{ slotId, startTime, endTime,
maxBookings, currentBookings, availableBookings, isAvailable }] }` with no envelope.

When `catalogServiceId` is sent (slot booking or `POST /bookings`), a catalog service whose
quotation mode is `REQUIRED` is refused (book from an accepted quote instead), and the vendor must
offer the service's category.

---

## 4. Request DTOs

### `VendorCompanyRequest`
```json
{
  "companyBrandName": "Sparkle Auto Care",
  "companyArabicName": "سباركل",
  "name": "Sparkle Auto Care LLC",
  "trnNumber": "100123456700003",
  "webSite": "https://sparkle.example.com",
  "email": "owner@sparkle.example.com",
  "phoneNumber": "042221111",
  "mobileNumber": "0501234567",
  "countryCode": "AE",
  "companyLocation": {
    "streetAddress1": "Warehouse 12, Al Quoz Industrial 3",
    "streetAddress2": "",
    "city": "Dubai", "postalCode": "00000", "area": "Al Quoz",
    "state": "Dubai", "country": "AE",
    "gmap": "https://maps.google.com/?q=25.13,55.23",
    "latitude": 25.13, "longitude": 55.23
  },
  "logoId": 7,
  "vatCertificateIds": [8, 9]
}
```
`logoId` / `vatCertificateIds` optional — upload via `/mediafiles/upload` first. `countryCode`
(ISO, e.g. `AE`) is the vendor's market and decides offering currency; defaults to `AE`.

### `VendorServiceOnboardingRequest`
```json
{
  "serviceType": "CAR_WASH",
  "openingTime": "08:00:00",
  "closingTime": "20:00:00",
  "breakStartTime": "13:00:00",
  "breakEndTime": "14:00:00",
  "workingDays": ["MONDAY","TUESDAY","WEDNESDAY","THURSDAY","FRIDAY","SATURDAY"],
  "concurrentAppointmentCapacity": 3,
  "tradeLicenceNumber": "CN-1099001",
  "serviceLocation": { "streetAddress1": "Shop 4", "city": "Dubai", "country": "AE", "latitude": 25.14, "longitude": 55.24 },
  "tradeLicenseAttachmentIds": [10],
  "serviceImagesIds": [11, 12],
  "serviceCategoryIds": [],
  "attributes": {
    "bay_count": 4,
    "average_wash_duration_minutes": 30,
    "mobile_service": false,
    "express_wash_available": true,
    "accepts_walk_ins": true
  }
}
```
- `serviceType` — required when adding a service, ignored on update. One of the types in section 6.
- `workingDays` — `java.time.DayOfWeek` names, UPPERCASE.
- times — `HH:mm:ss` (or `HH:mm`).
- `serviceLocation` optional → falls back to the company HQ address.
- `serviceCategoryIds` — extra categories to attach beyond the primary one for this type.
- `attributes` — the type's details, keyed by attribute code. `GET /vendors/services/attributes?serviceType=`
  lists the codes, types, required flags and options (with Arabic labels under `Accept-Language: ar`).
  Unknown codes and invalid values are refused with every failing field listed. On update, sent codes
  are merged into what's stored and a `null` value clears one.

### `CustomerBookingRequest`
```json
{
  "customerId": 42,
  "vendorServiceId": 5,
  "catalogServiceId": 3,
  "vehicleId": 101,
  "serviceIds": [5],
  "scheduleDate": "2026-09-05",
  "serviceStartTime": "09:00:00",
  "serviceEndTime": "09:30:00",
  "deliveryTime": "10:00:00",
  "comments": "Full exterior + interior",
  "memberId": 42,
  "memberType": "CUSTOMER"
}
```

---

## 5. End-to-end flows

### A. Onboard a vendor (2-step wizard)
1. *(optional)* `POST /mediafiles/upload` → collect `logoId`, `tradeLicenseAttachmentIds`, `serviceImagesIds`.
2. **Step 1 — company:** `POST /vendors` with `VendorCompanyRequest`, header `X-Auto-Adda-Id`.
   → `{ id: <vendorId> }`, `accountStatus: PENDING`, owner role set to `OTHER_SERVICE_OWNER`.
3. **Step 2 — service:** `GET /vendors/services/attributes?serviceType=CAR_WASH` to build the details
   form, then `POST /vendors/{vendorId}/services` with `VendorServiceOnboardingRequest`.
   → `{ id: <vendorServiceId> }`. This creates the working-hours rows and 60 days of booking slots.
   *(Or skip 2+3 and do both at once: `POST /vendors/onboard` with `{ company, service }`.)*
4. Edit later: `PUT /vendors/services/{vendorServiceId}` (changing hours/capacity regenerates slots).

A vendor adds a second service (e.g. also does battery) with another `POST /vendors/{vendorId}/services`
and a different `serviceType` — same company, independent hours & slots.

### B. Customer books
1. `GET /vendors/search?...` or `GET /vendors/services/all?serviceType=CAR_WASH` → pick a `vendorServiceId` (the service's `id`).
2. `GET /bookings/{vendorServiceId}/available-slots?date=05/09/2026` → list of slots.
3. `POST /bookings/{vendorServiceId}/book?vehicleId=101&time=2026-09-05T09:00:00` + header `X-Auto-Adda-Id` = customer id.
4. `GET /bookings/user/bookings` (header) → customer's bookings. `GET /bookings/{bookingId}` → detail.

### C. Vendor manages bookings
- `GET /bookings/vendor/{vendorId}?status=CONFIRMED` — across all the vendor's services.
- `PUT /bookings/status` — `WORK_INPROGRESS` → `COMPLETED` → `DELIVERED` (auto-maps to `DONE`).

---

## 6. Enums

**`workingDays`** — `MONDAY … SUNDAY`

**Service types** (`serviceType` on `VendorService`) and the category each is filed under:

| category | service types |
|---|---|
| Washing & Detailing | `CAR_WASH`, `CAR_DETAILING`, `UPHOLSTERY` |
| Routine Maintenance | `OIL_CHANGE` |
| Tires & Wheels | `TYRE_AND_BALANCING` |
| Electrical & EV | `BATTERY`, `EV_SERVICES` |
| Repairs & Diagnostics | `SUSPENSION`, `CAR_INSPECTION`, `QUALITY_CONTROL`, `ENGINEERING_WORK`, `HYDRAULIC_HOSE`, `PART_REPAIRING` |
| Tinting & Paint Protection | `GLASS_SERVICES` |
| Roadside Assistance | `RECOVERY`, `FUEL_SERVICES` |
| Insurance | `INSURANCE` |
| Accessories & Modification | `ACCESSORIES`, `MODIFICATION` |
| Other Services | `TOOLS`, `DELIVERY` |

Only Washing & Detailing has detail attributes in the starter seed (the eight car wash fields).

**Booking `status`** (subset): `CONFIRMED`, `WORK_INPROGRESS`, `COMPLETED`, `DELIVERED`, `DONE`,
`CANCELLED`, `PENDING`, `NEW`, `ASSIGNED`.

**`accountStatus`** (subset): `PENDING`, `VERIFIED`, `ACTIVE`, `SUSPENDED`, `REJECTED` (see `util/Status`).
New companies and services start as `PENDING`.

---

## 7. Breaking changes vs the previous API

| before | now |
|---|---|
| `POST /vendors` took the full flat form (company + hours + services) | `POST /vendors` = company only; hours/services via `POST /vendors/{id}/services` |
| `/vendors/car-wash`, `/vendors/{id}/services/car-wash`, `/vendors/car-wash/{id}`, `/vendors/car-wash/all` | `/vendors/onboard`, `/vendors/{id}/services`, `/vendors/services/{id}`, `/vendors/services/all?serviceType=CAR_WASH` |
| car wash fields at the top level of the request (`bayCount`, `mobileService`, …) and a `carWashDetails` block in the response | `attributes` map on the request and response (`bay_count`, `mobile_service`, …) |
| `/os-appointments/**` | `/bookings/**` |
| `/os-appointments/{id}/book` — `id` = vendorId | `/bookings/{id}/book` — `id` = **vendorServiceId** |
| `/os-appointments/other-service/{id}` | `/bookings/vendor/{vendorId}` (all branches) |
| `selectedOtherServiceIds` param | `selectedVendorServiceIds` |
| `/vendor-service-bridges` | `/vendor-services` |
| `VendorProfileResponse` had `serviceType`, `openingTime`, `closingTime`, `workingDays`, `concurrentAppointmentCapacity`, `tradeLicenceNumber` at top level | those moved into each `services[]` entry (`VendorServiceResponse`) |
