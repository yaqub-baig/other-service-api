package com.autoadda.apis.other.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LocationResponse {
    private String address;
    private String city;
    private String state;
    private String country;
    private String zip;
    private Double latitude;
    private Double longitude;
}
