package com.autoadda.apis.other.service;

import com.autoadda.apis.other.entity.Booking;
import com.autoadda.apis.other.entity.OsAppointmentServices;
import com.autoadda.apis.other.entity.OsAppointmentStatusActivity;
import com.autoadda.apis.other.entity.OsJobCard;
import com.autoadda.apis.other.entity.Vendor;
import com.autoadda.apis.other.entity.VendorService;
import com.autoadda.apis.other.exception.CustomerNotFoundException;
import com.autoadda.apis.other.exception.GarageNotFoundException;
import com.autoadda.apis.other.exception.OtherServiceNotFoundException;
import com.autoadda.apis.other.mapper.OsAppointmentDetailMapper;
import com.autoadda.apis.other.repository.*;
import com.autoadda.apis.other.request.OsAppointmentStatusRequest;
import com.autoadda.apis.other.request.OsCustomerAppointmentRequest;
import com.autoadda.apis.other.response.OsAppointmentDetailResponse;
import com.autoadda.apis.other.response.OsAppointmentResponse;
import com.autoadda.apis.other.util.BusinessValidator;
import com.autoadda.apis.other.util.DateHelper;
import com.autoadda.apis.other.util.Status;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final OsAppointmentStatusActivityRepository appointmentActivityRepo;
    private final OsAppointmentServiceRepository serviceRepo;
    private final VendorRepository vendorRepository;
    private final VendorServiceRepository vendorServiceRepo;
    private final OsJobCardService jobCardService;
    private final OsAppointmentDetailMapper appointmentDetailMapper;
    private final BusinessValidator validator;

    /**
     * Creates a new customer booking.
     *
     * @param request the booking request containing details like vendor ID,
     *                customer ID, vehicle ID, schedule date, service IDs, etc.
     * @return the created Booking object.
     * @throws CustomerNotFoundException     if the customer does not exist.
     * @throws OtherServiceNotFoundException if the vendor does not exist.
     */
    @Transactional
    public Booking createCustomerBooking(OsCustomerAppointmentRequest request)
            throws CustomerNotFoundException, OtherServiceNotFoundException {

        // Vendor is mandatory
        Vendor vendor = vendorRepository.findById(request.getOtherServiceId())
                .orElseThrow(() -> new OtherServiceNotFoundException("Vendor not found"));

        // Create booking
        Booking booking = Booking.builder()
                .vendor(vendor)
                .vehicleId(request.getVehicleId())
                .customerId(request.getCustomerId())
                .scheduleDate(request.getScheduleDate())
                .status(Status.CONFIRMED)
                .insert_tms(DateHelper.dbDate())
                .build();
        bookingRepository.save(booking);

        // Save activity
        appointmentActivityRepo.save(OsAppointmentStatusActivity.builder()
                .osAppointmentId(booking.getId())
                .status(booking.getStatus())
                .memberId(request.getMemberId())
                .memberType(request.getMemberType())
                .insert_tms(DateHelper.dbDate())
                .build());

        // Services optional
        if (request.getServiceIds() != null && !request.getServiceIds().isEmpty()) {
            List<OsAppointmentServices> services = request.getServiceIds().stream()
                    .filter(Objects::nonNull)
                    .map(serviceId -> {
                        VendorService vendorService = vendorServiceRepo.findById(serviceId)
                                .orElseThrow(() -> new RuntimeException("Service not found"));
                        return OsAppointmentServices.builder()
                                .appointmentId(booking.getId())
                                .vendorService(vendorService)
                                .status(Status.NEW)
                                .build();
                    })
                    .collect(Collectors.toList());

            serviceRepo.saveAll(services);
        }

        return booking;
    }

    /**
     * Retrieves all bookings for a vendor, optionally filtered by status.
     *
     * @param vendorId the ID of the vendor.
     * @param status   the status to filter bookings by, or null for all.
     * @return a list of Booking for the vendor.
     * @throws OtherServiceNotFoundException if the vendor does not exist.
     */
    @Transactional(readOnly = true)
    public List<Booking> getVendorBookings(Integer vendorId, Status status)
            throws OtherServiceNotFoundException {

        Vendor vendor = validator.validateAndGetVendor(vendorId);

        if (StringUtils.isEmpty(status.toString())) {
            return bookingRepository.findByVendorId(vendorId);
        } else {
            return bookingRepository.findByVendorIdAndStatus(vendorId, status);
        }
    }

    /**
     * Retrieves the details of a booking, including services and job cards.
     *
     * @param bookingId the ID of the booking.
     * @return a BookingResponse containing booking details, services, and job cards.
     */
    @Transactional(readOnly = true)
    public OsAppointmentResponse getBookingDetails(Integer bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        List<OsAppointmentServices> services = serviceRepo.findByAppointmentId(bookingId);
        List<OsJobCard> jobCards = jobCardService.getJobCardsByAppointmentId(bookingId);

        return OsAppointmentResponse.builder()
                .appointment(booking)
                .services(services)
                .jobcards(jobCards)
                .build();
    }

    /**
     * Updates the status of a booking.
     *
     * @param request the request containing booking ID, new status, comments,
     *                and member details.
     * @return the updated Booking object.
     */
    @Transactional
    public Booking updateBookingStatus(OsAppointmentStatusRequest request) {
        Booking booking = bookingRepository.findById(request.getAppointmentId())
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        Status newStatus = request.getStatus();

        booking.setStatus(newStatus);
        if (newStatus.equals(Status.WORK_INPROGRESS)) {
            booking.setServiceStartTime(DateHelper.dbTime());
        } else if (newStatus.equals(Status.COMPLETED)) {
            booking.setServiceEndTime(DateHelper.dbTime());
        } else if (newStatus.equals(Status.DELIVERED)) {
            booking.setStatus(Status.DONE);
            booking.setDeliveryTime(DateHelper.dbTime());
        }

        booking.setComments(request.getComments());
        bookingRepository.save(booking);

        appointmentActivityRepo.save(OsAppointmentStatusActivity.builder()
                .osAppointmentId(booking.getId())
                .status(booking.getStatus())
                .memberId(request.getMemberId())
                .memberType(request.getMemberType())
                .comments(request.getComments())
                .insert_tms(DateHelper.dbDate())
                .build());

        return booking;
    }

    /**
     * Retrieves all bookings for a vendor, optionally filtered by status.
     *
     * @param vendorId  the ID of the vendor.
     * @param statusStr the status to filter bookings by, or null for all.
     * @return a list of Booking for the vendor.
     * @throws GarageNotFoundException if the vendor does not exist.
     */
    @Transactional(readOnly = true)
    public List<OsAppointmentDetailResponse> getBookingsByVendor(Integer vendorId, String statusStr)
            throws GarageNotFoundException {
        validator.validateAndGetVendor(vendorId);

        if (StringUtils.isBlank(statusStr)) {
            return bookingRepository.findByVendorId(vendorId).stream()
                    .map(appointmentDetailMapper::entityToResponse)
                    .toList();
        }

        Status status = Status.valueOf(statusStr.toUpperCase());
        return bookingRepository.findByVendorIdAndStatus(vendorId, status).stream()
                .map(appointmentDetailMapper::entityToResponse)
                .toList();
    }

    public String generateBookingCode() {
        LocalDateTime now = LocalDateTime.now();
        String yearMonth = now.format(DateTimeFormatter.ofPattern("yyMM"));

        String codePrefix = "BKG-" + yearMonth + "-";

        List<String> latestCodes = bookingRepository.findLatestCodesByPrefix(codePrefix);

        int nextSequence = 1;
        if (!latestCodes.isEmpty()) {
            String latestCode = latestCodes.get(0);
            try {
                String sequencePart = latestCode.substring(codePrefix.length());
                nextSequence = Integer.parseInt(sequencePart) + 1;
            } catch (NumberFormatException | IndexOutOfBoundsException e) {
                log.warn("Failed to parse sequence number, starting from 1", e);
            }
        }

        return String.format("%s%06d", codePrefix, nextSequence);
    }

    // Legacy method names for backward compatibility
    @Deprecated
    public Booking createCustomerAppointment(OsCustomerAppointmentRequest request) {
        return createCustomerBooking(request);
    }

    @Deprecated
    public List<Booking> getOtherServiceAppointment(Integer vendorId, Status status) {
        return getVendorBookings(vendorId, status);
    }

    @Deprecated
    public OsAppointmentResponse getAppointmentsDetails(Integer bookingId) {
        return getBookingDetails(bookingId);
    }

    @Deprecated
    public Booking updateAppointmentStatus(OsAppointmentStatusRequest request) {
        return updateBookingStatus(request);
    }

    @Deprecated
    public List<OsAppointmentDetailResponse> getAppointmentsByGarage(Integer vendorId, String statusStr) {
        return getBookingsByVendor(vendorId, statusStr);
    }

    @Deprecated
    public String generateAppointmentCode() {
        return generateBookingCode();
    }
}
