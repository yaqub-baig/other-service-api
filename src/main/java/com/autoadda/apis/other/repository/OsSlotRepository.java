package com.autoadda.apis.other.repository;

import com.autoadda.apis.other.entity.OsAppointmentSlot;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OsSlotRepository extends JpaRepository<OsAppointmentSlot, Integer> {

    List<OsAppointmentSlot> findByVendorIdAndSlotDateGreaterThanEqual(Integer vendorId, LocalDate date);

    // Find slots within a time range
    List<OsAppointmentSlot> findByStartTimeBetween(LocalTime startTime, LocalTime endTime);

    // Find available slots within a time range (currentBookings < maxBookings)
    @Query("SELECT s FROM OsAppointmentSlot s WHERE s.startTime BETWEEN :startTime AND :endTime AND s.currentBookings < s.maxBookings")
    List<OsAppointmentSlot> findAvailableSlotsBetween(@Param("startTime") LocalTime startTime,
                                                      @Param("endTime") LocalTime endTime);

    // Find all available slots (currentBookings < maxBookings)
    @Query("SELECT s FROM OsAppointmentSlot s WHERE s.currentBookings < s.maxBookings")
    List<OsAppointmentSlot> findAvailableSlots();

    // Find slots by start time
    List<OsAppointmentSlot> findByStartTime(LocalTime startTime);

    // Find available slots by start time (currentBookings < maxBookings)
    @Query("SELECT s FROM OsAppointmentSlot s WHERE s.startTime = :startTime AND s.currentBookings < s.maxBookings")
    List<OsAppointmentSlot> findAvailableSlotsByStartTime(@Param("startTime") LocalTime startTime);

    Optional<OsAppointmentSlot> findBySlotDateAndStartTime(LocalDate date, LocalTime currentTime);

    List<OsAppointmentSlot> findByVendorIdAndSlotDate(Integer vendorId, LocalDate date);

    @Query("SELECT s FROM OsAppointmentSlot s WHERE s.vendor.id = :vendorId AND s.slotDate= :slotDate AND s.startTime = :time AND s.currentBookings < s.maxBookings")
    Optional<OsAppointmentSlot> findByVendorAndStartTime(@Param("vendorId") Integer vendorId, @Param("slotDate") LocalDate date, @Param("time") LocalTime time);

    @Query("SELECT s FROM OsAppointmentSlot s WHERE s.vendor.id = :vendorId " +
            "AND s.slotDate >= :startDate AND s.slotDate <= :endDate " +
            "ORDER BY s.slotDate, s.startTime")
    List<OsAppointmentSlot> findMonthlySlots(
            @Param("vendorId") Integer vendorId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    List<OsAppointmentSlot> findAvailableSlotsByVendorId(Integer vendorId);

    Optional<OsAppointmentSlot> findByVendorIdAndSlotDateAndStartTime(Integer id, LocalDate date, LocalTime currentTime);

    @Query("SELECT s FROM OsAppointmentSlot s WHERE " +
            "(s.slotDate < :currentDate) OR " +
            "(s.slotDate = :currentDate AND s.endTime <= :currentTime)")
    List<OsAppointmentSlot> findPastSlots(@Param("currentDate") LocalDate currentDate,
                                          @Param("currentTime") LocalTime currentTime);
}
