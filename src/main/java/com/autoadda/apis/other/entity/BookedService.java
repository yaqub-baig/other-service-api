package com.autoadda.apis.other.entity;

import com.autoadda.apis.other.util.Status;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "booking_services")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BookedService {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    @Column(name = "status")
    private Status status;

    @Column(name = "booking_id")
    private Integer bookingId;

    @ManyToOne
    @JoinColumn(name = "vendor_service_bridge_id", referencedColumnName = "id")
    private VendorService vendorService;

    @Column(name = "price")
    private Double price;

    @Column(name = "labour_price")
    private Double labourPrice;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "technician_id", referencedColumnName = "id")
    private Technician technician;

}
