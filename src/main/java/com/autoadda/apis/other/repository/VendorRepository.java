package com.autoadda.apis.other.repository;

import com.autoadda.apis.other.entity.Vendor;
import com.autoadda.apis.other.util.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface VendorRepository extends JpaRepository<Vendor, Integer> {

    List<Vendor> findByIsActiveTrue();

    List<Vendor> findByAccountStatus(Status status);

    List<Vendor> findByAccountStatusAndIsActiveTrue(Status status);

    Optional<Vendor> findByEmail(String email);

    Optional<Vendor> findByEmailAndIsActiveTrue(String email);

    List<Vendor> findByCompanyOwner(Integer ownerId);

    List<Vendor> findByCompanyOwnerAndIsActiveTrue(Integer ownerId);

    @Query("SELECT o FROM Vendor o WHERE (o.phoneNumber = :contact OR o.mobileNumber = :contact) AND o.isActive = true")
    List<Vendor> findByPhoneNumberOrMobileNumberAndIsActiveTrue(@Param("contact") String contact);

    @Query("SELECT o FROM Vendor o WHERE LOWER(o.companyBrandName) LIKE LOWER(CONCAT('%', :brandName, '%')) AND o.isActive = true")
    List<Vendor> findByCompanyBrandNameContainingIgnoreCaseAndIsActiveTrue(@Param("brandName") String brandName);

    @Query("SELECT o FROM Vendor o WHERE LOWER(o.name) = LOWER(:name) AND o.isActive = true")
    Optional<Vendor> findByNameIgnoreCase(@Param("name") String name);

    // Media-file reference counts (company-level only now)
    @Query("SELECT COUNT(o) FROM Vendor o WHERE o.companyLogo.id = :mediaFileId")
    long countByCompanyLogoId(@Param("mediaFileId") Long mediaFileId);

    @Query("SELECT COUNT(v) FROM Vendor v JOIN v.vatCertificate vc WHERE vc.id = :mediaFileId")
    long countByVatCertificateId(@Param("mediaFileId") Long mediaFileId);

    @Query("SELECT DISTINCT v FROM Vendor v " +
            "LEFT JOIN FETCH v.companyLocation " +
            "LEFT JOIN FETCH v.services s " +
            "LEFT JOIN FETCH s.serviceCategory " +
            "LEFT JOIN FETCH s.serviceLocation")
    Set<Vendor> findAllWithLocationAndServices();
}
