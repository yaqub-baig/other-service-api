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

    // Find all active other services
    List<Vendor> findByIsActiveTrue();

    // Find by company brand name (case-insensitive)
    @Query("SELECT o FROM Vendor o WHERE LOWER(o.companyBrandName) LIKE LOWER(CONCAT('%', :brandName, '%')) AND o.isActive = true")
    List<Vendor> findByCompanyBrandNameContainingIgnoreCase(@Param("brandName") String brandName);

    // Find by service type (case-insensitive)
    List<Vendor> findByServiceTypeContainingIgnoreCase(String serviceType);

    // Find by status
    List<Vendor> findByAccountStatus(Status status);

    // Find by chain ID
//    List<OtherService> findByChainId(Integer chainId);

    // Find by email
    Optional<Vendor> findByEmail(String email);

    // Find by phone number or mobile number
    @Query("SELECT o FROM Vendor o WHERE (o.phoneNumber = :contact OR o.mobileNumber = :contact) AND o.isActive = true")
    List<Vendor> findByPhoneNumberOrMobileNumber(@Param("contact") String contact);

    // Find by company owner ID
    List<Vendor> findByCompanyOwner(Integer ownerId);

    // Find by company brand name exact match (case-insensitive)
    @Query("SELECT o FROM Vendor o WHERE LOWER(o.companyBrandName) = LOWER(:brandName) AND o.isActive = true")
    Optional<Vendor> findByCompanyBrandNameIgnoreCase(@Param("brandName") String brandName);

    // Find by name (case-insensitive)
    @Query("SELECT o FROM Vendor o WHERE LOWER(o.name) = LOWER(:name) AND o.isActive = true")
    Optional<Vendor> findByNameIgnoreCase(@Param("name") String name);

    // Find by service type and active status
    List<Vendor> findByServiceTypeAndIsActiveTrue(String serviceType);

    // Find by status and active status
    List<Vendor> findByAccountStatusAndIsActiveTrue(Status status);

    // Find by chain ID and active status
//    List<OtherService> findByChainIdAndIsActiveTrue(Integer chainId);

    // Find by email and active status
    Optional<Vendor> findByEmailAndIsActiveTrue(String email);

    // Find by phone number or mobile number and active status
    @Query("SELECT o FROM Vendor o WHERE (o.phoneNumber = :contact OR o.mobileNumber = :contact) AND o.isActive = true")
    List<Vendor> findByPhoneNumberOrMobileNumberAndIsActiveTrue(@Param("contact") String contact);

    // Find by company owner ID and active status
    List<Vendor> findByCompanyOwnerAndIsActiveTrue(Integer ownerId);

    // Count references to media files
    @Query("SELECT COUNT(o) FROM Vendor o WHERE o.companyLogo.id = :mediaFileId")
    long countByCompanyLogoId(@Param("mediaFileId") Long mediaFileId);

    @Query("SELECT COUNT(os) FROM Vendor os JOIN os.vatCertificate vc WHERE vc.id = :mediaFileId")
    long countByVatCertificateId(@Param("mediaFileId") Long mediaFileId);

    @Query("SELECT COUNT(os) FROM Vendor os JOIN os.tradeLicenseAttachment tla WHERE tla.id = :mediaFileId")
    long countByTradeLicenseAttachmentId(@Param("mediaFileId") Long mediaFileId);

    @Query("SELECT COUNT(os) FROM Vendor os JOIN os.companyImages ci WHERE ci.id = :mediaFileId")
    long countByCompanyImagesId(@Param("mediaFileId") Long mediaFileId);

    // Find by company brand name containing and active (case-insensitive)
    @Query("SELECT o FROM Vendor o WHERE LOWER(o.companyBrandName) LIKE LOWER(CONCAT('%', :brandName, '%')) AND o.isActive = true")
    List<Vendor> findByCompanyBrandNameContainingIgnoreCaseAndIsActiveTrue(@Param("brandName") String brandName);

    Optional<Vendor> findByTradeLicenceNumber(String tradeLicenceNumber);
    @Query("SELECT DISTINCT g FROM Vendor g " +
            "LEFT JOIN FETCH g.companyLocation " +
            "LEFT JOIN FETCH g.services s " +
            "LEFT JOIN FETCH s.availableService")
    Set<Vendor> findAllWithLocationAndServices();
}
