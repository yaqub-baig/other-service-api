# other-service-api — Frontend Integration Guide

Covers the API after the **multi-service vendor** remodel.

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
| id used in | `/vendors/{vendorId}` | `/vendors/{vendorId}/services/...`, `/vendors/car-wash/{vendorServiceId}`, **all `/bookings/{vendorServiceId}/...`** |

A booking is always made against a **`vendorServiceId`**, not a vendorId.

Each service type also has a **detail block** (e.g. `carWashDetails` — bay count, express wash, …).
Adding a new service type on the backend = a new `/vendors/<type>` controller + detail table; the
generic `/vendors` and `/bookings` endpoints don't change.

---

## 2. Response envelopes

Two shapes depending on the controller:

**Enveloped** (`/bookings`, `/service-categories`, `/vendor-services`, `/mediafiles`):
```json
{ "status": "success", "code": "200", "message": "...", "data": { ... } }
```

**Raw** (`/vendors`, `/vendors/car-wash`): the DTO is the body directly, no wrapper.
```json
{ "id": 12, "companyBrandName": "...", "services": [ ... ] }
```

Errors (all controllers) come from the global handler as:
```json
{ "status": "failed", "code": "400", "message": "A car wash must have at least one working day", "body": null }
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
| POST | `/mediafiles/upload` | `multipart/form-data`, field `files` (one or many) | images ≤ 10 MB, video ≤ 30 MB. Returns `data: [{ id, url, ... }]` |
| DELETE | `/mediafiles/delete-image/{id}` | — | |

### 3.2 `ServiceCategoryController` — `/service-categories`  · admin (manage) / everyone (read)
The catalogue of service types (21 seeded: Car Wash, Battery, Tyre & Balancing, …). Read it to
populate dropdowns; the category whose `name` matches a service type is auto-attached on onboarding.

| method | path | scope |
|---|---|---|
| GET | `/service-categories` | list all — **frontend reads this** |
| GET | `/service-categories/{id}` | one |
| GET | `/service-categories/root` / `/service-categories/parent/{parentId}` | hierarchy |
| POST / PUT / DELETE | `/service-categories` `/{id}` | **admin only** |

`data` item: `{ id, name, description, image, isActive, price, laborPrice }`

### 3.3 `VendorController` — `/vendors`  · vendor owner (write) / admin + discovery (read)
The **company** record only. Raw response bodies (no envelope). Header `X-Auto-Adda-Id` = owner.

| method | path | scope | body |
|---|---|---|---|
| POST | `/vendors` | vendor owner — create company | `VendorCompanyRequest` |
| PUT | `/vendors/{id}` | vendor owner — edit company | `VendorCompanyRequest` (partial ok) |
| GET | `/vendors/{id}` | anyone — full profile incl. `services[]` | — |
| GET | `/vendors` | owner's own companies (header) | — |
| GET | `/vendors/all` | admin / listing — every active vendor | — |
| GET | `/vendors/brandname/{name}` | search by brand (contains) | — |
| GET | `/vendors/search?city=&zip=&vendorName=&serviceCategoryIds=` | discovery search | — |
| DELETE | `/vendors/{id}` | soft-delete (isActive=false) | — |

`VendorProfileResponse`:
```json
{
  "id": 12, "companyBrandName": "...", "companyArabicName": "...", "name": "...",
  "trnNumber": "...", "webSite": "...", "email": "...", "phoneNumber": "...", "mobileNumber": "...",
  "accountStatus": "PENDING",
  "companyLogo": { "id": 1, "url": "..." },
  "companyLocation": { "address": "...", "city": "...", "state": "...", "country": "...", "zip": "...", "latitude": 0, "longitude": 0 },
  "vatCertificate": [ ... ],
  "services": [ VendorServiceResponse, ... ]
}
```

### 3.4 `VendorServiceController` — `/vendor-services`  · generic per-service CRUD
Low-level access to `VendorService` rows. Prefer the **typed** controllers below for onboarding —
use this for admin/debug or generic reads. (Alias `/vendor-service-bridges` also works.)

| method | path | notes |
|---|---|---|
| GET | `/vendor-services` | all |
| GET | `/vendor-services/{id}` | one service |
| GET | `/vendor-services/vendor/{vendorId}` | a vendor's services |
| POST | `/vendor-services` | body `{ vendorId, serviceId (categoryId), isActive }` — bare link, no hours/slots |
| PUT / DELETE | `/vendor-services/{id}` | |

`VendorServiceResponse`:
```json
{
  "id": 5, "isActive": true, "vendorId": 12, "vendorName": "...",
  "serviceCategoryId": 13, "serviceCategoryName": "Car Wash", "serviceType": "CAR_WASH",
  "openingTime": "08:00", "closingTime": "20:00", "workingDays": "MONDAY,TUESDAY,...",
  "concurrentAppointmentCapacity": 3, "tradeLicenceNumber": "CN-1099001",
  "accountStatus": "PENDING",
  "serviceLocation": { LocationResponse }, "serviceImages": [ ... ], "tradeLicenseAttachment": [ ... ]
}
```

### 3.5 `CarWashVendorController` — `/vendors/car-wash*`  · vendor owner (Car Wash)
The typed onboarding + management surface for a Car Wash service. This is the template every other
service type will copy (`/vendors/battery`, `/vendors/tyre`, …). Raw response bodies. Header
`X-Auto-Adda-Id` = owner on the write endpoints.

| method | path | scope | body |
|---|---|---|---|
| POST | `/vendors/car-wash` | create **company + first car-wash service** in one call | `CarWashVendorRequest` = `{ company: VendorCompanyRequest, service: CarWashVendorServiceRequest }` |
| POST | `/vendors/{vendorId}/services/car-wash` | add a car-wash service to an **existing** company | `CarWashVendorServiceRequest` |
| PUT | `/vendors/car-wash/{vendorServiceId}` | edit a car-wash service (hours, capacity, licence, details) | `CarWashVendorServiceRequest` (partial ok) |
| GET | `/vendors/car-wash` | the caller's car-wash services (header) | — |
| GET | `/vendors/car-wash/all` | every car-wash service (listing) | — |

`CarWashVendorResponse`: `{ vendor: VendorProfileResponse, service: VendorServiceResponse, carWashDetails: {...} }`

### 3.6 `BookingController` — `/bookings`  · customer (book) / vendor (manage)
All `{serviceId}` / `{vendorServiceId}` path vars are **VendorService ids**.

| method | path | scope | params / body |
|---|---|---|---|
| GET | `/bookings/{serviceId}/available-slots?date=dd/MM/yyyy` | customer — show slots | date is **dd/MM/yyyy** |
| POST | `/bookings/{serviceId}/book` | customer — book a slot | query: `vehicleId`, `time` (ISO `2026-09-05T09:00:00`), `selectedVendorServiceIds` (csv, optional); header `X-Auto-Adda-Id` = customer |
| POST | `/bookings` | customer — direct booking (no slot) | body `CustomerBookingRequest` |
| GET | `/bookings/{bookingId}` | booking detail (services + job cards) | — |
| GET | `/bookings/user/bookings` | customer's own bookings | header `X-Auto-Adda-Id` |
| GET | `/bookings/vendor/{vendorId}` | vendor — all bookings across its services | `?status=` optional |
| PUT | `/bookings/status` | vendor — change status | body `{ appointmentId, status, comments, memberId, memberType }` |
| PUT | `/bookings/edit?bookingId=&serviceId=&vehicleId=&time=` | reschedule / move | header `X-Auto-Adda-Id` |
| DELETE | `/bookings/{bookingId}/cancel` | cancel | header `X-Auto-Adda-Id` |

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
`logoId` / `vatCertificateIds` optional — upload via `/mediafiles/upload` first.

### `BaseVendorServiceRequest` (fields shared by every service type)
```json
{
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
  "serviceCategoryIds": []
}
```
- `workingDays` — `java.time.DayOfWeek` names, UPPERCASE.
- times — `HH:mm:ss` (or `HH:mm`).
- `serviceLocation` optional → falls back to the company HQ address.
- `serviceCategoryIds` — extra categories to attach beyond the primary one for this type.

### `CarWashVendorServiceRequest` (= `BaseVendorServiceRequest` + car-wash fields)
```json
{
  "openingTime": "08:00:00", "closingTime": "20:00:00",
  "workingDays": ["MONDAY","TUESDAY","WEDNESDAY","THURSDAY","FRIDAY","SATURDAY"],
  "concurrentAppointmentCapacity": 3,

  "bayCount": 4,
  "averageWashDurationMinutes": 30,
  "mobileService": false,
  "selfServiceAvailable": true,
  "expressWashAvailable": true,
  "interiorDetailingAvailable": true,
  "waterRecycling": true,
  "acceptsWalkIns": true
}
```

### `CarWashVendorRequest` (combined create)
```json
{ "company": { VendorCompanyRequest }, "service": { CarWashVendorServiceRequest } }
```

### `CustomerBookingRequest`
```json
{
  "customerId": 42,
  "vendorServiceId": 5,
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

### A. Onboard a car wash (2-step wizard)
1. *(optional)* `POST /mediafiles/upload` → collect `logoId`, `tradeLicenseAttachmentIds`, `serviceImagesIds`.
2. **Step 1 — company:** `POST /vendors` with `VendorCompanyRequest`, header `X-Auto-Adda-Id`.
   → `{ id: <vendorId> }`, `accountStatus: PENDING`, owner role set to `OTHER_SERVICE_OWNER`.
3. **Step 2 — service:** `POST /vendors/{vendorId}/services/car-wash` with `CarWashVendorServiceRequest`.
   → `{ service: { id: <vendorServiceId> } }`. This creates the working-hours rows and 60 days of
   booking slots.
   *(Or skip 2+3 and do both at once: `POST /vendors/car-wash` with `{ company, service }`.)*
4. Edit later: `PUT /vendors/car-wash/{vendorServiceId}` (changing hours/capacity regenerates slots).

A vendor adds a second service (e.g. also does battery, once that controller exists) by calling that
type's `POST /vendors/{vendorId}/services/<type>` — same company, independent hours & slots.

### B. Customer books
1. `GET /vendors/search?...` or `GET /vendors/car-wash/all` → pick a `vendorServiceId` (from `service.id`).
2. `GET /bookings/{vendorServiceId}/available-slots?date=05/09/2026` → list of slots.
3. `POST /bookings/{vendorServiceId}/book?vehicleId=101&time=2026-09-05T09:00:00` + header `X-Auto-Adda-Id` = customer id.
4. `GET /bookings/user/bookings` (header) → customer's bookings. `GET /bookings/{bookingId}` → detail.

### C. Vendor manages bookings
- `GET /bookings/vendor/{vendorId}?status=CONFIRMED` — across all the vendor's services.
- `PUT /bookings/status` — `WORK_INPROGRESS` → `COMPLETED` → `DELIVERED` (auto-maps to `DONE`).

---

## 6. Enums

**`workingDays`** — `MONDAY … SUNDAY`

**Service types** (`serviceType` on `VendorService`; matching `ServiceCategory.name` in brackets):
`CAR_WASH` (Car Wash), `TYRE_AND_BALANCING` (Tyre & Balancing), `BATTERY` (Battery),
`SUSPENSION`, `RECOVERY`, `INSURANCE`, `CAR_INSPECTION` (Car Inspection), `QUALITY_CONTROL`
(Quality Control), `ENGINEERING_WORK` (Engineering Work), `HYDRAULIC_HOSE` (Hydraulic Hose),
`PART_REPAIRING` (Part Repairing), `ACCESSORIES`, `UPHOLSTERY`, `CAR_DETAILING` (Car Detailing),
`MODIFICATION`, `GLASS_SERVICES` (Glass Services), `OIL_CHANGE` (Oil Change), `FUEL_SERVICES`
(Fuel Services), `EV_SERVICES` (EV Services), `TOOLS`, `DELIVERY`.
Only `CAR_WASH` has a typed controller today.

**Booking `status`** (subset): `CONFIRMED`, `WORK_INPROGRESS`, `COMPLETED`, `DELIVERED`, `DONE`,
`CANCELLED`, `PENDING`, `NEW`, `ASSIGNED`.

**`accountStatus`**: `PENDING`, `VERIFIED`, `ACTIVE`, `SUSPENDED`, `REJECTED` (see `util/Status`).

---

## 7. Breaking changes vs the previous API

| before | now |
|---|---|
| `POST /vendors` took the full flat form (company + hours + services) | `POST /vendors` = company only; hours/services via `POST /vendors/{id}/services/<type>` |
| `/os-appointments/**` | `/bookings/**` |
| `/os-appointments/{id}/book` — `id` = vendorId | `/bookings/{id}/book` — `id` = **vendorServiceId** |
| `/os-appointments/other-service/{id}` | `/bookings/vendor/{vendorId}` (all branches) |
| `selectedOtherServiceIds` param | `selectedVendorServiceIds` |
| `/vendor-service-bridges` | `/vendor-services` (old path still aliased) |
| `VendorProfileResponse` had `serviceType`, `openingTime`, `closingTime`, `workingDays`, `concurrentAppointmentCapacity`, `tradeLicenceNumber` at top level | those moved into each `services[]` entry (`VendorServiceResponse`) |
