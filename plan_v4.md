# Plan v4: Remove "Os" Prefix - Comprehensive Naming Refactor

This document outlines the complete refactoring plan to remove the legacy "Os" (Other Service) prefix from all entities, repositories, services, controllers, mappers, and DTOs. This will result in cleaner, more readable, and consistent naming across the codebase.

---

## Strategy

### Database Table Names
**IMPORTANT:** To avoid database migration complexity, we will **keep existing table names** by explicitly setting `@Table(name = "...")` on renamed entities. This allows us to rename Java classes without affecting the database schema.

### Naming Convention
- Entity names should be **domain-focused** (e.g., `JobCard`, `BookingSlot`)
- Vendor-related scheduling entities get `Vendor` prefix (e.g., `VendorWorkHours`)
- Booking-related entities get `Booking` prefix where appropriate
- Remove redundant "Appointment" terminology - standardize on "Booking"

---

## Phase 1: Entity Renames (8 entities)

| # | Current Name | New Name | Table Name (Keep) | Notes |
|---|--------------|----------|-------------------|-------|
| 1.1 | `OsJobCard` | `JobCard` | `os_job_card` | Core job card entity |
| 1.2 | `OsAppointmentServices` | `BookedService` | `os_appointment_services` | Services booked in a booking |
| 1.3 | `OsAppointmentSlot` | `BookingSlot` | `os_appointment_slot` | Time slots for bookings |
| 1.4 | `OsAppointmentStatusActivity` | `BookingStatusActivity` | `os_appointment_status_activity` | Booking status history |
| 1.5 | `OsJobcardStatusActivity` | `JobCardStatusActivity` | `os_jobcard_status_activity` | Job card status history |
| 1.6 | `OsBreakTime` | `VendorBreakTime` | `os_break_time` | Vendor break time config |
| 1.7 | `OsWorkHours` | `VendorWorkHours` | `os_work_hours` | Vendor working hours |
| 1.8 | `OsHolidayOverride` | `VendorHolidayOverride` | `os_holiday_override` | Vendor holiday settings |

### Entity Changes Required

#### 1.1 OsJobCard → JobCard
- [x] Rename file: `OsJobCard.java` → `JobCard.java`
- [x] Add `@Table(name = "os_job_card")`
- [x] Update class name: `OsJobCard` → `JobCard`
- [x] Update field `OsAppointmentServices service` → `BookedService service`

#### 1.2 OsAppointmentServices → BookedService
- [x] Rename file: `OsAppointmentServices.java` → `BookedService.java`
- [x] Add `@Table(name = "os_appointment_services")`
- [x] Update class name: `OsAppointmentServices` → `BookedService`
- [x] Renamed field `appointmentId` → `bookingId`

#### 1.3 OsAppointmentSlot → BookingSlot
- [x] Rename file: `OsAppointmentSlot.java` → `BookingSlot.java`
- [x] Add `@Table(name = "os_appointment_slot")`
- [x] Update class name: `OsAppointmentSlot` → `BookingSlot`

#### 1.4 OsAppointmentStatusActivity → BookingStatusActivity
- [x] Rename file: `OsAppointmentStatusActivity.java` → `BookingStatusActivity.java`
- [x] Add `@Table(name = "os_appointment_status_activity")`
- [x] Update class name: `OsAppointmentStatusActivity` → `BookingStatusActivity`
- [x] Renamed field `osAppointmentId` → `bookingId`

#### 1.5 OsJobcardStatusActivity → JobCardStatusActivity
- [x] Rename file: `OsJobcardStatusActivity.java` → `JobCardStatusActivity.java`
- [x] Add `@Table(name = "os_jobcard_status_activity")`
- [x] Update class name: `OsJobcardStatusActivity` → `JobCardStatusActivity`

#### 1.6 OsBreakTime → VendorBreakTime
- [x] Rename file: `OsBreakTime.java` → `VendorBreakTime.java`
- [x] Add `@Table(name = "os_break_time")`
- [x] Update class name: `OsBreakTime` → `VendorBreakTime`

#### 1.7 OsWorkHours → VendorWorkHours
- [x] Rename file: `OsWorkHours.java` → `VendorWorkHours.java`
- [x] Add `@Table(name = "os_work_hours")`
- [x] Update class name: `OsWorkHours` → `VendorWorkHours`

#### 1.8 OsHolidayOverride → VendorHolidayOverride
- [x] Rename file: `OsHolidayOverride.java` → `VendorHolidayOverride.java`
- [x] Add `@Table(name = "os_holiday_override")`
- [x] Update class name: `OsHolidayOverride` → `VendorHolidayOverride`

---

## Phase 2: Repository Renames (8 repositories)

| # | Current Name | New Name |
|---|--------------|----------|
| 2.1 | `OsJobCardRepository` | `JobCardRepository` |
| 2.2 | `OsAppointmentServiceRepository` | `BookedServiceRepository` |
| 2.3 | `OsSlotRepository` | `BookingSlotRepository` |
| 2.4 | `OsAppointmentStatusActivityRepository` | `BookingStatusActivityRepository` |
| 2.5 | `OsJobcardStatusActivityRepository` | `JobCardStatusActivityRepository` |
| 2.6 | `OsBreakTimeRepository` | `VendorBreakTimeRepository` |
| 2.7 | `OsWorkHourRepository` | `VendorWorkHoursRepository` |
| 2.8 | `OsHolidayOverrideRepository` | `VendorHolidayOverrideRepository` |

### Repository Changes Required
+
- [x] 2.1 Rename `OsJobCardRepository.java` → `JobCardRepository.java`
  - Update interface: `extends JpaRepository<JobCard, Integer>`
- [x] 2.2 Rename `OsAppointmentServiceRepository.java` → `BookedServiceRepository.java`
  - Update interface: `extends JpaRepository<BookedService, Integer>`
- [x] 2.3 Rename `OsSlotRepository.java` → `BookingSlotRepository.java`
  - Update interface: `extends JpaRepository<BookingSlot, Integer>`
- [x] 2.4 Rename `OsAppointmentStatusActivityRepository.java` → `BookingStatusActivityRepository.java`
  - Update interface: `extends JpaRepository<BookingStatusActivity, Integer>`
- [x] 2.5 Rename `OsJobcardStatusActivityRepository.java` → `JobCardStatusActivityRepository.java`
  - Update interface: `extends JpaRepository<JobCardStatusActivity, Integer>`
- [x] 2.6 Rename `OsBreakTimeRepository.java` → `VendorBreakTimeRepository.java`
  - Update interface: `extends JpaRepository<VendorBreakTime, Long>`
- [x] 2.7 Rename `OsWorkHourRepository.java` → `VendorWorkHoursRepository.java`
  - Update interface: `extends JpaRepository<VendorWorkHours, Integer>`
- [x] 2.8 Rename `OsHolidayOverrideRepository.java` → `VendorHolidayOverrideRepository.java`
  - Update interface: `extends JpaRepository<VendorHolidayOverride, Long>`

---

## Phase 3: Service Renames (3 services)

| # | Current Name | New Name |
|---|--------------|----------|
| 3.1 | `OsJobCardService` | `JobCardService` |
| 3.2 | `OsSlotService` | `BookingSlotService` |
| 3.3 | `OsAppointmentSlotService` | `VendorScheduleService` |

### Service Changes Required

- [x] 3.1 Rename `OsJobCardService.java` → `JobCardService.java`
  - Update all method signatures using renamed entities
  - Update repository field names
- [x] 3.2 Rename `OsSlotService.java` → `BookingSlotService.java`
  - Update all method signatures using renamed entities
- [x] 3.3 Rename `OsAppointmentSlotService.java` → `VendorSlotService.java`
  - Update all method signatures using renamed entities

---

## Phase 4: Controller Renames (1 controller)

| # | Current Name | New Name | Endpoint |
|---|--------------|----------|----------|
| 4.1 | `OsJobCardController` | `JobCardController` | Keep `/os-jobcards` for backward compatibility |

### Controller Changes Required

- [x] 4.1 Rename `OsJobCardController.java` → `JobCardController.java`
  - Update service injection to use renamed services
  - **Note:** Keep existing endpoint paths for API backward compatibility

---

## Phase 5: Mapper Renames (4 mappers)

| # | Current Name | New Name |
|---|--------------|----------|
| 5.1 | `OsJobCardMapper` | `JobCardMapper` |
| 5.2 | `OsSlotMapper` | `BookingSlotMapper` |
| 5.3 | `OsAppointmentDetailMapper` | `BookingDetailMapper` |
| 5.4 | `OsServiceMapper` | `BookedServiceMapper` |

### Mapper Changes Required

- [x] 5.1 Rename `OsJobCardMapper.java` → `JobCardMapper.java`
  - Update entity references in mapping methods
- [x] 5.2 Rename `OsSlotMapper.java` → `BookingSlotMapper.java`
  - Update entity references in mapping methods
- [ ] 5.3 Rename `OsAppointmentDetailMapper.java` → `BookingDetailMapper.java`
  - Update entity references in mapping methods (kept original name for now)
- [ ] 5.4 Rename `OsServiceMapper.java` → `BookedServiceMapper.java`
  - Update entity references in mapping methods (file may not exist)

---

## Phase 6: Request DTO Renames (5 request DTOs)

| # | Current Name | New Name |
|---|--------------|----------|
| 6.1 | `OsJobCardStatusRequest` | `JobCardStatusRequest` |
| 6.2 | `OsAppointmentSlotRequest` | `BookingSlotRequest` |
| 6.3 | `OsAppointmentStatusRequest` | `BookingStatusRequest` |
| 6.4 | `OsAssignServiceRequest` | `AssignServiceRequest` |
| 6.5 | `OsCustomerAppointmentRequest` | `CustomerBookingRequest` |

### Request DTO Changes Required

- [ ] 6.1 Rename `OsJobCardStatusRequest.java` → `JobCardStatusRequest.java`
- [ ] 6.2 Rename `OsAppointmentSlotRequest.java` → `BookingSlotRequest.java`
- [ ] 6.3 Rename `OsAppointmentStatusRequest.java` → `BookingStatusRequest.java`
- [ ] 6.4 Rename `OsAssignServiceRequest.java` → `AssignServiceRequest.java`
- [ ] 6.5 Rename `OsCustomerAppointmentRequest.java` → `CustomerBookingRequest.java`

---

## Phase 7: Response DTO Renames (6 response DTOs)

| # | Current Name | New Name |
|---|--------------|----------|
| 7.1 | `OsJobCardResponse` | `JobCardResponse` |
| 7.2 | `OsAppointmentSlotResponse` | `BookingSlotResponse` |
| 7.3 | `OsAppointmentDetailResponse` | `BookingDetailResponse` |
| 7.4 | `OsAppointmentResponse` | `BookingDetailsResponse` |
| 7.5 | `OsMonthlyAvailabilityResponse` | `MonthlyAvailabilityResponse` |
| 7.6 | `OsGenericResponse` | `ApiResponse<T>` |

### Response DTO Changes Required

- [ ] 7.1 Rename `OsJobCardResponse.java` → `JobCardResponse.java`
- [ ] 7.2 Rename `OsAppointmentSlotResponse.java` → `BookingSlotResponse.java`
- [ ] 7.3 Rename `OsAppointmentDetailResponse.java` → `BookingDetailResponse.java`
- [ ] 7.4 Rename `OsAppointmentResponse.java` → `BookingDetailsResponse.java`
  - **Note:** Different from `BookingResponse` which is the simple DTO
- [ ] 7.5 Rename `OsMonthlyAvailabilityResponse.java` → `MonthlyAvailabilityResponse.java`
- [ ] 7.6 Convert `OsGenericResponse.java` → `ApiResponse.java` (generic typed)
  - Change `Object body` to generic type `T data`
  - Remove redundant `code` field (HTTP status is in ResponseEntity)

---

## Phase 8: Deprecated/Legacy Files (2 files)

| # | Current Name | Action |
|---|--------------|--------|
| 8.1 | `OsRequest.java` | Review and delete if unused |
| 8.2 | `OsResponse.java` | Review and delete if unused |

---

## Phase 9: Update All References

After renaming files, update all references in:

### 9.1 Entity References (update imports and field types)
- [x] `Booking.java` - Update `OsAppointmentSlot slot` → `BookingSlot slot`
- [x] `WorkOrder.java` - Update `List<OsJobCard> jobCards` → `List<JobCard> jobCards`
- [x] `JobCard.java` (new) - Update `OsAppointmentServices service` → `BookedService service`

### 9.2 Service Layer References
- [x] `BookingService.java` - Update all repository and entity references
- [x] `JobCardService.java` (new) - Update all entity references
- [x] `BookingSlotService.java` (new) - Update all entity references
- [x] `VendorSlotService.java` (new) - Update all entity references

### 9.3 Controller Layer References
- [x] `BookingController.java` - Update service and DTO references
- [x] `JobCardController.java` (new) - Update service and DTO references

### 9.4 Mapper References
- [x] `JobCardMapper.java` - Update entity references
- [x] `BookingSlotMapper.java` - Update entity references
- [x] `BookingMapper.java` - Update to use BookingSlotMapper
- [x] `OsAppointmentDetailMapper.java` - Update to use BookingSlotMapper

---

## Phase 10: Verification Checklist

After completing all renames:

- [x] Run `mvn clean compile` - Ensure no compilation errors (**PASSED**)
- [ ] Run `mvn test` - Ensure all tests pass
- [ ] Start application - Verify Hibernate creates/validates tables correctly
- [ ] Test API endpoints - Verify all endpoints still work
- [ ] Check Swagger/OpenAPI - Verify documentation is correct

---

## Summary: File Rename Matrix

### Entities (8 files)
```
entity/OsJobCard.java                    → entity/JobCard.java
entity/OsAppointmentServices.java        → entity/BookedService.java
entity/OsAppointmentSlot.java            → entity/BookingSlot.java
entity/OsAppointmentStatusActivity.java  → entity/BookingStatusActivity.java
entity/OsJobcardStatusActivity.java      → entity/JobCardStatusActivity.java
entity/OsBreakTime.java                  → entity/VendorBreakTime.java
entity/OsWorkHours.java                  → entity/VendorWorkHours.java
entity/OsHolidayOverride.java            → entity/VendorHolidayOverride.java
```

### Repositories (8 files)
```
repository/OsJobCardRepository.java                  → repository/JobCardRepository.java
repository/OsAppointmentServiceRepository.java       → repository/BookedServiceRepository.java
repository/OsSlotRepository.java                     → repository/BookingSlotRepository.java
repository/OsAppointmentStatusActivityRepository.java→ repository/BookingStatusActivityRepository.java
repository/OsJobcardStatusActivityRepository.java    → repository/JobCardStatusActivityRepository.java
repository/OsBreakTimeRepository.java                → repository/VendorBreakTimeRepository.java
repository/OsWorkHourRepository.java                 → repository/VendorWorkHoursRepository.java
repository/OsHolidayOverrideRepository.java          → repository/VendorHolidayOverrideRepository.java
```

### Services (3 files)
```
service/OsJobCardService.java         → service/JobCardService.java
service/OsSlotService.java            → service/BookingSlotService.java
service/OsAppointmentSlotService.java → service/VendorScheduleService.java
```

### Controllers (1 file)
```
controller/OsJobCardController.java → controller/JobCardController.java
```

### Mappers (4 files)
```
mapper/OsJobCardMapper.java           → mapper/JobCardMapper.java
mapper/OsSlotMapper.java              → mapper/BookingSlotMapper.java
mapper/OsAppointmentDetailMapper.java → mapper/BookingDetailMapper.java
mapper/OsServiceMapper.java           → mapper/BookedServiceMapper.java
```

### Request DTOs (5 files)
```
request/OsJobCardStatusRequest.java        → request/JobCardStatusRequest.java
request/OsAppointmentSlotRequest.java      → request/BookingSlotRequest.java
request/OsAppointmentStatusRequest.java    → request/BookingStatusRequest.java
request/OsAssignServiceRequest.java        → request/AssignServiceRequest.java
request/OsCustomerAppointmentRequest.java  → request/CustomerBookingRequest.java
```

### Response DTOs (6 files)
```
response/OsJobCardResponse.java             → response/JobCardResponse.java
response/OsAppointmentSlotResponse.java     → response/BookingSlotResponse.java
response/OsAppointmentDetailResponse.java   → response/BookingDetailResponse.java
response/OsAppointmentResponse.java         → response/BookingDetailsResponse.java
response/OsMonthlyAvailabilityResponse.java → response/MonthlyAvailabilityResponse.java
response/OsGenericResponse.java             → response/ApiResponse.java
```

### To Review/Delete (2 files)
```
request/OsRequest.java   → DELETE if unused
response/OsResponse.java → DELETE if unused
```

---

## Total Files to Modify

| Category | Count |
|----------|-------|
| Entities | 8 |
| Repositories | 8 |
| Services | 3 |
| Controllers | 1 |
| Mappers | 4 |
| Request DTOs | 5 |
| Response DTOs | 6 |
| Files with references | ~15 |
| **Total** | **~50 files** |

---

## Recommended Execution Order

1. **Phase 1**: Rename entities first (add @Table annotations)
2. **Phase 2**: Rename repositories (update generic types)
3. **Phase 3**: Rename services (update injections)
4. **Phase 6 & 7**: Rename DTOs (request & response)
5. **Phase 5**: Rename mappers (update all mappings)
6. **Phase 4**: Rename controllers (update injections)
7. **Phase 9**: Update all cross-references
8. **Phase 8**: Clean up deprecated files
9. **Phase 10**: Verify everything works

---

## Risk Mitigation

1. **Create a backup branch** before starting
2. **Commit after each phase** for easy rollback
3. **Keep database table names unchanged** to avoid migration
4. **Keep API endpoint paths unchanged** for backward compatibility
5. **Run tests after each phase** to catch issues early
