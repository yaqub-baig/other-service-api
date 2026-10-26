package com.autoadda.apis.other.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MarketAvailabilityRequest {
    private String countryCode;
    private String currencyCode;
    private Boolean isAvailable;
    private Map<String, Object> regulatoryConfig;
    private String regulatoryNote;
    private String regulatoryNoteAr;
}
