package com.autoadda.apis.other.service;


import com.autoadda.apis.other.entity.Location;
import com.autoadda.apis.other.entity.MediaFile;
import com.autoadda.apis.other.entity.ServiceCategory;
import com.autoadda.apis.other.entity.Vendor;
import com.autoadda.apis.other.entity.VendorServiceBridge;
import com.autoadda.apis.other.exception.AlreadyExistsException;
import com.autoadda.apis.other.exception.ResourceNotFoundException;
import com.autoadda.apis.other.mapper.VendorMapper;
import com.autoadda.apis.other.repository.MediaFileRepository;
import com.autoadda.apis.other.repository.ServiceCategoryRepository;
import com.autoadda.apis.other.repository.VendorRepository;
import com.autoadda.apis.other.request.GarageServiceRequest;
import com.autoadda.apis.other.request.OtherServiceRequest;
import com.autoadda.apis.other.response.OsResponse;
import com.autoadda.apis.other.util.Status;
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
    public Vendor createVendor(OtherServiceRequest request, String autoAddaId) {
        Vendor vendor = mapToEntity(request);

        // Set location
        Location location = locationService.createLocation(request.getCompanyLocation());
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
                        .map(serviceCategory -> vendorServiceBridgeService.createVendorServiceBridge(
                                new GarageServiceRequest(saved.getId(), serviceCategory.getId(), TRUE)))
                        .map(vendorServices::add)
                        .orElseThrow(() -> new ResourceNotFoundException("invalid id for service category"));
            });
        }

        saved.setServices(vendorServices);
        return vendorRepository.save(saved);
    }

    @Override
    public List<Vendor> getAllVendors() {
        return vendorRepository.findAll().stream()
                .filter(Vendor::isActive)
                .toList();
    }

    @Override
    public List<Vendor> getVendorsByOwnerId(String autoAddaId) {
        return vendorRepository.findByCompanyOwner(Integer.parseInt(autoAddaId)).stream()
                .filter(Vendor::isActive)
                .toList();
    }

    @Override
    public Optional<Vendor> getVendorById(Integer id) {
        return vendorRepository.findById(id)
                .filter(Vendor::isActive);
    }

    @Override
    @Transactional
    public Vendor updateVendor(Integer id, OtherServiceRequest request) {
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

        return vendorRepository.save(existing);
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
                        .map(serviceCategory -> vendorServiceBridgeService.createVendorServiceBridge(
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
    public List<Vendor> findVendorsByBrandName(String name) {
        return vendorRepository.findByCompanyBrandNameContainingIgnoreCaseAndIsActiveTrue(name);
    }

    @Override
    public List<Vendor> findVendorsByServiceType(String serviceType) {
        return vendorRepository.findByServiceTypeAndIsActiveTrue(serviceType);
    }

    @Override
    public List<Vendor> findVendorsByStatus(Status status) {
        return vendorRepository.findByAccountStatusAndIsActiveTrue(status);
    }

    @Override
    public Optional<Vendor> findVendorByEmail(String email) {
        return vendorRepository.findByEmailAndIsActiveTrue(email);
    }

    @Override
    public Optional<Vendor> findVendorByContact(String contact) {
        return vendorRepository.findByPhoneNumberOrMobileNumberAndIsActiveTrue(contact)
                .stream().findFirst();
    }

    @Override
    @Transactional
    public List<OsResponse> searchVendors(String city, String zip, String vendorName, List<Integer> serviceCategoryIds) {
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
