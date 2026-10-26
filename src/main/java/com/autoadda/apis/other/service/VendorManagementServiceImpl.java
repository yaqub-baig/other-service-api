package com.autoadda.apis.other.service;

import com.autoadda.apis.other.util.IsoCodes;

import com.autoadda.apis.other.clients.AuthClient;
import com.autoadda.apis.other.entity.MediaFile;
import com.autoadda.apis.other.entity.ServiceCategory;
import com.autoadda.apis.other.entity.Vendor;
import com.autoadda.apis.other.entity.VendorBreakTime;
import com.autoadda.apis.other.entity.VendorService;
import com.autoadda.apis.other.entity.VendorWorkHours;
import com.autoadda.apis.other.exception.BusinessException;
import com.autoadda.apis.other.exception.ResourceNotFoundException;
import com.autoadda.apis.other.exception.VendorNotFoundException;
import com.autoadda.apis.other.mapper.VendorMapper;
import com.autoadda.apis.other.mapper.VendorProfileMapper;
import com.autoadda.apis.other.mapper.ServiceAttributeDefinitionMapper;
import com.autoadda.apis.other.mapper.VendorServiceMapper;
import com.autoadda.apis.other.repository.MediaFileRepository;
import com.autoadda.apis.other.repository.ServiceCategoryRepository;
import com.autoadda.apis.other.repository.VendorBreakTimeRepository;
import com.autoadda.apis.other.repository.VendorHolidayOverrideRepository;
import com.autoadda.apis.other.repository.VendorRepository;
import com.autoadda.apis.other.repository.VendorServiceRepository;
import com.autoadda.apis.other.repository.VendorWorkHoursRepository;
import com.autoadda.apis.other.request.vendor.VendorServiceOnboardingRequest;
import com.autoadda.apis.other.request.vendor.VendorCompanyRequest;
import com.autoadda.apis.other.response.ServiceAttributeDefinitionResponse;
import com.autoadda.apis.other.response.VendorOnboardingResponse;
import com.autoadda.apis.other.response.VendorProfileResponse;
import com.autoadda.apis.other.response.VendorResponse;
import com.autoadda.apis.other.response.VendorServiceResponse;
import com.autoadda.apis.other.service.vendor.VendorSlotGenerationService;
import com.autoadda.apis.other.service.vendor.onboarding.DefaultVendorOnboardingHandler;
import com.autoadda.apis.other.service.vendor.onboarding.VendorOnboardingHandler;
import com.autoadda.apis.other.util.ERole;
import com.autoadda.apis.other.util.Status;
import com.autoadda.apis.other.util.VendorServiceType;
import com.autoadda.apis.other.validation.AttributeValidator;
import com.autoadda.apis.other.validation.FieldSpecFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class VendorManagementServiceImpl implements VendorManagementService {

    private final VendorRepository vendorRepository;
    private final VendorServiceRepository vendorServiceRepository;
    private final ServiceCategoryRepository serviceCategoryRepository;
    private final LocationService locationService;
    private final MediaFileService mediaFileService;
    private final MediaFileRepository mediaFileRepository;
    private final VendorMapper vendorMapper;
    private final VendorProfileMapper vendorProfileMapper;
    private final VendorServiceMapper vendorServiceMapper;
    private final AuthClient authClient;
    private final VendorWorkHoursRepository workHoursRepository;
    private final VendorBreakTimeRepository breakTimeRepository;
    private final VendorHolidayOverrideRepository holidayOverrideRepository;
    private final VendorSlotGenerationService slotGenerationService;
    private final List<VendorOnboardingHandler> onboardingHandlers;
    private final DefaultVendorOnboardingHandler defaultOnboardingHandler;
    private final MarketService marketService;
    private final ServiceAttributeDefinitionService attributeDefinitionService;
    private final ServiceAttributeDefinitionMapper attributeMapper;
    private final AttributeValidator attributeValidator;
    private final FieldSpecFactory fieldSpecFactory;
    private final ObjectMapper objectMapper;

    // ==================== company ====================

    @Override
    @Transactional
    public VendorProfileResponse createCompany(VendorCompanyRequest request, String autoAddaId) {
        Integer ownerId = Integer.parseInt(autoAddaId);
        if (authClient.getUser(ownerId) == null) {
            throw new VendorNotFoundException("Owner not found with id: " + ownerId);
        }

        Vendor vendor = new Vendor();
        vendor.setCompanyBrandName(request.getCompanyBrandName());
        vendor.setCompanyArabicName(request.getCompanyArabicName());
        vendor.setName(request.getName());
        vendor.setTrnNumber(request.getTrnNumber());
        vendor.setWebSite(request.getWebSite());
        vendor.setEmail(request.getEmail());
        vendor.setPhoneNumber(request.getPhoneNumber());
        vendor.setMobileNumber(request.getMobileNumber());
        vendor.setCountryCode(request.getCountryCode() != null
                ? IsoCodes.country(request.getCountryCode())
                : marketService.getDefaultCountry());
        vendor.setCompanyOwner(ownerId);
        vendor.setAccountStatus(Status.PENDING);

        if (request.getCompanyLocation() != null) {
            vendor.setCompanyLocation(locationService.createLocation(request.getCompanyLocation()));
        }
        if (request.getLogoId() != null) {
            vendor.setCompanyLogo(resolveMedia(request.getLogoId()));
        }
        addMedia(request.getVatCertificateIds(), vendor.getVatCertificate());

        Vendor saved = vendorRepository.save(vendor);

        if (authClient.updateRole(ownerId, ERole.OTHER_SERVICE_OWNER) == null) {
            throw new ResourceNotFoundException("error while updating owner role");
        }
        return vendorProfileMapper.toVendorProfileResponse(saved);
    }

    @Override
    @Transactional
    public VendorProfileResponse updateCompany(Integer vendorId, VendorCompanyRequest request) {
        Vendor existing = getVendorOrThrow(vendorId);

        applyIfPresent(request.getCompanyBrandName(), existing::setCompanyBrandName);
        applyIfPresent(request.getCompanyArabicName(), existing::setCompanyArabicName);
        applyIfPresent(request.getName(), existing::setName);
        applyIfPresent(request.getTrnNumber(), existing::setTrnNumber);
        applyIfPresent(request.getWebSite(), existing::setWebSite);
        applyIfPresent(request.getEmail(), existing::setEmail);
        applyIfPresent(request.getPhoneNumber(), existing::setPhoneNumber);
        applyIfPresent(request.getMobileNumber(), existing::setMobileNumber);
        if (request.getCountryCode() != null) {
            existing.setCountryCode(IsoCodes.country(request.getCountryCode()));
        }

        if (request.getCompanyLocation() != null && existing.getCompanyLocation() != null) {
            existing.setCompanyLocation(locationService.updateLocation(
                    existing.getCompanyLocation().getId(), request.getCompanyLocation()));
        } else if (request.getCompanyLocation() != null) {
            existing.setCompanyLocation(locationService.createLocation(request.getCompanyLocation()));
        }
        if (request.getLogoId() != null) {
            if (existing.getCompanyLogo() != null) {
                scheduleMediaFileCleanup(Collections.singletonList(existing.getCompanyLogo().getId()));
            }
            existing.setCompanyLogo(resolveMedia(request.getLogoId()));
        }
        if (request.getVatCertificateIds() != null) {
            List<Long> old = existing.getVatCertificate().stream().map(MediaFile::getId).toList();
            if (!old.isEmpty()) {
                scheduleMediaFileCleanup(old);
            }
            existing.getVatCertificate().clear();
            addMedia(request.getVatCertificateIds(), existing.getVatCertificate());
        }

        return vendorProfileMapper.toVendorProfileResponse(vendorRepository.save(existing));
    }

    @Override
    @Transactional
    public void deleteVendor(Integer vendorId) {
        Vendor vendor = getVendorOrThrow(vendorId);
        vendor.setActive(false);
        vendorRepository.save(vendor);

        List<Long> mediaIds = new ArrayList<>();
        if (vendor.getCompanyLogo() != null) {
            mediaIds.add(vendor.getCompanyLogo().getId());
        }
        vendor.getVatCertificate().forEach(m -> mediaIds.add(m.getId()));
        vendor.getServices().forEach(vs -> {
            vs.getTradeLicenseAttachment().forEach(m -> mediaIds.add(m.getId()));
            vs.getServiceImages().forEach(m -> mediaIds.add(m.getId()));
        });
        if (!mediaIds.isEmpty()) {
            scheduleMediaFileCleanup(mediaIds);
        }
    }

    @Override
    public List<VendorProfileResponse> getAllVendors() {
        return vendorProfileMapper.toVendorProfileResponseList(
                vendorRepository.findByIsActiveTrue());
    }

    @Override
    public List<VendorProfileResponse> getVendorsByOwnerId(String autoAddaId) {
        return vendorProfileMapper.toVendorProfileResponseList(
                vendorRepository.findByCompanyOwnerAndIsActiveTrue(Integer.parseInt(autoAddaId)));
    }

    @Override
    public Optional<VendorProfileResponse> getVendorById(Integer id) {
        return vendorRepository.findById(id)
                .filter(Vendor::isActive)
                .map(vendorProfileMapper::toVendorProfileResponse);
    }

    @Override
    public List<VendorProfileResponse> findVendorsByBrandName(String name) {
        return vendorProfileMapper.toVendorProfileResponseList(
                vendorRepository.findByCompanyBrandNameContainingIgnoreCaseAndIsActiveTrue(name));
    }

    @Override
    public List<VendorProfileResponse> findVendorsByStatus(Status status) {
        return vendorProfileMapper.toVendorProfileResponseList(
                vendorRepository.findByAccountStatusAndIsActiveTrue(status));
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
        List<Vendor> results = new ArrayList<>();

        for (Vendor vendor : vendors) {
            boolean cityCheck = true;
            boolean zipCheck = true;
            boolean nameCheck = true;
            boolean serviceCheck = true;

            var address = vendor.getCompanyLocation();
            if (StringUtils.isNotEmpty(city) && (address == null || !city.equalsIgnoreCase(address.getCity()))) {
                cityCheck = false;
            }
            if (StringUtils.isNotEmpty(zip) && (address == null || !zip.equalsIgnoreCase(address.getPostalCode()))) {
                zipCheck = false;
            }
            if (StringUtils.isNotEmpty(vendorName)
                    && (vendor.getName() == null || !vendor.getName().toLowerCase().contains(vendorName.toLowerCase()))) {
                nameCheck = false;
            }
            if (serviceCategoryIds != null && !serviceCategoryIds.isEmpty()) {
                List<Integer> ids = vendor.getServices().stream()
                        .map(VendorService::getServiceCategory)
                        .filter(Objects::nonNull)
                        .map(ServiceCategory::getId)
                        .toList();
                serviceCheck = serviceCategoryIds.stream().anyMatch(ids::contains);
            }
            if (cityCheck && zipCheck && nameCheck && serviceCheck) {
                results.add(vendor);
            }
        }
        return results.stream().map(vendorMapper::entityToResponse).toList();
    }

    // ==================== services ====================

    @Override
    @Transactional
    public VendorOnboardingResponse onboard(VendorCompanyRequest company, VendorServiceOnboardingRequest service,
                                            String autoAddaId) {
        if (company == null || service == null) {
            throw new BusinessException("company and service are both required");
        }
        VendorProfileResponse vendor = createCompany(company, autoAddaId);
        return new VendorOnboardingResponse(vendor, addService(vendor.getId(), service, autoAddaId));
    }

    @Override
    @Transactional
    public VendorServiceResponse addService(Integer vendorId, VendorServiceOnboardingRequest request, String autoAddaId) {
        VendorServiceType type = request.getServiceType();
        if (type == null) {
            throw new BusinessException("serviceType is required");
        }
        Vendor vendor = getVendorOrThrow(vendorId);

        VendorOnboardingHandler handler = resolveHandler(type);
        handler.validate(request);

        VendorService service = new VendorService();
        service.setVendor(vendor);
        service.setIsActive(true);
        service.setServiceType(type.name());
        service.setAccountStatus(Status.PENDING);
        service.setServiceCategory(categoryFor(type));
        service.setOpeningTime(timeString(request.getOpeningTime()));
        service.setClosingTime(timeString(request.getClosingTime()));
        service.setWorkingDays(request.getWorkingDays() != null ? joinDays(request.getWorkingDays()) : null);
        service.setConcurrentAppointmentCapacity(request.getConcurrentAppointmentCapacity());
        service.setTradeLicenceNumber(request.getTradeLicenceNumber());
        // validated even when none are sent, so required details can't be skipped
        service.setAttributes(checkedAttributes(service, request.getAttributes()));

        handler.applyDefaults(service, request);

        if (request.getServiceLocation() != null) {
            service.setServiceLocation(locationService.createLocation(request.getServiceLocation()));
        }
        addMedia(request.getTradeLicenseAttachmentIds(), service.getTradeLicenseAttachment());
        addMedia(request.getServiceImagesIds(), service.getServiceImages());

        VendorService saved = vendorServiceRepository.save(service);
        attachExtraCategories(saved, request.getServiceCategoryIds());

        rebuildWorkingHours(saved, request);
        rebuildBreaks(saved, request);

        workHoursRepository.flush();
        breakTimeRepository.flush();
        slotGenerationService.generateSlotsForDays(saved, 60);

        return vendorServiceMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public VendorServiceResponse updateService(Integer vendorServiceId, VendorServiceOnboardingRequest request) {
        VendorService service = getServiceOrThrow(vendorServiceId);

        Integer oldCapacity = service.getConcurrentAppointmentCapacity();
        String oldWorkingDays = service.getWorkingDays();
        String oldOpen = service.getOpeningTime();
        String oldClose = service.getClosingTime();

        if (request.getOpeningTime() != null) {
            service.setOpeningTime(timeString(request.getOpeningTime()));
        }
        if (request.getClosingTime() != null) {
            service.setClosingTime(timeString(request.getClosingTime()));
        }
        if (request.getWorkingDays() != null && !request.getWorkingDays().isEmpty()) {
            service.setWorkingDays(joinDays(request.getWorkingDays()));
        }
        if (request.getConcurrentAppointmentCapacity() != null) {
            service.setConcurrentAppointmentCapacity(request.getConcurrentAppointmentCapacity());
        }
        applyIfPresent(request.getTradeLicenceNumber(), service::setTradeLicenceNumber);
        if (request.getAttributes() != null) {
            Map<String, Object> merged = new LinkedHashMap<>(vendorServiceMapper.readAttributes(service));
            request.getAttributes().forEach((code, value) -> {
                if (value == null) {
                    merged.remove(code);
                } else {
                    merged.put(code, value);
                }
            });
            service.setAttributes(checkedAttributes(service, merged));
        }

        if (request.getServiceLocation() != null && service.getServiceLocation() != null) {
            service.setServiceLocation(locationService.updateLocation(
                    service.getServiceLocation().getId(), request.getServiceLocation()));
        } else if (request.getServiceLocation() != null) {
            service.setServiceLocation(locationService.createLocation(request.getServiceLocation()));
        }
        if (request.getTradeLicenseAttachmentIds() != null) {
            replaceMedia(service.getTradeLicenseAttachment(), request.getTradeLicenseAttachmentIds());
        }
        if (request.getServiceImagesIds() != null) {
            replaceMedia(service.getServiceImages(), request.getServiceImagesIds());
        }

        VendorService saved = vendorServiceRepository.save(service);
        attachExtraCategories(saved, request.getServiceCategoryIds());

        boolean hoursChanged = request.getWorkingDays() != null
                && (!Objects.equals(oldWorkingDays, saved.getWorkingDays())
                || !Objects.equals(oldOpen, saved.getOpeningTime())
                || !Objects.equals(oldClose, saved.getClosingTime()));
        if (hoursChanged) {
            rebuildWorkingHours(saved, request);
            rebuildBreaks(saved, request);
            slotGenerationService.generateSlotsForDays(saved, 60);
        }
        if (!Objects.equals(oldCapacity, saved.getConcurrentAppointmentCapacity())) {
            slotGenerationService.updateUpcomingSlotsForVendorService(saved);
        }
        return vendorServiceMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void deleteService(Integer vendorServiceId) {
        // Deactivated, not deleted: offerings, bookings and quotes keep pointing at this row
        VendorService service = getServiceOrThrow(vendorServiceId);
        service.setIsActive(false);
        vendorServiceRepository.save(service);
    }

    @Override
    public VendorServiceResponse getService(Integer vendorServiceId) {
        return vendorServiceMapper.toResponse(getServiceOrThrow(vendorServiceId));
    }

    @Override
    public List<VendorServiceResponse> getServicesByVendor(Integer vendorId) {
        return vendorServiceMapper.toResponseList(vendorServiceRepository.findByVendorId(vendorId));
    }

    @Override
    public List<VendorServiceResponse> getServicesByOwner(String autoAddaId, VendorServiceType type) {
        Integer owner = Integer.parseInt(autoAddaId);
        return vendorServiceMapper.toResponseList(type != null
                ? vendorServiceRepository.findByVendor_CompanyOwnerAndServiceTypeAndIsActiveTrue(owner, type.name())
                : vendorServiceRepository.findByVendor_CompanyOwnerAndIsActiveTrue(owner));
    }

    @Override
    public List<VendorServiceResponse> getServices(VendorServiceType type) {
        return vendorServiceMapper.toResponseList(type != null
                ? vendorServiceRepository.findByServiceTypeAndIsActiveTrue(type.name())
                : vendorServiceRepository.findByIsActiveTrue());
    }

    @Override
    public List<ServiceAttributeDefinitionResponse> getVendorAttributes(VendorServiceType type) {
        return attributeMapper.toResponseList(
                attributeDefinitionService.resolveVendorAttributes(categoryFor(type).getId()));
    }

    // ==================== helpers ====================

    private Vendor getVendorOrThrow(Integer vendorId) {
        return vendorRepository.findById(vendorId)
                .orElseThrow(() -> new VendorNotFoundException("Vendor not found with id: " + vendorId));
    }

    private VendorService getServiceOrThrow(Integer vendorServiceId) {
        return vendorServiceRepository.findById(vendorServiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor service not found with id: " + vendorServiceId));
    }

    private ServiceCategory categoryFor(VendorServiceType type) {
        return serviceCategoryRepository.findByCode(type.getCategoryCode())
                .orElseThrow(() -> new ResourceNotFoundException("Service category " + type.getCategoryCode()
                        + " for " + type.getDisplayName() + " not found; is catalog.seed.enabled on?"));
    }

    /**
     * Checks the values against the category's VENDOR attributes and returns them as JSON.
     */
    private String checkedAttributes(VendorService service, Map<String, Object> values) {
        Map<String, Object> submitted = values != null ? values : Map.of();
        attributeValidator.validateOrThrow(fieldSpecFactory.forVendorService(
                attributeDefinitionService.resolveVendorAttributes(service.getServiceCategory().getId())), submitted);
        if (submitted.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(submitted);
        } catch (JsonProcessingException e) {
            throw new BusinessException("attributes could not be stored");
        }
    }

    private VendorOnboardingHandler resolveHandler(VendorServiceType type) {
        return onboardingHandlers.stream()
                .filter(h -> h.type() == type)
                .findFirst()
                .orElse(defaultOnboardingHandler);
    }

    private void attachExtraCategories(VendorService service, List<Integer> categoryIds) {
        if (categoryIds == null || categoryIds.isEmpty()) {
            return;
        }
        // extra categories are attached as their own VendorService rows sharing this service's schedule fields
        Integer primaryCategoryId = service.getServiceCategory() != null ? service.getServiceCategory().getId() : null;
        categoryIds.stream()
                .filter(cid -> !cid.equals(primaryCategoryId))
                .filter(cid -> vendorServiceRepository.findByVendorIdAndServiceCategoryId(service.getVendor().getId(), cid).isEmpty())
                .forEach(cid -> {
                    ServiceCategory cat = serviceCategoryRepository.findById(cid)
                            .orElseThrow(() -> new ResourceNotFoundException("invalid service category id: " + cid));
                    VendorService extra = new VendorService();
                    extra.setVendor(service.getVendor());
                    extra.setServiceCategory(cat);
                    extra.setIsActive(true);
                    extra.setServiceType(service.getServiceType());
                    extra.setOpeningTime(service.getOpeningTime());
                    extra.setClosingTime(service.getClosingTime());
                    extra.setWorkingDays(service.getWorkingDays());
                    extra.setConcurrentAppointmentCapacity(service.getConcurrentAppointmentCapacity());
                    vendorServiceRepository.save(extra);
                });
    }

    private void rebuildWorkingHours(VendorService service, VendorServiceOnboardingRequest request) {
        workHoursRepository.deleteAll(workHoursRepository.findByVendorServiceId(service.getId()));
        if (request.getWorkingDays() == null) {
            return;
        }
        request.getWorkingDays().forEach(day -> {
            VendorWorkHours wh = new VendorWorkHours();
            wh.setVendorService(service);
            wh.setDayOfWeek(day);
            wh.setOpenTime(request.getOpeningTime());
            wh.setCloseTime(request.getClosingTime());
            wh.setClosed(false);
            workHoursRepository.save(wh);
        });
    }

    private void rebuildBreaks(VendorService service, VendorServiceOnboardingRequest request) {
        breakTimeRepository.deleteAll(breakTimeRepository.findByVendorServiceId(service.getId()));
        if (request.getWorkingDays() == null
                || request.getBreakStartTime() == null || request.getBreakEndTime() == null) {
            return;
        }
        request.getWorkingDays().forEach(day -> {
            VendorBreakTime bt = new VendorBreakTime();
            bt.setVendorService(service);
            bt.setDayOfWeek(day);
            bt.setBreakStart(request.getBreakStartTime());
            bt.setBreakEnd(request.getBreakEndTime());
            breakTimeRepository.save(bt);
        });
    }

    private MediaFile resolveMedia(Long id) {
        return mediaFileRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Media file not found with id: " + id));
    }

    private void addMedia(List<Long> ids, List<MediaFile> target) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        ids.forEach(id -> target.add(resolveMedia(id)));
    }

    private void replaceMedia(List<MediaFile> target, List<Long> ids) {
        List<Long> old = target.stream().map(MediaFile::getId).toList();
        if (!old.isEmpty()) {
            scheduleMediaFileCleanup(old);
        }
        target.clear();
        addMedia(ids, target);
    }

    private static String timeString(java.time.LocalTime t) {
        return t != null ? t.toString() : null;
    }

    private static String joinDays(List<DayOfWeek> days) {
        return days.stream().map(Enum::name).collect(Collectors.joining(","));
    }

    private static <T> void applyIfPresent(T value, Consumer<T> setter) {
        if (value != null) {
            setter.accept(value);
        }
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
                log.error("Error cleaning up media file with id: {}", mediaFileId, e);
            }
        }
    }

    private boolean isMediaFileStillReferenced(Long mediaFileId) {
        return vendorRepository.countByCompanyLogoId(mediaFileId) > 0
                || vendorRepository.countByVatCertificateId(mediaFileId) > 0;
    }
}
