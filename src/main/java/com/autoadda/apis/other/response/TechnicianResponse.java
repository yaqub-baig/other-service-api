package com.autoadda.apis.other.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TechnicianResponse {
    private Integer id;
    private String name;
    private String email;
    private String phoneNumber;
    // Add other relevant fields from Technician entity if needed
}
