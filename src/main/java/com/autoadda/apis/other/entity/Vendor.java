package com.autoadda.apis.other.entity;


import com.autoadda.apis.other.util.Status;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * A vendor company. Company-level identity only — everything specific to a
 * service the vendor offers (working hours, capacity, trade licence, branch
 * address, branch images, booking slots) lives on {@link VendorService}.
 */
@Entity
@Getter
@Setter
public class Vendor {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Integer id;

    @Column(name = "company_brand_name")
    private String companyBrandName;

    @Column(name = "company_arabic_name")
    private String companyArabicName;

    @Column(name = "company_name")
    private String name;

    @Column(name = "trn_number")
    private String trnNumber;

    @Column(name = "website")
    private String webSite;

    @Column(name = "email_id")
    private String email;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "mobile_number")
    private String mobileNumber;

    @OneToOne
    @JoinColumn(name = "company_logo")
    private MediaFile companyLogo;

    /** Registered / HQ address. Each service may override with its own branch address. */
    @OneToOne
    private Location companyLocation;

    @Column(name = "account_status")
    private Status accountStatus;

    /**
     * ISO 3166-1 alpha-2 country the vendor operates in; decides which services and currency apply.
     */
    @Column(name = "country_code", length = 2)
    private String countryCode;

    @JsonManagedReference("vendor-services")
    @OneToMany(mappedBy = "vendor", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<VendorService> services = new ArrayList<>();

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "company_owner_id")
    private Integer companyOwner;

    private boolean isActive;

    @OneToMany
    @JoinColumn(name = "company_vat_certificate")
    private List<MediaFile> vatCertificate = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        isActive = true;
        createdAt = LocalDateTime.now();
    }
}
