# other-service-api

The Auto Adda service for non-garage vehicle services: vendors (car wash, tyres, batteries, tinting and so on), the service catalog they sell from, offerings, quotations, bookings and job cards.

Spring Boot 3.5 on Java 21, MySQL 8, Maven.

## Run locally

You need Java 21, Maven and a MySQL 8 server on `localhost:3306`.

1. Put your MySQL password in `src/main/resources/application-local.properties` (`spring.datasource.password`). The user defaults to `root`.
2. Start the service:

   ```bash
   mvn spring-boot:run
   ```

The `auto_adda_other_service` database is created if it doesn't exist. Hibernate (`ddl-auto=update`) creates the tables; there is no migration tool. To start from scratch, drop the database and restart.

| What | URL |
| --- | --- |
| API base | `http://localhost:9010/api/v1` |
| Swagger UI | `http://localhost:9010/api/v1/swagger-ui.html` |
| Health | `http://localhost:9010/api/v1/actuator/health` |

Eureka registration is switched off by default (`eureka.client.register-with-eureka=false`), so no Eureka server is needed.

## Profiles

| Profile | Used for | Database settings |
| --- | --- | --- |
| `local` (default) | Development | `application-local.properties` |
| `prod` | The Docker image | `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD` environment variables |
| `test` | Automated tests | In-memory H2, seeding off |

## Configuration

| Property | Default | Meaning |
| --- | --- | --- |
| `server.port` | `9010` | HTTP port |
| `clients.identity-api.url` | `http://localhost:8010/api/v1` | Identity service (env `IDENTITY_API_URL`) |
| `clients.vehicle-api.url` | `http://localhost:8030/api/v1` | Vehicle service (env `VEHICLE_API_URL`) |
| `catalog.seed.enabled` | `true` | Load the starter catalog on startup |
| `catalog.seed.markets` | `AE` | Countries the starter services are enabled in, e.g. `AE,SA,QA` |
| `catalog.default-country` | `AE` | Country for vendors that don't send one |
| `catalog.max-indexed-attributes` | `20` | Cap on attributes with a search index |
| `quotation.expiry.cron` | hourly | When expired quotation requests and quotes are closed |
| `file.upload-dir` | `../uploaded-files` | Where uploaded media is stored |

## Starter catalog

On startup, `CatalogSeeder` loads `src/main/resources/seed/catalog-seed.json`: 10 categories and 14 services, each with a published v1 attribute schema (39 attributes, 64 options) and Arabic labels. It matches existing rows by code, so restarts don't duplicate anything. Set `catalog.seed.enabled=false` to skip it.

These are the only categories. Vendor onboarding files each service type (`VendorServiceType`) under one of them; for example Car Wash and Car Detailing both go under Washing & Detailing. Onboarding needs the seed, so keep `catalog.seed.enabled` on outside tests.

## How the catalog fits together

```
ServiceCategory  ──<  CatalogService  ──<  ServiceSchemaVersion  ──<  ServiceAttributeDefinition
 (Tires & Wheels)     (Tire Replacement)    (v1 PUBLISHED, v2 DRAFT)    (rim_size, tyre_brand, ...)
                            │
                            ├──< ServiceMarketAvailability   (country, currency)
                            │
Vendor ──< VendorService ──<  ServiceOffering ── catalog service + schema version
```

- **Attributes:** category-level attributes are defaults, and a service's published schema adds to or overrides them. Only published versions are live, and they can't be edited; changes go into a new draft.
- **Validation:** offerings and quotation requests are checked by one `AttributeValidator`, covering required fields, types, options and validation rules.
- **Quotation mode:** a service is DIRECT, OPTIONAL or REQUIRED; when it has no mode set, it follows its category's quotation scope.
- **Languages:** send `Accept-Language: ar` for Arabic labels.

Endpoint details for each frontend are in `frontend-integration-guide-admin.md`, `frontend-integration-guide-vendor.md` and `frontend-integration-guide-customer.md`. Recent breaking changes are listed in `service-catalog-api-changes.md`.

## Tests

```bash
mvn test
```

Tests run against in-memory H2, so no MySQL is needed. They don't cover MySQL-only SQL: the native JSON search queries and the generated-column indexes.

## Docker

```bash
docker build -t other-service-api .
docker run -p 9010:9010 \
  -e SPRING_DATASOURCE_URL='jdbc:mysql://<host>:3306/auto_adda_other_service?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true' \
  -e SPRING_DATASOURCE_USERNAME=<user> \
  -e SPRING_DATASOURCE_PASSWORD=<password> \
  other-service-api
```

The Dockerfile builds the jar itself. The image runs with the `prod` profile and has no credentials baked in.

## Request conventions

- **User identity:** the caller's user id arrives in the `X-Auto-Adda-Id` header.
- **Error status:** errors return HTTP 201 with the real status in the body's `code` field, for example `"code": "400"`. Validation errors list each failing field in `body`.
