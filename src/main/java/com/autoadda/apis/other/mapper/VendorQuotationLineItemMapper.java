package com.autoadda.apis.other.mapper;

import com.autoadda.apis.other.entity.VendorQuotationLineItem;
import com.autoadda.apis.other.response.VendorQuotationLineItemResponse;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class VendorQuotationLineItemMapper {

    public VendorQuotationLineItemResponse toResponse(VendorQuotationLineItem entity) {
        return Optional.ofNullable(entity).map(e -> {
            VendorQuotationLineItemResponse response = new VendorQuotationLineItemResponse();
            response.setId(e.getId());
            response.setItemType(e.getItemType());
            response.setTitle(e.getTitle());
            response.setDescription(e.getDescription());
            response.setQuantity(e.getQuantity());
            response.setUnit(e.getUnit());
            response.setUnitPrice(e.getUnitPrice());
            response.setDiscount(e.getDiscount());
            response.setTaxRate(e.getTaxRate());
            response.setTotalPrice(e.getTotalPrice());
            response.setDisplayOrder(e.getDisplayOrder());
            response.setIsOptional(e.getIsOptional());
            response.setCreatedAt(e.getCreatedAt());
            return response;
        }).orElse(null);
    }

    public List<VendorQuotationLineItemResponse> toResponseList(List<VendorQuotationLineItem> entities) {
        if (entities == null || entities.isEmpty()) {
            return Collections.emptyList();
        }
        return entities.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
}
