package com.autoadda.apis.other.service.vendor;

import com.autoadda.apis.other.entity.CarWashDetails;
import com.autoadda.apis.other.repository.CarWashDetailsRepository;
import com.autoadda.apis.other.repository.VendorRepository;
import com.autoadda.apis.other.request.vendor.CarWashVendorRequest;
import com.autoadda.apis.other.response.VendorProfileResponse;
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
 * Car Wash orchestration: delegates the shared vendor lifecycle to
 * {@link VendorManagementService} and owns the {@code car_wash_details} table.
 *
 * <p>This is the per-service template. A new service copies this class,
 * swapping the {@code VendorServiceType}, request/response types and detail
 * repository.</p>
 */
@Service
@RequiredArgsConstructor
public class CarWashVendorService {

    private static final VendorServiceType TYPE = VendorServiceType.CAR_WASH;

    private final VendorManagementService vendorManagementService;
    private final CarWashDetailsRepository detailsRepository;
    private final VendorRepository vendorRepository;

    @Transactional
    public CarWashVendorResponse create(CarWashVendorRequest request, String autoAddaId) {
        VendorProfileResponse profile = vendorManagementService.createVendor(request, TYPE, autoAddaId);
        CarWashDetails details = upsertDetails(profile.getId(), request);
        return new CarWashVendorResponse(profile, toResponse(details));
    }

    @Transactional
    public CarWashVendorResponse update(Integer id, CarWashVendorRequest request) {
        VendorProfileResponse profile = vendorManagementService.updateVendor(id, request);
        CarWashDetails details = upsertDetails(id, request);
        return new CarWashVendorResponse(profile, toResponse(details));
    }

    @Transactional(readOnly = true)
    public List<CarWashVendorResponse> getMine(String autoAddaId) {
        return vendorManagementService.getVendorsByOwnerIdAndType(autoAddaId, TYPE).stream()
                .map(this::withDetails)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CarWashVendorResponse> getAll() {
        return vendorManagementService.findVendorsByServiceType(TYPE.name()).stream()
                .map(this::withDetails)
                .toList();
    }

    private CarWashVendorResponse withDetails(VendorProfileResponse profile) {
        CarWashDetailsResponse details = detailsRepository.findById(profile.getId())
                .map(this::toResponse)
                .orElse(null);
        return new CarWashVendorResponse(profile, details);
    }

    /** Creates the detail row on first call, patches the provided fields on later calls. */
    private CarWashDetails upsertDetails(Integer vendorId, CarWashVendorRequest request) {
        CarWashDetails details = detailsRepository.findById(vendorId).orElseGet(() -> {
            CarWashDetails fresh = new CarWashDetails();
            fresh.setVendor(vendorRepository.getReferenceById(vendorId));
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
