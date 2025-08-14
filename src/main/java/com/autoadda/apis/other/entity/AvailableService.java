package com.autoadda.apis.other.entity;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AvailableService {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id")
	private Integer id;

	@Column(name = "name")
	private String name;

	@Column(name = "description")
	private String description;

	@Column(name = "price")
	private Double price;

	@Column(name = "labor_price")
	private Double laborPrice;

	@OneToMany(mappedBy = "availableService", cascade = CascadeType.ALL, orphanRemoval = true)
	@JsonIgnore
	private List<GarageService> garageLinks = new ArrayList<>();

	@OneToMany(mappedBy = "availableService", cascade = CascadeType.ALL, orphanRemoval = true)
	@JsonIgnore
	private List<OtherServiceBridge> otherGarageLinks = new ArrayList<>();

}
