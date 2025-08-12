package com.autoadda.apis.other.response;

import java.time.LocalDate;

import com.autoadda.apis.other.util.Status;

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
public class AppointmentServices {

	private Integer id;

	private Status status;

	private Integer appointmentId;

	private LocalDate insert_tms;

	private GarageService service;

	private GarageTechnician technician;

}
