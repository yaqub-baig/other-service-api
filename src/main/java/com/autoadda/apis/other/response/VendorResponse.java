package com.autoadda.apis.other.response;

import com.autoadda.apis.other.entity.Location;
import com.autoadda.apis.other.entity.MediaFile;
import com.autoadda.apis.other.util.Status;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VendorResponse {
    private Integer id;
    private String companyBrandName;
    private String companyArabicName;
    private String name;
    private String trnNumber;
    private String webSite;
    private String email;
    private String phoneNumber;
    private String mobileNumber;
    private Location compnayLocation;
    private MediaFile companyLogo;
    private Status accountStatus;
    private LocalDateTime createdAt;
    private Integer companyOwnerId;
    private Boolean isActive;
    private List<MediaFile> vatCertificate;
}
