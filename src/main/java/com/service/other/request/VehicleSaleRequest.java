package com.service.other.request;

import java.time.LocalDate;

import com.service.other.entity.VehicleDetails;
import com.service.other.entity.VehicleOwner;
import com.service.other.util.Status;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehicleSaleRequest {

	private Integer id;

	private VehicleDetails vehicle;
	private Integer vehicleId;
	private VehicleOwner previousOwner;
	private Integer previousOwnerId;
	private VehicleOwner newOwner;
	private Integer newOwnerId;
	private LocalDate saleDate;

	private double salePrice;

	@Enumerated(EnumType.STRING)
	private Status status; // ACTIVE, REMOVED

	private InspectionReportRequest inspectionReport;

	private VehicleSaleInvoiceRequest invoice;
}
