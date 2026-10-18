# Implementation Roadmap: Building the Dynamic Service Marketplace (MySQL Edition)

This document provides a comprehensive, staged roadmap to evolve the current application into the fully dynamic, multi-service automotive marketplace described in the schema summary, using **MySQL** as the database.

The plan is broken down into sequential stages. Each stage builds upon the previous one, ensuring a stable and logical development process. It is critical to complete the stages in order, as foundational refactoring must happen before new features are built on top of it.

---

## Stage 1: Foundational Refactoring and Dependencies

**Goal:** Align the existing codebase with the new, clearer architectural vision and entity names. This is the most disruptive but most important stage.

### 1.1. Add Dependencies
- **Action:** Update `pom.xml` to include dependencies for spatial data handling with MySQL.
- **Details:**
    - Add dependencies for **Hibernate Spatial** and a JTS (Java Topology Suite) library. This will allow JPA/Hibernate to work with MySQL's spatial data types.
    ```xml
    <!-- Example Dependency for JTS -->
    <dependency>
        <groupId>org.locationtech.jts</groupId>
        <artifactId>jts-core</artifactId>
        <version>1.19.0</version> <!-- Use a recent version -->
    </dependency>
    <!-- Example Dependency for Hibernate Spatial -->
    <dependency>
        <groupId>org.hibernate.orm</groupId>
        <artifactId>hibernate-spatial</artifactId>
        <version>6.4.4.Final</version> <!-- Match your Hibernate version -->
    </dependency>
    ```

### 1.2. Refactor `OtherService` to `Vendor`
- **Action:** Rename the entity representing a vendor.
- **Files to Change:**
    - `src/main/java/com/autoadda/apis/other/entity/OtherService.java` -> `Vendor.java`
    - Update `@Entity` and `@Table` annotations.
    - `OtherServiceRepository.java` -> `VendorRepository.java`
    - `OtherServiceManagementService.java` -> `VendorService.java`
    - `OtherServiceController.java` -> `VendorController.java`
    - Update all relationships, method signatures, and variable names across the entire application that reference the old name.

### 1.3. Refactor `OsAvailableService` to `ServiceCategory`
- **Action:** Rename the entity representing a master service category.
- **Files to Change:**
    - `OsAvailableService.java` -> `ServiceCategory.java`
    - `OsAvailableServiceRepository.java` -> `ServiceCategoryRepository.java`
    - `OsAvailableServiceController.java` -> `ServiceCategoryController.java`
    - `OsAvailableServiceSeeder.java` -> `ServiceCategorySeeder.java`
    - Update all references.

### 1.4. Refactor `OsService` to `VendorService`
- **Action:** Rename the join entity between a vendor and a service category.
- **Files to Change:**
    - `OsService.java` -> `VendorService.java`
    - `OsServiceRepository.java` -> `VendorServiceRepository.java`
    - Update all references, especially within the `Vendor` and `ServiceCategory` entities.

### 1.5. Refactor `OsAppointmentDetails` to `Booking`
- **Action:** Rename the entity representing a customer booking.
- **Files to Change:**
    - `OsAppointmentDetails.java` -> `Booking.java`
    - `OsAppointmentDetailsRepository.java` -> `BookingRepository.java`
    - `OsAppointmentController.java` -> `BookingController.java`
    - `OsAppointmentService.java` -> `BookingService.java`
    - Update all references.

---

## Stage 2: Implement Dynamic Service & Filter Configuration

**Goal:** Build the admin-facing tools to dynamically define service categories, attributes, and filters without code changes.

### 2.1. Enhance `ServiceCategory` for Hierarchy
- **Action:** Add support for parent-child relationships.
- **File:** `ServiceCategory.java`
- **Modification:** Add a self-referencing `parentId` field.
    ```java
    @ManyToOne
    @JoinColumn(name = "parent_id")
    private ServiceCategory parent;
    ```

### 2.2. Create `ServiceAttributeDefinition` Module
- **Action:** Create the entity and full CRUD management for service attributes.
- **New Entity:** `ServiceAttributeDefinition.java` (with fields for `serviceCategory`, `name`, `dataType`, `isFilterable`, `isRequired`, etc.).
- **New Repository, Service, and Controller:** Create the full stack (`ServiceAttributeRepository`, `ServiceAttributeService`, `ServiceAttributeAdminController`) for managing these definitions.

### 2.3. Create Dynamic Filter System Module
- **Action:** Create the entities and management APIs for dynamic filters.
- **New Entity:** `ServiceFilterDefinition.java` (with `serviceCategory`, `filterCode`, `filterLabel`, `filterType`, etc.).
- **New Entity:** `FilterOption.java` (linked to `ServiceFilterDefinition` to store dropdown options).
- **New Repository, Service, and Controller:** Create the full stack for managing filter definitions and their options via an admin API.

---

## Stage 3: Implement Dynamic Vendor Offerings & Pricing

**Goal:** Empower vendors to create specific, detailed offerings and define their pricing. This is the core of the marketplace functionality.

### 3.1. Implement Pricing Models
- **Action:** Add pricing strategies to the `VendorService` entity.
- **New Enum:** `PricingType.java` (`FIXED`, `RANGE`, `QUOTE_BASED`, `NEGOTIABLE`).
- **File:** `VendorService.java`
- **Modification:** Add the `pricingType` enum field and `price`, `minPrice`, `maxPrice` columns.

### 3.2. Create the `ServiceOffering` Entity (Crucial)
- **Action:** Create the new `ServiceOffering` entity to represent a specific, sellable item.
- **New Entity:** `ServiceOffering.java`
- **Key Fields:**
    - `vendorService` (link to the vendor's capability).
    - `offeringName`, `sku`, `price`, `stockStatus`.
    - **`filterableAttributes` (JSON):** This is the most critical field. It will store a JSON object of key-value pairs corresponding to the `ServiceAttributeDefinition` and `ServiceFilterDefinition` for that category.
- **New Repository, Service, and Controller:** Create a full stack for vendors to manage their own service offerings.

---

## Stage 4: Implement Dynamic Search & Discovery

**Goal:** Replace the static search functionality with a powerful, dynamic query engine using MySQL's features.

### 4.1. Refactor Search Service for JSON
- **Action:** Rewrite the search logic to query the `ServiceOffering` entity's JSON column.
- **Service:** The existing service that handles search.
- **Logic:**
    - The method will now accept a dynamic `Map<String, Object>` of filters.
    - It will use the JPA Criteria API or native queries to dynamically build a query.
    - It must query against the `filterableAttributes` JSON field, using MySQL functions like `JSON_EXTRACT` or `JSON_CONTAINS` in the `WHERE` clause.

### 4.2. Implement Geo-Spatial Search with MySQL
- **Action:** Add location-based search capabilities using MySQL's native spatial functions.
- **Logic:**
    - In the `Vendor` entity, change the `companyLocation` field to use `org.locationtech.jts.geom.Point`.
    - Configure `application.properties` to use a MySQL-specific spatial dialect (e.g., `org.hibernate.spatial.dialect.mysql.MySQL56SpatialDialect`).
    - The search service will use native queries with functions like `ST_Distance_Sphere` to calculate distances and find vendors within a specified radius.

### 4.3. Update Search API
- **Action:** Modify the search endpoint to be fully dynamic.
- **Controller:** The controller handling search requests.
- **Modification:** The endpoint will now accept a `@RequestBody` containing a search request object with a map of dynamic filters and location data, instead of fixed `@RequestParam` parameters.

---

## Stage 5: Implement Advanced Quotation Management

**Goal:** Build the complete, end-to-end quotation workflow as designed in `plan_v2.md`.

- **Action:** Implement all entities, repositories, services, and controllers as detailed in the `plan_v2.md` file. This includes:
    - **Admin Config:** `QuotationScopeDefinition`, `QuotationFieldDefinition`.
    - **Customer Flow:** `QuotationRequest`, `QuotationRequestFieldValue`.
    - **Vendor Flow:** `VendorQuotation`, `VendorQuotationLineItem`.
    - All corresponding services and controllers.

---

## Stage 6: Enhance Booking Management

**Goal:** Integrate the new features into the booking process.

### 6.1. Update `Booking` Entity
- **Action:** Add fields for detailed pricing and quote references.
- **File:** `Booking.java`
- **Modification:**
    - Add `subtotal`, `taxes`, `discount`, `totalAmount` fields.
    - Add `referenceType` (String) and `referenceId` (Integer) to link back to an accepted `VendorQuotation`.

### 6.2. Implement Quote-to-Booking Conversion
- **Action:** Create the service logic that converts an accepted quote into a booking.
- **Service:** `BookingService` or `QuotationService`.
- **Logic:** When a quote is accepted, a new `Booking` record is created and pre-filled with all the details from the `VendorQuotation` (price, line items, vendor, customer). The `referenceId` and `referenceType` are set accordingly.