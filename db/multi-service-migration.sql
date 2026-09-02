-- ============================================================================
--  Multi-service vendor remodel — manual DB migration
--
--  Run this ONCE, against `auto_adda_other_service`, BEFORE deploying the new
--  jar. `ddl-auto=update` only ADDS columns; it cannot rename tables/columns,
--  change a primary key, or drop an index — those are all done here.
--
--  Back up the database first.
-- ============================================================================

SET FOREIGN_KEY_CHECKS = 0;
-- MySQL Workbench "safe update mode" blocks UPDATEs whose WHERE has no key column.
SET SQL_SAFE_UPDATES = 0;

-- ---------------------------------------------------------------------------
-- 1. os / other-service naming  ->  vendor / booking
-- ---------------------------------------------------------------------------
RENAME TABLE os_work_hours TO vendor_work_hours;

ALTER TABLE booking
  CHANGE COLUMN selected_other_service   selected_vendor_service   VARCHAR(255),
  CHANGE COLUMN os_subscription_plan_id  subscription_plan_id      VARCHAR(255);

ALTER TABLE booking_status_activity
  CHANGE COLUMN os_appointment_id        booking_id                INT;

-- ---------------------------------------------------------------------------
-- 2. Drop the UNIQUE index on vendor.trade_licence_number
--    (trade licence is now per-service and may be shared / null)
--    Find the real index name first:
--      SELECT INDEX_NAME FROM information_schema.STATISTICS
--       WHERE TABLE_NAME='vendor' AND COLUMN_NAME='trade_licence_number' AND NON_UNIQUE=0;
-- ---------------------------------------------------------------------------
-- ALTER TABLE vendor DROP INDEX <index_name>;

-- ---------------------------------------------------------------------------
-- 3. New per-service columns on vendor_service_bridge
--    (ddl-auto also adds these on boot — listed here for a manual run)
-- ---------------------------------------------------------------------------
ALTER TABLE vendor_service_bridge
  ADD COLUMN service_type                     VARCHAR(64)  NULL,
  ADD COLUMN opening_time                     VARCHAR(32)  NULL,
  ADD COLUMN closing_time                     VARCHAR(32)  NULL,
  ADD COLUMN working_days                     VARCHAR(255) NULL,
  ADD COLUMN concurrent_appointment_capacity  INT          NULL,
  ADD COLUMN trade_licence_number             VARCHAR(255) NULL,
  ADD COLUMN service_location_id              INT          NULL,
  ADD COLUMN account_status                   INT          NULL,
  ADD COLUMN created_at                       DATETIME(6)  NULL;

-- new vendor_service_id FK columns on the scheduling tables + booking
ALTER TABLE vendor_work_hours       ADD COLUMN vendor_service_id INT NULL;
ALTER TABLE vendor_break_time       ADD COLUMN vendor_service_id INT NULL;
ALTER TABLE vendor_holiday_override ADD COLUMN vendor_service_id INT NULL;
ALTER TABLE booking_slot            ADD COLUMN vendor_service_id INT NULL;
ALTER TABLE booking                 ADD COLUMN vendor_service_id INT NULL;

-- media-file FK columns for the per-service galleries
ALTER TABLE media_file
  ADD COLUMN service_trade_license INT NULL,
  ADD COLUMN service_images        INT NULL;

-- ---------------------------------------------------------------------------
-- 4. Backfill — build one "primary" vendor_service row per existing vendor
--    from vendor.service_type, copy the operating fields onto it, then
--    re-point the scheduling rows and bookings.
-- ---------------------------------------------------------------------------

-- 4a. ensure a bridge row exists for each vendor's own service_type category
INSERT INTO vendor_service_bridge (vendor_id, service_category_id, is_active)
SELECT v.id, sc.id, 1
FROM vendor v
JOIN service_category sc
  ON sc.name = CASE v.service_type
        WHEN 'TYRE_AND_BALANCING' THEN 'Tyre & Balancing'
        WHEN 'SUSPENSION'         THEN 'Suspension'
        WHEN 'BATTERY'            THEN 'Battery'
        WHEN 'RECOVERY'           THEN 'Recovery'
        WHEN 'INSURANCE'          THEN 'Insurance'
        WHEN 'CAR_INSPECTION'     THEN 'Car Inspection'
        WHEN 'QUALITY_CONTROL'    THEN 'Quality Control'
        WHEN 'ENGINEERING_WORK'   THEN 'Engineering Work'
        WHEN 'HYDRAULIC_HOSE'     THEN 'Hydraulic Hose'
        WHEN 'PART_REPAIRING'     THEN 'Part Repairing'
        WHEN 'ACCESSORIES'        THEN 'Accessories'
        WHEN 'UPHOLSTERY'         THEN 'Upholstery'
        WHEN 'CAR_WASH'           THEN 'Car Wash'
        WHEN 'CAR_DETAILING'     THEN 'Car Detailing'
        WHEN 'MODIFICATION'       THEN 'Modification'
        WHEN 'GLASS_SERVICES'     THEN 'Glass Services'
        WHEN 'OIL_CHANGE'         THEN 'Oil Change'
        WHEN 'FUEL_SERVICES'      THEN 'Fuel Services'
        WHEN 'EV_SERVICES'        THEN 'EV Services'
        WHEN 'TOOLS'              THEN 'Tools'
        WHEN 'DELIVERY'           THEN 'Delivery'
     END
WHERE v.service_type IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM vendor_service_bridge b
                  WHERE b.vendor_id = v.id AND b.service_category_id = sc.id);

-- 4b. copy the operating fields from vendor onto its primary bridge
UPDATE vendor_service_bridge b
JOIN vendor v ON v.id = b.vendor_id
JOIN service_category sc ON sc.id = b.service_category_id AND sc.name = CASE v.service_type
        WHEN 'TYRE_AND_BALANCING' THEN 'Tyre & Balancing' WHEN 'SUSPENSION' THEN 'Suspension'
        WHEN 'BATTERY' THEN 'Battery' WHEN 'RECOVERY' THEN 'Recovery' WHEN 'INSURANCE' THEN 'Insurance'
        WHEN 'CAR_INSPECTION' THEN 'Car Inspection' WHEN 'QUALITY_CONTROL' THEN 'Quality Control'
        WHEN 'ENGINEERING_WORK' THEN 'Engineering Work' WHEN 'HYDRAULIC_HOSE' THEN 'Hydraulic Hose'
        WHEN 'PART_REPAIRING' THEN 'Part Repairing' WHEN 'ACCESSORIES' THEN 'Accessories'
        WHEN 'UPHOLSTERY' THEN 'Upholstery' WHEN 'CAR_WASH' THEN 'Car Wash' WHEN 'CAR_DETAILING' THEN 'Car Detailing'
        WHEN 'MODIFICATION' THEN 'Modification' WHEN 'GLASS_SERVICES' THEN 'Glass Services'
        WHEN 'OIL_CHANGE' THEN 'Oil Change' WHEN 'FUEL_SERVICES' THEN 'Fuel Services'
        WHEN 'EV_SERVICES' THEN 'EV Services' WHEN 'TOOLS' THEN 'Tools' WHEN 'DELIVERY' THEN 'Delivery' END
SET b.service_type = v.service_type,
    b.opening_time = v.opening_time,
    b.closing_time = v.closing_time,
    b.working_days = v.working_days,
    b.concurrent_appointment_capacity = v.concurrent_appointment_capacity,
    b.trade_licence_number = v.trade_licence_number,
    b.service_location_id  = v.company_location_id,
    b.account_status = v.account_status,
    b.created_at = COALESCE(b.created_at, v.created_at, NOW())
WHERE b.service_type IS NULL;

-- fill secondary bridges (extra categories) with the vendor defaults + a type
UPDATE vendor_service_bridge b
JOIN vendor v ON v.id = b.vendor_id
SET b.service_type = COALESCE(b.service_type, v.service_type),
    b.opening_time = COALESCE(b.opening_time, v.opening_time),
    b.closing_time = COALESCE(b.closing_time, v.closing_time),
    b.working_days = COALESCE(b.working_days, v.working_days),
    b.concurrent_appointment_capacity = COALESCE(b.concurrent_appointment_capacity, v.concurrent_appointment_capacity)
WHERE b.service_type IS NULL;

-- 4c. re-point scheduling rows: vendor_id -> the vendor's primary vendor_service id
UPDATE vendor_work_hours w
JOIN vendor v ON v.id = w.vendor_id
JOIN vendor_service_bridge b ON b.vendor_id = v.id AND b.service_type = v.service_type
SET w.vendor_service_id = b.id
WHERE w.vendor_service_id IS NULL;

UPDATE vendor_break_time t
JOIN vendor v ON v.id = t.vendor_id
JOIN vendor_service_bridge b ON b.vendor_id = v.id AND b.service_type = v.service_type
SET t.vendor_service_id = b.id
WHERE t.vendor_service_id IS NULL;

UPDATE vendor_holiday_override h
JOIN vendor v ON v.id = h.vendor_id
JOIN vendor_service_bridge b ON b.vendor_id = v.id AND b.service_type = v.service_type
SET h.vendor_service_id = b.id
WHERE h.vendor_service_id IS NULL;

UPDATE booking_slot s
JOIN vendor v ON v.id = s.vendor_id
JOIN vendor_service_bridge b ON b.vendor_id = v.id AND b.service_type = v.service_type
SET s.vendor_service_id = b.id
WHERE s.vendor_service_id IS NULL;

-- 4d. bookings: derive vendor_service_id from the booked slot, else the vendor's primary service
UPDATE booking bk
JOIN booking_slot s ON s.id = bk.slot_id
SET bk.vendor_service_id = s.vendor_service_id
WHERE bk.vendor_service_id IS NULL AND s.vendor_service_id IS NOT NULL;

UPDATE booking bk
JOIN vendor v ON v.id = bk.vendor_id
JOIN vendor_service_bridge b ON b.vendor_id = v.id AND b.service_type = v.service_type
SET bk.vendor_service_id = b.id
WHERE bk.vendor_service_id IS NULL;

-- 4e. move per-service media references off the vendor columns
UPDATE media_file m
JOIN vendor v ON v.id = m.company_trade_license
JOIN vendor_service_bridge b ON b.vendor_id = v.id AND b.service_type = v.service_type
SET m.service_trade_license = b.id
WHERE m.service_trade_license IS NULL;

UPDATE media_file m
JOIN vendor v ON v.id = m.company_images
JOIN vendor_service_bridge b ON b.vendor_id = v.id AND b.service_type = v.service_type
SET m.service_images = b.id
WHERE m.service_images IS NULL;

-- ---------------------------------------------------------------------------
-- 5. car_wash_details PK: vendor_id -> vendor_service_id  (only if it has rows)
-- ---------------------------------------------------------------------------
-- ALTER TABLE car_wash_details ADD COLUMN vendor_service_id INT NULL;
-- UPDATE car_wash_details d
--   JOIN vendor_service_bridge b ON b.vendor_id = d.vendor_id AND b.service_type = 'CAR_WASH'
--   SET d.vendor_service_id = b.id;
-- -- drop the old FK (name via information_schema.KEY_COLUMN_USAGE), then:
-- ALTER TABLE car_wash_details DROP PRIMARY KEY, ADD PRIMARY KEY (vendor_service_id);
-- ALTER TABLE car_wash_details DROP COLUMN vendor_id;
-- ALTER TABLE car_wash_details
--   ADD CONSTRAINT fk_cwd_vs FOREIGN KEY (vendor_service_id) REFERENCES vendor_service_bridge(id);
-- If car_wash_details is empty:  DROP TABLE car_wash_details;

SET FOREIGN_KEY_CHECKS = 1;
SET SQL_SAFE_UPDATES = 1;

-- ============================================================================
-- 6. CLEANUP — run only after the app has been verified on the new schema
-- ============================================================================
-- ALTER TABLE vendor
--   DROP COLUMN service_type, DROP COLUMN opening_time, DROP COLUMN closing_time,
--   DROP COLUMN working_days, DROP COLUMN concurrent_appointment_capacity,
--   DROP COLUMN trade_licence_number;
-- ALTER TABLE vendor_work_hours       DROP FOREIGN KEY <fk>, DROP COLUMN vendor_id;
-- ALTER TABLE vendor_break_time       DROP FOREIGN KEY <fk>, DROP COLUMN vendor_id;
-- ALTER TABLE vendor_holiday_override DROP FOREIGN KEY <fk>, DROP COLUMN vendor_id;
-- ALTER TABLE booking_slot            DROP FOREIGN KEY <fk>, DROP COLUMN vendor_id;
-- media_file: DROP COLUMN company_trade_license, DROP COLUMN company_images;
-- keep booking.vendor_id (still mapped, always populated)
