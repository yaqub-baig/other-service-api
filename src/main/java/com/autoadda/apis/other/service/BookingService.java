package com.autoadda.apis.other.service;

import com.autoadda.apis.other.entity.Booking;
import com.autoadda.apis.other.entity.BookedService;
import com.autoadda.apis.other.entity.BookingStatusActivity;
import com.autoadda.apis.other.entity.JobCard;
import com.autoadda.apis.other.entity.Vendor;
import com.autoadda.apis.other.entity.VendorQuotation;
import com.autoadda.apis.other.entity.VendorQuotationLineItem;
import com.autoadda.apis.other.entity.VendorService;
import com.autoadda.apis.other.exception.CustomerNotFoundException;
import com.autoadda.apis.other.exception.GarageNotFoundException;
import com.autoadda.apis.other.exception.OtherServiceNotFoundException;
import com.autoadda.apis.other.mapper.BookingMapper;
import com.autoadda.apis.other.mapper.OsAppointmentDetailMapper;
import com.autoadda.apis.other.exception.BusinessException;
import com.autoadda.apis.other.repository.*;
import com.autoadda.apis.other.request.BookingFromQuotationRequest;
import com.autoadda.apis.other.request.OsAppointmentStatusRequest;
import com.autoadda.apis.other.request.OsCustomerAppointmentRequest;
import com.autoadda.apis.other.response.BookingResponse;
import com.autoadda.apis.other.util.VendorQuotationStatus;
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

import java.math.BigDecimal;
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
    private final BookingStatusActivityRepository appointmentActivityRepo;
    private final BookedServiceRepository serviceRepo;
    private final VendorRepository vendorRepository;
    private final VendorServiceRepository vendorServiceRepo;
    private final VendorQuotationRepository vendorQuotationRepository;
    private final JobCardService jobCardService;
    private final OsAppointmentDetailMapper appointmentDetailMapper;
    private final BusinessValidator validator;
    private final BookingMapper bookingMapper; // Inject BookingMapper

    /**
     * Creates a new customer booking.
     *
     * @param request the booking request containing details like vendor ID,
     *                customer ID, vehicle ID, schedule date, service IDs, etc.
     * @return the created BookingResponse DTO.
     * @throws CustomerNotFoundException     if the customer does not exist.
     * @throws OtherServiceNotFoundException if the vendor does not exist.
     */
    @Transactional
    public BookingResponse createCustomerBooking(OsCustomerAppointmentRequest request)
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
        Booking savedBooking = bookingRepository.save(booking);

        // Save activity
        appointmentActivityRepo.save(BookingStatusActivity.builder()
                .bookingId(savedBooking.getId())
                .status(savedBooking.getStatus())
                .memberId(request.getMemberId())
                .memberType(request.getMemberType())
                .insertTms(DateHelper.dbDate())
                .build());

        // Services optional
        if (request.getServiceIds() != null && !request.getServiceIds().isEmpty()) {
            List<BookedService> services = request.getServiceIds().stream()
                    .filter(Objects::nonNull)
                    .map(serviceId -> {
                        VendorService vendorService = vendorServiceRepo.findById(serviceId)
                                .orElseThrow(() -> new RuntimeException("Service not found"));
                        return BookedService.builder()
                                .bookingId(savedBooking.getId())
                                .vendorService(vendorService)
                                .status(Status.NEW)
                                .build();
                    })
                    .collect(Collectors.toList());

            serviceRepo.saveAll(services);
        }

        return bookingMapper.toBookingResponse(savedBooking);
    }

    /**
     * Creates a booking from an accepted vendor quotation.
     * The booking is pre-filled with pricing details from the quotation.
     *
     * @param request the booking request containing quotation ID and optional details.
     * @return the created BookingResponse DTO.
     * @throws BusinessException if the quotation is not found or not accepted.
     */
    @Transactional
    public BookingResponse createBookingFromQuotation(BookingFromQuotationRequest request) {
        // Get and validate the vendor quotation
        VendorQuotation quotation = vendorQuotationRepository.findById(request.getVendorQuotationId())
                .orElseThrow(() -> new BusinessException("Vendor quotation not found"));

        // Validate the quotation is accepted
        if (quotation.getStatus() != VendorQuotationStatus.ACCEPTED) {
            throw new BusinessException("Can only create booking from an accepted quotation. Current status: " + quotation.getStatus());
        }

        // Validate customer owns the quotation request
        if (!quotation.getQuotationRequest().getCustomerId().equals(request.getCustomerId())) {
            throw new BusinessException("You can only create bookings from your own quotation requests");
        }

        // Check if a booking already exists for this quotation
        if (bookingRepository.existsByReferenceTypeAndReferenceId("QUOTATION", quotation.getId())) {
            throw new BusinessException("A booking has already been created for this quotation");
        }

        // Calculate pricing from line items if available
        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal taxes = BigDecimal.ZERO;
        BigDecimal discount = BigDecimal.ZERO;

        List<VendorQuotationLineItem> lineItems = quotation.getLineItems();
        if (lineItems != null && !lineItems.isEmpty()) {
            for (VendorQuotationLineItem item : lineItems) {
                if (item.getIsOptional() == null || !item.getIsOptional()) {
                    if (item.getUnitPrice() != null && item.getQuantity() != null) {
                        subtotal = subtotal.add(item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
                    }
                    if (item.getDiscount() != null) {
                        discount = discount.add(item.getDiscount());
                    }
                    if (item.getTaxRate() != null && item.getUnitPrice() != null && item.getQuantity() != null) {
                        BigDecimal itemSubtotal = item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
                        taxes = taxes.add(itemSubtotal.multiply(item.getTaxRate()).divide(BigDecimal.valueOf(100)));
                    }
                }
            }
        }

        // Use quotation total amount if available, otherwise calculate
        BigDecimal totalAmount = quotation.getTotalAmount();
        if (totalAmount == null) {
            totalAmount = subtotal.subtract(discount).add(taxes);
        }

        // Generate booking reference
        String bookingCode = generateBookingCode();

        // Create the booking
        Booking booking = Booking.builder()
                .appointmentReferenceId(bookingCode)
                .vendor(quotation.getVendor())
                .customerId(request.getCustomerId())
                .vehicleId(request.getVehicleId())
                .scheduleDate(request.getScheduleDate())
                .comments(request.getComments())
                .status(Status.CONFIRMED)
                .insert_tms(DateHelper.dbDate())
                // Quotation reference
                .referenceType("QUOTATION")
                .referenceId(quotation.getId())
                // Pricing details
                .subtotal(subtotal)
                .taxes(taxes)
                .discount(discount)
                .totalAmount(totalAmount)
                .build();

        Booking savedBooking = bookingRepository.save(booking);

        // Save activity
        appointmentActivityRepo.save(BookingStatusActivity.builder()
                .bookingId(savedBooking.getId())
                .status(savedBooking.getStatus())
                .comments("Booking created from quotation: " + quotation.getQuoteNumber())
                .insertTms(DateHelper.dbDate())
                .build());

        log.info("Created booking {} from quotation {} for customer {}",
                bookingCode, quotation.getQuoteNumber(), request.getCustomerId());

        return bookingMapper.toBookingResponse(savedBooking);
    }

    /**
     * Retrieves all bookings for a vendor, optionally filtered by status.
     *
     * @param vendorId the ID of the vendor.
     * @param status   the status to filter bookings by, or null for all.
     * @return a list of BookingResponse DTOs for the vendor.
     * @throws OtherServiceNotFoundException if the vendor does not exist.
     */
    @Transactional(readOnly = true)
    public List<BookingResponse> getVendorBookings(Integer vendorId, Status status)
            throws OtherServiceNotFoundException {

        validator.validateAndGetVendor(vendorId);

        List<Booking> bookings;
        if (status == null) { // status.toString() would throw NPE if status is null
            bookings = bookingRepository.findByVendorId(vendorId);
        } else {
            bookings = bookingRepository.findByVendorIdAndStatus(vendorId, status);
        }
        return bookingMapper.toBookingResponseList(bookings);
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

        List<BookedService> services = serviceRepo.findByBookingId(bookingId);
        List<JobCard> jobCards = jobCardService.getJobCardsByBookingId(bookingId);

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
     * @return the updated BookingResponse DTO.
     */
    @Transactional
    public BookingResponse updateBookingStatus(OsAppointmentStatusRequest request) {
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
        Booking updatedBooking = bookingRepository.save(booking);

        appointmentActivityRepo.save(BookingStatusActivity.builder()
                .bookingId(updatedBooking.getId())
                .status(updatedBooking.getStatus())
                .memberId(request.getMemberId())
                .memberType(request.getMemberType())
                .comments(request.getComments())
                .insertTms(DateHelper.dbDate())
                .build());

        return bookingMapper.toBookingResponse(updatedBooking);
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
    public BookingResponse createCustomerAppointment(OsCustomerAppointmentRequest request) {
        return createCustomerBooking(request);
    }

    @Deprecated
    public List<BookingResponse> getOtherServiceAppointment(Integer vendorId, Status status) {
        return getVendorBookings(vendorId, status);
    }

    @Deprecated
    public OsAppointmentResponse getAppointmentsDetails(Integer bookingId) {
        return getBookingDetails(bookingId);
    }

    @Deprecated
    public BookingResponse updateAppointmentStatus(OsAppointmentStatusRequest request) {
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
