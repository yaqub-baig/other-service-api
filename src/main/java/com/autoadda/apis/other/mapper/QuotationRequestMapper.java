package com.autoadda.apis.other.mapper;

import com.autoadda.apis.other.entity.QuotationRequest;
import com.autoadda.apis.other.response.QuotationRequestResponse;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class QuotationRequestMapper {

    private final ServiceCategoryMapper serviceCategoryMapper;
    private final QuotationRequestFieldValueMapper fieldValueMapper;
    private final VendorQuotationMapper vendorQuotationMapper;

    public QuotationRequestMapper(ServiceCategoryMapper serviceCategoryMapper,
                                  QuotationRequestFieldValueMapper fieldValueMapper,
                                  VendorQuotationMapper vendorQuotationMapper) {
        this.serviceCategoryMapper = serviceCategoryMapper;
        this.fieldValueMapper = fieldValueMapper;
        this.vendorQuotationMapper = vendorQuotationMapper;
    }

    public QuotationRequestResponse toResponse(QuotationRequest entity) {
        return Optional.ofNullable(entity).map(e -> {
            QuotationRequestResponse response = new QuotationRequestResponse();
            response.setId(e.getId());
            response.setQuotationNumber(e.getQuotationNumber());
            response.setServiceCategory(serviceCategoryMapper.toResponse(e.getServiceCategory()));
            if (e.getCatalogService() != null) {
                response.setServiceId(e.getCatalogService().getId());
                response.setServiceCode(e.getCatalogService().getCode());
                response.setServiceName(e.getCatalogService().getName());
            }
            response.setCustomerId(e.getCustomerId());
            response.setServiceLocationType(e.getServiceLocationType());
            response.setCity(e.getCity());
            response.setArea(e.getArea());
            response.setAddress(e.getAddress());
            response.setLatitude(e.getLatitude());
            response.setLongitude(e.getLongitude());
            response.setStatus(e.getStatus());
            response.setCustomerNotes(e.getCustomerNotes());
            response.setExpiresAt(e.getExpiresAt());
            response.setBroadcastCount(e.getBroadcastCount());
            response.setQuotesReceivedCount(e.getQuotesReceivedCount());
            response.setFieldValues(fieldValueMapper.toResponseList(e.getFieldValues()));
            response.setVendorQuotations(vendorQuotationMapper.toResponseList(e.getVendorQuotations()));
            response.setCreatedAt(e.getCreatedAt());
            response.setUpdatedAt(e.getUpdatedAt());
            return response;
        }).orElse(null);
    }

    public List<QuotationRequestResponse> toResponseList(List<QuotationRequest> entities) {
        if (entities == null || entities.isEmpty()) {
            return Collections.emptyList();
        }
        return entities.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
}
