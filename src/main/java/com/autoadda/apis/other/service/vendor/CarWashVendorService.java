package com.autoadda.apis.other.service.vendor;

import com.autoadda.apis.other.entity.CarWashDetails;
import com.autoadda.apis.other.repository.CarWashDetailsRepository;
import com.autoadda.apis.other.repository.VendorServiceRepository;
import com.autoadda.apis.other.request.vendor.CarWashVendorRequest;
import com.autoadda.apis.other.request.vendor.CarWashVendorServiceRequest;
import com.autoadda.apis.other.response.VendorProfileResponse;
import com.autoadda.apis.other.response.VendorServiceResponse;
import com.autoadda.apis.other.response.vendor.CarWashDetailsResponse;
import com.autoadda.apis.other.response.vendor.CarWashVendorResponse;
import com.autoadda.apis.other.service.VendorManagementService;
import com.autoadda.apis.other.util.VendorServiceType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.function.Consumer;

/**
 * Car Wash orchestration: delegates the shared vendor/service lifecycle to
 * {@link VendorManagementService} and owns the {@code car_wash_details} table
 * (keyed by vendorService id).
 *
 * <p>Per-service template — a new service copies this class, swapping the
 * {@code VendorServiceType}, request/response types and detail repository.</p>
 */
@Service
@RequiredArgsConstructor
public class CarWashVendorService {

    private static final VendorServiceType TYPE = VendorServiceType.CAR_WASH;

    private final VendorManagementService vendorManagementService;
    private final CarWashDetailsRepository detailsRepository;
    private final VendorServiceRepository vendorServiceRepository;

    /** Create the company and its first car-wash service in one call. */
    @Transactional
    public CarWashVendorResponse create(CarWashVendorRequest request, String autoAddaId) {
        VendorProfileResponse company = vendorManagementService.createCompany(request.getCompany(), autoAddaId);
        VendorServiceResponse service = vendorManagementService.addService(
                company.getId(), request.getService(), TYPE, autoAddaId);
        CarWashDetails details = upsertDetails(service.getId(), request.getService());
        return new CarWashVendorResponse(company, service, toResponse(details));
    }

    /** Add a car-wash service to an existing vendor company. */
    @Transactional
    public CarWashVendorResponse addToCompany(Integer vendorId, CarWashVendorServiceRequest request, String autoAddaId) {
        VendorServiceResponse service = vendorManagementService.addService(vendorId, request, TYPE, autoAddaId);
        CarWashDetails details = upsertDetails(service.getId(), request);
        VendorProfileResponse company = vendorManagementService.getVendorById(vendorId).orElse(null);
        return new CarWashVendorResponse(company, service, toResponse(details));
    }

    @Transactional
    public CarWashVendorResponse updateService(Integer vendorServiceId, CarWashVendorServiceRequest request) {
        VendorServiceResponse service = vendorManagementService.updateService(vendorServiceId, request);
        CarWashDetails details = upsertDetails(vendorServiceId, request);
        VendorProfileResponse company = service.getVendorId() != null
                ? vendorManagementService.getVendorById(service.getVendorId()).orElse(null) : null;
        return new CarWashVendorResponse(company, service, toResponse(details));
    }

    @Transactional(readOnly = true)
    public List<CarWashVendorResponse> getMine(String autoAddaId) {
        return vendorManagementService.getServicesByOwnerAndType(autoAddaId, TYPE).stream()
                .map(this::withDetails)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CarWashVendorResponse> getAll() {
        return vendorManagementService.getServicesByType(TYPE).stream()
                .map(this::withDetails)
                .toList();
    }

    private CarWashVendorResponse withDetails(VendorServiceResponse service) {
        CarWashDetailsResponse details = detailsRepository.findById(service.getId())
                .map(this::toResponse)
                .orElse(null);
        VendorProfileResponse company = service.getVendorId() != null
                ? vendorManagementService.getVendorById(service.getVendorId()).orElse(null) : null;
        return new CarWashVendorResponse(company, service, details);
    }

    private CarWashDetails upsertDetails(Integer vendorServiceId, CarWashVendorServiceRequest request) {
        CarWashDetails details = detailsRepository.findById(vendorServiceId).orElseGet(() -> {
            CarWashDetails fresh = new CarWashDetails();
            fresh.setVendorService(vendorServiceRepository.getReferenceById(vendorServiceId));
            return fresh;
        });

        setIfPresent(request.getBayCount(), details::setBayCount);
        setIfPresent(request.getAverageWashDurationMinutes(), details::setAverageWashDurationMinutes);
        setIfPresent(request.getMobileService(), details::setMobileService);
        setIfPresent(request.getSelfServiceAvailable(), details::setSelfServiceAvailable);
        setIfPresent(request.getExpressWashAvailable(), details::setExpressWashAvailable);
        setIfPresent(request.getInteriorDetailingAvailable(), details::setInteriorDetailingAvailable);
        setIfPresent(request.getWaterRecycling(), details::setWaterRecycling);
        setIfPresent(request.getAcceptsWalkIns(), details::setAcceptsWalkIns);

        return detailsRepository.save(details);
    }

    private CarWashDetailsResponse toResponse(CarWashDetails d) {
        if (d == null) {
            return null;
        }
        return CarWashDetailsResponse.builder()
                .bayCount(d.getBayCount())
                .averageWashDurationMinutes(d.getAverageWashDurationMinutes())
                .mobileService(d.getMobileService())
                .selfServiceAvailable(d.getSelfServiceAvailable())
                .expressWashAvailable(d.getExpressWashAvailable())
                .interiorDetailingAvailable(d.getInteriorDetailingAvailable())
                .waterRecycling(d.getWaterRecycling())
                .acceptsWalkIns(d.getAcceptsWalkIns())
                .build();
    }

    private static <T> void setIfPresent(T value, Consumer<T> setter) {
        if (value != null) {
            setter.accept(value);
        }
    }
}
