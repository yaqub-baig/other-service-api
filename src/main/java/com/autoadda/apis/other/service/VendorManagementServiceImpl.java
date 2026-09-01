package com.autoadda.apis.other.service;


import com.autoadda.apis.other.clients.AuthClient;
import com.autoadda.apis.other.entity.Location;
import com.autoadda.apis.other.entity.MediaFile;
import com.autoadda.apis.other.entity.ServiceCategory;
import com.autoadda.apis.other.entity.Vendor;
import com.autoadda.apis.other.entity.VendorBreakTime;
import com.autoadda.apis.other.entity.VendorServiceBridge;
import com.autoadda.apis.other.entity.VendorWorkHours;
import com.autoadda.apis.other.exception.AlreadyExistsException;
import com.autoadda.apis.other.exception.ResourceNotFoundException;
import com.autoadda.apis.other.mapper.VendorMapper;
import com.autoadda.apis.other.mapper.VendorProfileMapper;
import com.autoadda.apis.other.repository.MediaFileRepository;
import com.autoadda.apis.other.repository.ServiceCategoryRepository;
import com.autoadda.apis.other.repository.VendorBreakTimeRepository;
import com.autoadda.apis.other.repository.VendorRepository;
import com.autoadda.apis.other.repository.VendorWorkHoursRepository;
import com.autoadda.apis.other.request.GarageServiceRequest;
import com.autoadda.apis.other.request.OtherServiceRequest;
import com.autoadda.apis.other.request.vendor.BaseVendorRequest;
import com.autoadda.apis.other.response.VendorResponse;
import com.autoadda.apis.other.response.VendorProfileResponse;
import com.autoadda.apis.other.service.vendor.VendorSlotGenerationService;
import com.autoadda.apis.other.service.vendor.onboarding.DefaultVendorOnboardingHandler;
import com.autoadda.apis.other.service.vendor.onboarding.VendorOnboardingHandler;
import com.autoadda.apis.other.util.ERole;
import com.autoadda.apis.other.util.Status;
import com.autoadda.apis.other.util.VendorServiceType;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static java.lang.Boolean.TRUE;

@Slf4j
@Service
@RequiredArgsConstructor
public class VendorManagementServiceImpl implements VendorManagementService {

    private final VendorRepository vendorRepository;
    private final ServiceCategoryRepository serviceCategoryRepository;
    private final LocationService locationService;
    private final MediaFileService mediaFileService;
    private final MediaFileRepository mediaFileRepository;
    private final VendorServiceBridgeService vendorServiceBridgeService;
    private final VendorMapper vendorMapper;
    private final VendorProfileMapper vendorProfileMapper;
    private final AuthClient authClient;
    private final VendorWorkHoursRepository workHoursRepository;
    private final VendorBreakTimeRepository breakTimeRepository;
    private final VendorSlotGenerationService slotGenerationService;
    private final List<VendorOnboardingHandler> onboardingHandlers;
    private final DefaultVendorOnboardingHandler defaultOnboardingHandler;

    private Vendor mapToEntity(OtherServiceRequest request) {
        Vendor vendor = new Vendor();
        vendor.setCompanyBrandName(request.getCompanyBrandName());
        vendor.setName(request.getName());
        vendor.setEmail(request.getEmail());
        vendor.setPhoneNumber(request.getPhoneNumber());
        vendor.setMobileNumber(request.getMobileNumber());
        vendor.setTrnNumber(request.getTrnNumber());
        vendor.setTradeLicenceNumber(request.getTradeLicenceNumber());
        vendor.setOpeningTime(request.getOpeningTime());
        vendor.setClosingTime(request.getClosingTime());
        vendor.setWorkingDays(request.getWorkingDays());
        vendor.setWebSite(request.getWebSite());
        vendor.setAccountStatus(Status.PENDING);
        vendor.setCompanyArabicName(request.getCompanyArabicName());
        vendor.setCreatedAt(LocalDateTime.now());
        return vendor;
    }

    @Override
    @Transactional
    public VendorProfileResponse createVendor(OtherServiceRequest request, String autoAddaId) {
        Vendor vendor = mapToEntity(request);

        // Set location (handle null gracefully)
        Location location = null;
        if (request.getCompanyLocation() != null) {
            location = locationService.createLocation(request.getCompanyLocation());
        }
        vendor.setCompanyLocation(location);
        vendor.setCompanyOwner(Integer.parseInt(autoAddaId));

        // Handle company logo
        MediaFile logo = mediaFileRepository.findById(request.getLogoId())
                .orElseThrow(() -> new EntityNotFoundException("Media file not found with id: " + request.getLogoId()));
        vendor.setCompanyLogo(logo);

        // Handle VAT certificates
        if (request.getVatCertificateIds() != null && !request.getVatCertificateIds().isEmpty()) {
            request.getVatCertificateIds().forEach(id -> {
                MediaFile mediaFile = mediaFileRepository.findById(id)
                        .orElseThrow(() -> new EntityNotFoundException("Media file not found with id: " + id));
                vendor.getVatCertificate().add(mediaFile);
            });
        }

        // Handle trade license attachments
        if (request.getTradeLicenseAttachmentIds() != null && !request.getTradeLicenseAttachmentIds().isEmpty()) {
            request.getTradeLicenseAttachmentIds().forEach(id -> {
                MediaFile mediaFile = mediaFileRepository.findById(id)
                        .orElseThrow(() -> new EntityNotFoundException("Media file not found with id: " + id));
                vendor.getTradeLicenseAttachment().add(mediaFile);
            });
        }

        // Handle company images
        if (request.getCompanyImagesIds() != null && !request.getCompanyImagesIds().isEmpty()) {
            request.getCompanyImagesIds().forEach(id -> {
                MediaFile mediaFile = mediaFileRepository.findById(id)
                        .orElseThrow(() -> new EntityNotFoundException("Media file not found with id: " + id));
                vendor.getCompanyImages().add(mediaFile);
            });
        }

        Vendor saved = vendorRepository.save(vendor);
        List<VendorServiceBridge> vendorServices = new ArrayList<>();
        if (request.getOsServiceIds() != null && !request.getOsServiceIds().isEmpty()) {
            request.getOsServiceIds().forEach(sId -> {
                serviceCategoryRepository.findById(sId)
                        .map(serviceCategory -> vendorServiceBridgeService.createVendorServiceBridgeEntity(
                                new GarageServiceRequest(saved.getId(), serviceCategory.getId(), TRUE)))
                        .map(vendorServices::add)
                        .orElseThrow(() -> new ResourceNotFoundException("invalid id for service category"));
            });
        }

        saved.setServices(vendorServices);
        Vendor finalVendor = vendorRepository.save(saved);
        return vendorProfileMapper.toVendorProfileResponse(finalVendor);
    }

    // ==================== Per-service onboarding (garage-style) ====================

    @Override
    @Transactional
    public VendorProfileResponse createVendor(BaseVendorRequest request, VendorServiceType type, String autoAddaId) {
        Integer ownerId = Integer.parseInt(autoAddaId);
        if (authClient.getUser(ownerId) == null) {
            throw new ResourceNotFoundException("Owner not found with id: " + ownerId);
        }

        VendorOnboardingHandler handler = resolveHandler(type);
        handler.validate(request);

        Vendor vendor = mapToEntity(request, type);
        handler.applyDefaults(vendor, request);
        vendor.setCompanyOwner(ownerId);

        if (request.getCompanyLocation() != null) {
            vendor.setCompanyLocation(locationService.createLocation(request.getCompanyLocation()));
        }
        attachMedia(vendor, request);

        Vendor saved = vendorRepository.save(vendor);
        saved.setServices(attachServiceBridges(saved, resolveServiceCategoryIds(request, type)));
        saved = vendorRepository.save(saved);

        handleVendorWorkingHours(saved, request);
        handleVendorBreakHours(saved, request);

        if (authClient.updateRole(ownerId, ERole.OTHER_SERVICE_OWNER) == null) {
            throw new ResourceNotFoundException("error while updating owner role");
        }

        vendorRepository.flush();
        workHoursRepository.flush();
        breakTimeRepository.flush();
        slotGenerationService.generateSlotsForDays(saved, 60);

        return vendorProfileMapper.toVendorProfileResponse(saved);
    }

    @Override
    @Transactional
    public VendorProfileResponse updateVendor(Integer id, BaseVendorRequest request) {
        Vendor existing = vendorRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Vendor not found with id: " + id));

        Integer oldCapacity = existing.getConcurrentAppointmentCapacity();

        applyIfPresent(request.getCompanyBrandName(), existing::setCompanyBrandName);
        applyIfPresent(request.getCompanyArabicName(), existing::setCompanyArabicName);
        applyIfPresent(request.getName(), existing::setName);
        applyIfPresent(request.getEmail(), existing::setEmail);
        applyIfPresent(request.getPhoneNumber(), existing::setPhoneNumber);
        applyIfPresent(request.getMobileNumber(), existing::setMobileNumber);
        applyIfPresent(request.getTrnNumber(), existing::setTrnNumber);
        applyIfPresent(request.getWebSite(), existing::setWebSite);
        if (request.getTradeLicenceNumber() != null) {
            validateTradeLicenceNumber(request.getTradeLicenceNumber());
            existing.setTradeLicenceNumber(request.getTradeLicenceNumber());
        }
        if (request.getOpeningTime() != null) {
            existing.setOpeningTime(request.getOpeningTime().toString());
        }
        if (request.getClosingTime() != null) {
            existing.setClosingTime(request.getClosingTime().toString());
        }
        if (request.getWorkingDays() != null && !request.getWorkingDays().isEmpty()) {
            existing.setWorkingDays(joinDays(request.getWorkingDays()));
        }
        if (request.getConcurrentAppointmentCapacity() != null) {
            existing.setConcurrentAppointmentCapacity(request.getConcurrentAppointmentCapacity());
        }
        if (request.getCompanyLocation() != null && existing.getCompanyLocation() != null) {
            existing.setCompanyLocation(locationService.updateLocation(
                    existing.getCompanyLocation().getId(), request.getCompanyLocation()));
        }

        if (request.getServiceCategoryIds() != null) {
            List<Integer> existingCategoryIds = existing.getServices().stream()
                    .map(b -> b.getServiceCategory().getId())
                    .toList();
            request.getServiceCategoryIds().stream()
                    .filter(cid -> !existingCategoryIds.contains(cid))
                    .forEach(cid -> existing.getServices().add(serviceCategoryRepository.findById(cid)
                            .map(sc -> vendorServiceBridgeService.createVendorServiceBridgeEntity(
                                    new GarageServiceRequest(existing.getId(), sc.getId(), TRUE)))
                            .orElseThrow(() -> new ResourceNotFoundException("invalid id for service category: " + cid))));
        }

        Vendor updated = vendorRepository.save(existing);

        if (!Objects.equals(oldCapacity, updated.getConcurrentAppointmentCapacity())) {
            slotGenerationService.updateUpcomingSlotsForVendor(updated);
        }
        return vendorProfileMapper.toVendorProfileResponse(updated);
    }

    @Override
    public List<VendorProfileResponse> getVendorsByOwnerIdAndType(String autoAddaId, VendorServiceType type) {
        List<Vendor> vendors = vendorRepository
                .findByCompanyOwnerAndServiceTypeAndIsActiveTrue(Integer.parseInt(autoAddaId), type.name());
        return vendorProfileMapper.toVendorProfileResponseList(vendors);
    }

    private VendorOnboardingHandler resolveHandler(VendorServiceType type) {
        return onboardingHandlers.stream()
                .filter(h -> h.type() == type)
                .findFirst()
                .orElse(defaultOnboardingHandler);
    }

    private Vendor mapToEntity(BaseVendorRequest request, VendorServiceType type) {
        Vendor vendor = new Vendor();
        vendor.setCompanyBrandName(request.getCompanyBrandName());
        vendor.setCompanyArabicName(request.getCompanyArabicName());
        vendor.setName(request.getName());
        vendor.setEmail(request.getEmail());
        vendor.setPhoneNumber(request.getPhoneNumber());
        vendor.setMobileNumber(request.getMobileNumber());
        vendor.setTrnNumber(request.getTrnNumber());
        vendor.setTradeLicenceNumber(request.getTradeLicenceNumber());
        vendor.setWebSite(request.getWebSite());
        vendor.setOpeningTime(request.getOpeningTime() != null ? request.getOpeningTime().toString() : null);
        vendor.setClosingTime(request.getClosingTime() != null ? request.getClosingTime().toString() : null);
        vendor.setWorkingDays(request.getWorkingDays() != null ? joinDays(request.getWorkingDays()) : null);
        vendor.setConcurrentAppointmentCapacity(request.getConcurrentAppointmentCapacity());
        vendor.setServiceType(type.name());
        vendor.setAccountStatus(Status.PENDING);
        vendor.setCreatedAt(LocalDateTime.now());
        return vendor;
    }

    private void attachMedia(Vendor vendor, BaseVendorRequest request) {
        if (request.getLogoId() != null) {
            vendor.setCompanyLogo(mediaFileRepository.findById(request.getLogoId())
                    .orElseThrow(() -> new EntityNotFoundException("Media file not found with id: " + request.getLogoId())));
        }
        addMediaFiles(request.getVatCertificateIds(), vendor.getVatCertificate());
        addMediaFiles(request.getTradeLicenseAttachmentIds(), vendor.getTradeLicenseAttachment());
        addMediaFiles(request.getCompanyImagesIds(), vendor.getCompanyImages());
    }

    private void addMediaFiles(List<Long> ids, List<MediaFile> target) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        ids.forEach(id -> target.add(mediaFileRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Media file not found with id: " + id))));
    }

    private List<Integer> resolveServiceCategoryIds(BaseVendorRequest request, VendorServiceType type) {
        List<Integer> ids = new ArrayList<>();
        if (request.getServiceCategoryIds() != null) {
            ids.addAll(request.getServiceCategoryIds());
        }
        serviceCategoryRepository.findByName(type.getDisplayName())
                .map(ServiceCategory::getId)
                .filter(id -> !ids.contains(id))
                .ifPresent(ids::add);
        return ids;
    }

    private List<VendorServiceBridge> attachServiceBridges(Vendor vendor, List<Integer> categoryIds) {
        List<VendorServiceBridge> bridges = new ArrayList<>();
        categoryIds.forEach(cid -> serviceCategoryRepository.findById(cid)
                .map(sc -> vendorServiceBridgeService.createVendorServiceBridgeEntity(
                        new GarageServiceRequest(vendor.getId(), sc.getId(), TRUE)))
                .map(bridges::add)
                .orElseThrow(() -> new ResourceNotFoundException("invalid id for service category: " + cid)));
        return bridges;
    }

    private void handleVendorWorkingHours(Vendor vendor, BaseVendorRequest request) {
        if (request.getWorkingDays() == null) {
            return;
        }
        request.getWorkingDays().forEach(day -> {
            VendorWorkHours wh = new VendorWorkHours();
            wh.setVendor(vendor);
            wh.setDayOfWeek(day);
            wh.setOpenTime(request.getOpeningTime());
            wh.setCloseTime(request.getClosingTime());
            wh.setClosed(false);
            workHoursRepository.save(wh);
        });
    }

    private void handleVendorBreakHours(Vendor vendor, BaseVendorRequest request) {
        if (request.getWorkingDays() == null
                || request.getBreakStartTime() == null || request.getBreakEndTime() == null) {
            return;
        }
        request.getWorkingDays().forEach(day -> {
            VendorBreakTime bt = new VendorBreakTime();
            bt.setVendor(vendor);
            bt.setDayOfWeek(day);
            bt.setBreakStart(request.getBreakStartTime());
            bt.setBreakEnd(request.getBreakEndTime());
            breakTimeRepository.save(bt);
        });
    }

    private static String joinDays(List<java.time.DayOfWeek> days) {
        return days.stream().map(Enum::name).collect(Collectors.joining(","));
    }

    private static <T> void applyIfPresent(T value, java.util.function.Consumer<T> setter) {
        if (value != null) {
            setter.accept(value);
        }
    }

    @Override
    public List<VendorProfileResponse> getAllVendors() {
        List<Vendor> vendors = vendorRepository.findAll().stream()
                .filter(Vendor::isActive)
                .toList();
        return vendorProfileMapper.toVendorProfileResponseList(vendors);
    }

    @Override
    public List<VendorProfileResponse> getVendorsByOwnerId(String autoAddaId) {
        List<Vendor> vendors = vendorRepository.findByCompanyOwner(Integer.parseInt(autoAddaId)).stream()
                .filter(Vendor::isActive)
                .toList();
        return vendorProfileMapper.toVendorProfileResponseList(vendors);
    }

    @Override
    public Optional<VendorProfileResponse> getVendorById(Integer id) {
        return vendorRepository.findById(id)
                .filter(Vendor::isActive)
                .map(vendorProfileMapper::toVendorProfileResponse);
    }

    @Override
    @Transactional
    public VendorProfileResponse updateVendor(Integer id, OtherServiceRequest request) {
        Vendor existing = vendorRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Vendor not found with id: " + id));

        // Update location if provided
        if (request.getCompanyLocation() != null) {
            Location existingLocation = existing.getCompanyLocation();
            Location location = locationService.updateLocation(existingLocation.getId(), request.getCompanyLocation());
            existing.setCompanyLocation(location);
        }

        // Update other fields if provided
        if (request.getCompanyArabicName() != null) {
            existing.setCompanyArabicName(request.getCompanyArabicName());
        }
        if (request.getCompanyBrandName() != null) {
            existing.setCompanyBrandName(request.getCompanyBrandName());
        }
        if (request.getOpeningTime() != null) {
            existing.setOpeningTime(request.getOpeningTime());
        }
        if (request.getClosingTime() != null) {
            existing.setClosingTime(request.getClosingTime());
        }
        if (request.getWorkingDays() != null) {
            existing.setWorkingDays(request.getWorkingDays());
        }
        if (request.getTradeLicenceNumber() != null) {
            validateTradeLicenceNumber(request.getTradeLicenceNumber());
            existing.setTradeLicenceNumber(request.getTradeLicenceNumber());
        }
        if (request.getTrnNumber() != null) {
            existing.setTrnNumber(request.getTrnNumber());
        }
        if (request.getPhoneNumber() != null) {
            existing.setPhoneNumber(request.getPhoneNumber());
        }
        if (request.getWebSite() != null) {
            existing.setWebSite(request.getWebSite());
        }
        if (request.getName() != null) {
            existing.setName(request.getName());
        }
        if (request.getMobileNumber() != null) {
            existing.setMobileNumber(request.getMobileNumber());
        }
        if (request.getEmail() != null) {
            existing.setEmail(request.getEmail());
        }

        // Handle company logo update
        if (request.getLogoId() != null) {
            if (existing.getCompanyLogo() != null) {
                scheduleMediaFileCleanup(Collections.singletonList(existing.getCompanyLogo().getId()));
            }
            MediaFile mediaFile = mediaFileRepository.findById(request.getLogoId())
                    .orElseThrow(() -> new ResourceNotFoundException("File not found"));
            existing.setCompanyLogo(mediaFile);
        }

        // Handle VAT certificate update
        if (request.getVatCertificateIds() != null) {
            List<Long> oldVatCertIds = existing.getVatCertificate().stream()
                    .map(MediaFile::getId)
                    .toList();
            if (!oldVatCertIds.isEmpty()) {
                scheduleMediaFileCleanup(oldVatCertIds);
            }

            existing.getVatCertificate().clear();
            request.getVatCertificateIds().forEach(vid -> {
                MediaFile mediaFile = mediaFileRepository.findById(vid)
                        .orElseThrow(() -> new ResourceNotFoundException("File not found"));
                existing.getVatCertificate().add(mediaFile);
            });
        }

        // Handle trade license update
        if (request.getTradeLicenseAttachmentIds() != null) {
            List<Long> oldTradeLicenseIds = existing.getTradeLicenseAttachment().stream()
                    .map(MediaFile::getId)
                    .toList();
            if (!oldTradeLicenseIds.isEmpty()) {
                scheduleMediaFileCleanup(oldTradeLicenseIds);
            }

            existing.getTradeLicenseAttachment().clear();
            request.getTradeLicenseAttachmentIds().forEach(tid -> {
                MediaFile mediaFile = mediaFileRepository.findById(tid)
                        .orElseThrow(() -> new ResourceNotFoundException("File not found"));
                existing.getTradeLicenseAttachment().add(mediaFile);
            });
        }

        // Handle company images update
        if (request.getCompanyImagesIds() != null) {
            List<Long> oldCompanyImageIds = existing.getCompanyImages().stream()
                    .map(MediaFile::getId)
                    .toList();
            if (!oldCompanyImageIds.isEmpty()) {
                scheduleMediaFileCleanup(oldCompanyImageIds);
            }

            existing.getCompanyImages().clear();
            request.getCompanyImagesIds().forEach(cid -> {
                MediaFile mediaFile = mediaFileRepository.findById(cid)
                        .orElseThrow(() -> new ResourceNotFoundException("File not found"));
                existing.getCompanyImages().add(mediaFile);
            });
        }

        List<VendorServiceBridge> vendorServiceBridges = handleVendorServices(request, existing);

        existing.getServices().clear();
        existing.getServices().addAll(vendorServiceBridges);

        Vendor updatedVendor = vendorRepository.save(existing);
        return vendorProfileMapper.toVendorProfileResponse(updatedVendor);
    }

    private void validateTradeLicenceNumber(String tradeLicenceNumber) {
        vendorRepository.findByTradeLicenceNumber(tradeLicenceNumber).ifPresent(vendor -> {
            throw new AlreadyExistsException("Vendor with trade licence number " + tradeLicenceNumber + " already exists");
        });
    }

    private List<VendorServiceBridge> handleVendorServices(OtherServiceRequest request, Vendor vendor) {
        List<VendorServiceBridge> vendorServices = new ArrayList<>();
        if (request.getOsServiceIds() != null && !request.getOsServiceIds().isEmpty()) {
            request.getOsServiceIds().forEach(sId -> {
                serviceCategoryRepository.findById(sId)
                        .map(serviceCategory -> vendorServiceBridgeService.createVendorServiceBridgeEntity(
                                new GarageServiceRequest(vendor.getId(), serviceCategory.getId(), TRUE)))
                        .map(vendorServices::add)
                        .orElseThrow(() -> new ResourceNotFoundException("invalid id for service category"));
            });
        }
        return vendorServices;
    }

    @Override
    @Transactional
    public void deleteVendor(Integer id) {
        Vendor vendor = vendorRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Vendor not found with id: " + id));

        // Mark as inactive instead of deleting
        vendor.setActive(false);
        vendorRepository.save(vendor);

        // Schedule media files for cleanup
        List<Long> mediaFileIds = new ArrayList<>();

        if (vendor.getCompanyLogo() != null) {
            mediaFileIds.add(vendor.getCompanyLogo().getId());
        }

        mediaFileIds.addAll(vendor.getVatCertificate().stream()
                .map(MediaFile::getId)
                .toList());

        mediaFileIds.addAll(vendor.getTradeLicenseAttachment().stream()
                .map(MediaFile::getId)
                .toList());

        mediaFileIds.addAll(vendor.getCompanyImages().stream()
                .map(MediaFile::getId)
                .toList());

        if (!mediaFileIds.isEmpty()) {
            scheduleMediaFileCleanup(mediaFileIds);
        }
    }

    @Override
    public List<VendorProfileResponse> findVendorsByBrandName(String name) {
        List<Vendor> vendors = vendorRepository.findByCompanyBrandNameContainingIgnoreCaseAndIsActiveTrue(name);
        return vendorProfileMapper.toVendorProfileResponseList(vendors);
    }

    @Override
    public List<VendorProfileResponse> findVendorsByServiceType(String serviceType) {
        List<Vendor> vendors = vendorRepository.findByServiceTypeAndIsActiveTrue(serviceType);
        return vendorProfileMapper.toVendorProfileResponseList(vendors);
    }

    @Override
    public List<VendorProfileResponse> findVendorsByStatus(Status status) {
        List<Vendor> vendors = vendorRepository.findByAccountStatusAndIsActiveTrue(status);
        return vendorProfileMapper.toVendorProfileResponseList(vendors);
    }

    @Override
    public Optional<VendorProfileResponse> findVendorByEmail(String email) {
        return vendorRepository.findByEmailAndIsActiveTrue(email)
                .map(vendorProfileMapper::toVendorProfileResponse);
    }

    @Override
    public Optional<VendorProfileResponse> findVendorByContact(String contact) {
        return vendorRepository.findByPhoneNumberOrMobileNumberAndIsActiveTrue(contact)
                .stream().findFirst()
                .map(vendorProfileMapper::toVendorProfileResponse);
    }

    @Override
    @Transactional
    public List<VendorResponse> searchVendors(String city, String zip, String vendorName, List<Integer> serviceCategoryIds) {
        Set<Vendor> vendors = vendorRepository.findAllWithLocationAndServices();
        List<Vendor> searchResults = new ArrayList<>();

        for (Vendor vendor : vendors) {
            boolean cityCheck = true;
            boolean nameCheck = true;
            boolean zipCheck = true;
            boolean serviceCheck = true;

            log.info("Searching vendors, total: {}", vendors.size());
            log.info("Current vendor: {}", vendor.getName());

            Location address = vendor.getCompanyLocation();

            if (StringUtils.isNotEmpty(city)) {
                if (!address.getCity().equalsIgnoreCase(city)) {
                    cityCheck = false;
                }
            }

            if (StringUtils.isNotEmpty(zip)) {
                if (!address.getPostalCode().equalsIgnoreCase(zip)) {
                    zipCheck = false;
                }
            }

            if (StringUtils.isNotEmpty(vendorName)) {
                if (!vendor.getName().toLowerCase().contains(vendorName.toLowerCase())) {
                    nameCheck = false;
                }
            }

            if (serviceCategoryIds != null && !serviceCategoryIds.isEmpty()) {
                List<Integer> vendorServiceIds = vendor.getServices().stream()
                        .map(VendorServiceBridge::getServiceCategory)
                        .map(ServiceCategory::getId)
                        .toList();

                serviceCheck = serviceCategoryIds.stream()
                        .anyMatch(vendorServiceIds::contains);
            }

            if (cityCheck && nameCheck && zipCheck && serviceCheck) {
                searchResults.add(vendor);
            }
        }

        return searchResults.stream().map(vendorMapper::entityToResponse).toList();
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void scheduleMediaFileCleanup(List<Long> mediaFileIds) {
        for (Long mediaFileId : mediaFileIds) {
            try {
                if (!isMediaFileStillReferenced(mediaFileId)) {
                    mediaFileService.delete(mediaFileId);
                }
            } catch (Exception e) {
                log.error("Error cleaning up media file with id: " + mediaFileId, e);
            }
        }
    }

    private boolean isMediaFileStillReferenced(Long mediaFileId) {
        return vendorRepository.countByCompanyLogoId(mediaFileId) > 0 ||
                vendorRepository.countByVatCertificateId(mediaFileId) > 0 ||
                vendorRepository.countByTradeLicenseAttachmentId(mediaFileId) > 0 ||
                vendorRepository.countByCompanyImagesId(mediaFileId) > 0;
    }
}
