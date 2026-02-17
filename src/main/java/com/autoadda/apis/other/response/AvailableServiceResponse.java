package com.autoadda.apis.other.response;

import com.autoadda.apis.other.entity.MediaFile;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class AvailableServiceResponse {

	private Integer id;
	private String name;
	private String description;
	private MediaFile image;
	private Boolean isActive;
	private Double price;
	private Double laborPrice;
	private Integer garageId;
}
