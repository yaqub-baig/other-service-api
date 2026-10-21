package com.autoadda.apis.other.response;

import com.autoadda.apis.other.util.SchemaVersionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SchemaVersionResponse {
    private Integer id;
    private Integer serviceId;
    private String serviceCode;
    private Integer versionNo;
    private SchemaVersionStatus status;
    private String notes;
    private Integer attributeCount;
    private LocalDateTime publishedAt;
    private LocalDateTime createdAt;
}
