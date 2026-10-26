package com.autoadda.apis.other.mapper;

import com.autoadda.apis.other.entity.QuotationRequestFieldValue;
import com.autoadda.apis.other.response.QuotationRequestFieldValueResponse;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class QuotationRequestFieldValueMapper {

    public QuotationRequestFieldValueResponse toResponse(QuotationRequestFieldValue entity) {
        return Optional.ofNullable(entity).map(e -> {
            QuotationRequestFieldValueResponse response = new QuotationRequestFieldValueResponse();
            response.setId(e.getId());
            response.setFieldCode(e.getFieldCode());
            response.setFieldLabel(e.getFieldLabel());
            response.setFieldValue(e.getFieldValue());
            response.setFileUrl(e.getFileUrl());
            response.setCreatedAt(e.getCreatedAt());
            return response;
        }).orElse(null);
    }

    public List<QuotationRequestFieldValueResponse> toResponseList(List<QuotationRequestFieldValue> entities) {
        if (entities == null || entities.isEmpty()) {
            return Collections.emptyList();
        }
        return entities.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
}
