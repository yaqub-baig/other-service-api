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
    private String name;
    private String description;

    @OneToOne
    @JoinColumn(name = "image")
    private MediaFile image;

    @ManyToOne
    @JoinColumn(name = "parent_id")
    private ServiceCategory parent;
}