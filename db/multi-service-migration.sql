-- ============================================================================
--  Multi-service vendor remodel — DB migration (POST-BOOT recovery version)
--
--  Use this when the new jar has ALREADY been deployed once, so Hibernate
--  `ddl-auto=update` has already:
--    * created an empty `vendor_work_hours` next to the old `os_work_hours`
--    * added `vendor_service_id` to vendor_break_time / vendor_holiday_override
--      / booking_slot / booking
--    * added the new columns to `vendor_service_bridge` and `media_file`
--    * added `selected_vendor_service` / `subscription_plan_id` to `booking`
--      and `booking_id` to `booking_status_activity`
--  ...while the OLD columns / tables still hold the real data.
--
--  This script fills the new structures from the old ones and drops the old.
--  Back up the database first. Run the whole thing in one execution.
-- ============================================================================

SET FOREIGN_KEY_CHECKS = 0;
SET SQL_SAFE_UPDATES = 0;

-- ---------------------------------------------------------------------------
-- 1. Build a "primary" vendor_service row per existing vendor from its
--    vendor.service_type, and copy the operating fields onto it.
--    (bridge rows normally already exist from onboarding; INSERT covers gaps.)
-- ---------------------------------------------------------------------------
INSERT INTO vendor_service_bridge (vendor_id, service_category_id, is_active)
SELECT v.id, sc.id, 1
FROM vendor v
JOIN service_category sc ON sc.name = CASE v.service_type
        WHEN 'TYRE_AND_BALANCING' THEN 'Tyre & Balancing' WHEN 'SUSPENSION' THEN 'Suspension'
        WHEN 'BATTERY' THEN 'Battery' WHEN 'RECOVERY' THEN 'Recovery' WHEN 'INSURANCE' THEN 'Insurance'
        WHEN 'CAR_INSPECTION' THEN 'Car Inspection' WHEN 'QUALITY_CONTROL' THEN 'Quality Control'
        WHEN 'ENGINEERING_WORK' THEN 'Engineering Work' WHEN 'HYDRAULIC_HOSE' THEN 'Hydraulic Hose'
        WHEN 'PART_REPAIRING' THEN 'Part Repairing' WHEN 'ACCESSORIES' THEN 'Accessories'
        WHEN 'UPHOLSTERY' THEN 'Upholstery' WHEN 'CAR_WASH' THEN 'Car Wash' WHEN 'CAR_DETAILING' THEN 'Car Detailing'
        WHEN 'MODIFICATION' THEN 'Modification' WHEN 'GLASS_SERVICES' THEN 'Glass Services'
        WHEN 'OIL_CHANGE' THEN 'Oil Change' WHEN 'FUEL_SERVICES' THEN 'Fuel Services'
        WHEN 'EV_SERVICES' THEN 'EV Services' WHEN 'TOOLS' THEN 'Tools' WHEN 'DELIVERY' THEN 'Delivery' END
WHERE v.service_type IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM vendor_service_bridge b
                  WHERE b.vendor_id = v.id AND b.service_category_id = sc.id);

-- primary bridge (matches vendor.service_type) gets the full copy
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

-- any remaining bridges (extra categories) get the vendor defaults + a type
UPDATE vendor_service_bridge b
JOIN vendor v ON v.id = b.vendor_id
SET b.service_type = COALESCE(b.service_type, v.service_type),
    b.opening_time = COALESCE(b.opening_time, v.opening_time),
    b.closing_time = COALESCE(b.closing_time, v.closing_time),
    b.working_days = COALESCE(b.working_days, v.working_days),
    b.concurrent_appointment_capacity = COALESCE(b.concurrent_appointment_capacity, v.concurrent_appointment_capacity),
    b.created_at = COALESCE(b.created_at, NOW())
WHERE b.service_type IS NULL;

-- ---------------------------------------------------------------------------
-- 2. os_work_hours (old, has data) -> vendor_work_hours (new, empty)
--    Skip this whole block if `os_work_hours` no longer exists.
-- ---------------------------------------------------------------------------
INSERT INTO vendor_work_hours (vendor_service_id, day_of_week, open_time, close_time, is_closed, slot_duration_minutes)
SELECT b.id, w.day_of_week, w.open_time, w.close_time, w.is_closed,
       COALESCE(w.slot_duration_minutes, 20)
FROM os_work_hours w
JOIN vendor v ON v.id = w.vendor_id
JOIN vendor_service_bridge b ON b.vendor_id = v.id AND b.service_type = v.service_type
WHERE NOT EXISTS (
    SELECT 1 FROM vendor_work_hours nw
    WHERE nw.vendor_service_id = b.id AND nw.day_of_week = w.day_of_week
);

DROP TABLE IF EXISTS os_work_hours;

-- ---------------------------------------------------------------------------
-- 3. Backfill vendor_service_id on the tables Hibernate extended in place
-- ---------------------------------------------------------------------------
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

-- ---------------------------------------------------------------------------
-- 4. bookings: vendor_service_id from the booked slot, else the vendor's primary
-- ---------------------------------------------------------------------------
UPDATE booking bk
JOIN booking_slot s ON s.id = bk.slot_id
SET bk.vendor_service_id = s.vendor_service_id
WHERE bk.vendor_service_id IS NULL AND s.vendor_service_id IS NOT NULL;

UPDATE booking bk
JOIN vendor v ON v.id = bk.vendor_id
JOIN vendor_service_bridge b ON b.vendor_id = v.id AND b.service_type = v.service_type
SET bk.vendor_service_id = b.id
WHERE bk.vendor_service_id IS NULL;

-- ---------------------------------------------------------------------------
-- 5. copy renamed columns old -> new, then drop the old ones
-- ---------------------------------------------------------------------------
UPDATE booking
SET selected_vendor_service = selected_other_service
WHERE selected_vendor_service IS NULL AND selected_other_service IS NOT NULL;

UPDATE booking
SET subscription_plan_id = os_subscription_plan_id
WHERE subscription_plan_id IS NULL AND os_subscription_plan_id IS NOT NULL;

ALTER TABLE booking
  DROP COLUMN IF EXISTS selected_other_service,
  DROP COLUMN IF EXISTS os_subscription_plan_id;

UPDATE booking_status_activity
SET booking_id = os_appointment_id
WHERE booking_id IS NULL AND os_appointment_id IS NOT NULL;

ALTER TABLE booking_status_activity DROP COLUMN IF EXISTS os_appointment_id;

-- ---------------------------------------------------------------------------
-- 6. per-service media references
-- ---------------------------------------------------------------------------
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
-- 7. drop the UNIQUE index on vendor.trade_licence_number
--    (trade licence is per-service now and may repeat / be null)
--    Find the index name:
--      SELECT INDEX_NAME FROM information_schema.STATISTICS
--       WHERE TABLE_NAME='vendor' AND COLUMN_NAME='trade_licence_number' AND NON_UNIQUE=0;
-- ---------------------------------------------------------------------------
-- ALTER TABLE vendor DROP INDEX <index_name>;

-- ---------------------------------------------------------------------------
-- 8. car_wash_details — if Hibernate created it fresh with a vendor_service_id
--    PK, nothing to do. If it has an old vendor_id PK with rows, repoint:
--      ALTER TABLE car_wash_details ADD COLUMN vendor_service_id INT NULL;
--      UPDATE car_wash_details d JOIN vendor_service_bridge b
--        ON b.vendor_id = d.vendor_id AND b.service_type = 'CAR_WASH'
--        SET d.vendor_service_id = b.id;
--      ALTER TABLE car_wash_details DROP FOREIGN KEY <fk>, DROP PRIMARY KEY,
--        ADD PRIMARY KEY (vendor_service_id), DROP COLUMN vendor_id;
-- ---------------------------------------------------------------------------

SET FOREIGN_KEY_CHECKS = 1;
SET SQL_SAFE_UPDATES = 1;

-- ============================================================================
-- 9. VERIFY
-- ============================================================================
-- SELECT COUNT(*) FROM vendor_work_hours   WHERE vendor_service_id IS NULL;  -- 0
-- SELECT COUNT(*) FROM vendor_break_time   WHERE vendor_service_id IS NULL;  -- 0
-- SELECT COUNT(*) FROM booking_slot        WHERE vendor_service_id IS NULL;  -- 0
-- SELECT COUNT(*) FROM booking WHERE vendor_service_id IS NULL AND slot_id IS NOT NULL;  -- 0
-- SELECT id, vendor_id, service_type, opening_time, concurrent_appointment_capacity
--   FROM vendor_service_bridge LIMIT 20;
-- vendors whose service_type didn't map to a category (need a manual bridge):
-- SELECT id, service_type FROM vendor WHERE service_type IS NOT NULL
--   AND id NOT IN (SELECT vendor_id FROM vendor_service_bridge WHERE service_type IS NOT NULL);

-- ============================================================================
-- 10. CLEANUP — only after the app is verified on the new schema
-- ============================================================================
-- ALTER TABLE vendor
--   DROP COLUMN service_type, DROP COLUMN opening_time, DROP COLUMN closing_time,
--   DROP COLUMN working_days, DROP COLUMN concurrent_appointment_capacity,
--   DROP COLUMN trade_licence_number;
-- ALTER TABLE vendor_break_time       DROP FOREIGN KEY <fk>, DROP COLUMN vendor_id;
-- ALTER TABLE vendor_holiday_override DROP FOREIGN KEY <fk>, DROP COLUMN vendor_id;
-- ALTER TABLE booking_slot            DROP FOREIGN KEY <fk>, DROP COLUMN vendor_id;
-- ALTER TABLE media_file DROP COLUMN company_trade_license, DROP COLUMN company_images;
-- keep booking.vendor_id (still mapped, always populated)
