package com.autoadda.apis.other.mapper;

import com.autoadda.apis.other.entity.VendorQuotation;
import com.autoadda.apis.other.response.VendorQuotationResponse;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class VendorQuotationMapper {

    private final VendorProfileMapper vendorProfileMapper;
    private final VendorQuotationLineItemMapper lineItemMapper;

    public VendorQuotationMapper(VendorProfileMapper vendorProfileMapper, VendorQuotationLineItemMapper lineItemMapper) {
        this.vendorProfileMapper = vendorProfileMapper;
        this.lineItemMapper = lineItemMapper;
    }

    public VendorQuotationResponse toResponse(VendorQuotation entity) {
        return Optional.ofNullable(entity).map(e -> {
            VendorQuotationResponse response = new VendorQuotationResponse();
            response.setId(e.getId());
            response.setQuoteNumber(e.getQuoteNumber());
            response.setQuotationRequestId(e.getQuotationRequest() != null ? e.getQuotationRequest().getId() : null);
            response.setVendor(vendorProfileMapper.toVendorProfileResponse(e.getVendor()));
            response.setPricingType(e.getPricingType());
            response.setTotalAmount(e.getTotalAmount());
            response.setMinAmount(e.getMinAmount());
            response.setMaxAmount(e.getMaxAmount());
            response.setInspectionFee(e.getInspectionFee());
            response.setEstimatedDuration(e.getEstimatedDuration());
            response.setWarrantyInfo(e.getWarrantyInfo());
            response.setValidUntil(e.getValidUntil());
            response.setStatus(e.getStatus());
            response.setVendorNotes(e.getVendorNotes());
            response.setRejectionReason(e.getRejectionReason());
            response.setRespondedAt(e.getRespondedAt());
            response.setLineItems(lineItemMapper.toResponseList(e.getLineItems()));
            response.setCreatedAt(e.getCreatedAt());
            response.setUpdatedAt(e.getUpdatedAt());
            return response;
        }).orElse(null);
    }

    public List<VendorQuotationResponse> toResponseList(List<VendorQuotation> entities) {
        if (entities == null || entities.isEmpty()) {
            return Collections.emptyList();
        }
        return entities.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
}
