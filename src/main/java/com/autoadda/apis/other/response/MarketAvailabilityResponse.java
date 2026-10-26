package com.autoadda.apis.other.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MarketAvailabilityResponse {
    private Integer id;
    private Integer serviceId;
    private String serviceCode;
    private String countryCode;
    private String currencyCode;
    private Boolean isAvailable;
    private String regulatoryConfig; // Raw JSON
    private String regulatoryNote;
    private String regulatoryNoteAr;
    private LocalDateTime updatedAt;
}
