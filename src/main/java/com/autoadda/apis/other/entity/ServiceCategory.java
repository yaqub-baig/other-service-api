package com.autoadda.apis.other.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class ServiceCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    /**
     * Stable machine code, e.g. "TIRES_WHEELS". Optional for categories created before codes existed.
     */
    @Column(name = "code", unique = true, length = 60)
    private String code;

    private String name;
    private String description;

    @Column(name = "display_order")
    private Integer displayOrder = 0;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @OneToOne
    @JoinColumn(name = "image")
    private MediaFile image;

    @ManyToOne
    @JoinColumn(name = "parent_id")
    private ServiceCategory parent;
}