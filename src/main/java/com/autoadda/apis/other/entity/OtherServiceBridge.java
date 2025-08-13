package com.autoadda.apis.other.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "other_service_bridge")
@Data
@Getter
@Setter
public class OtherServiceBridge {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Integer id;

    @Column(name = "is_active")
    private Boolean isActive;

    @ManyToOne
    @JoinColumn(name = "other_garage_id")
    private OtherService otherServiceGarage;

    @ManyToOne
    @JoinColumn(name = "service_id")
    private AvailableService availableService;

}
