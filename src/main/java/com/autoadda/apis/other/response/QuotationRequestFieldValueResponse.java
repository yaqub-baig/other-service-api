package com.autoadda.apis.other.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class QuotationRequestFieldValueResponse {
    private Integer id;
    private String fieldCode;
    private String fieldLabel;
    private String fieldValue; // Raw JSON string
    private String fileUrl;
    private LocalDateTime createdAt;
}
